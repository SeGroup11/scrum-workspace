package team.segroup11.review.github

import com.fasterxml.jackson.annotation.JsonProperty
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskType
import git4idea.repo.GitRepositoryManager
import org.jetbrains.plugins.github.api.GithubApiRequest
import org.jetbrains.plugins.github.api.GithubApiRequestExecutor
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.api.data.GithubIssue
import org.jetbrains.plugins.github.api.data.GithubIssueComment
import org.jetbrains.plugins.github.api.data.GithubIssueState
import org.jetbrains.plugins.github.api.executeSuspend
import org.jetbrains.plugins.github.authentication.GHAccountsUtil
import org.jetbrains.plugins.github.authentication.accounts.GHAccountManager
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import org.jetbrains.plugins.github.exceptions.GithubAuthenticationException
import org.jetbrains.plugins.github.exceptions.GithubRateLimitExceededException
import org.jetbrains.plugins.github.exceptions.GithubStatusCodeException
import java.io.IOException
import java.util.Locale

// Build 262's GithubIssue omits pull_request, GithubUser omits the numeric id, and
// GithubIssueCommentWithHtml exposes rendered HTML only. Keep these wire fields in this adapter.
internal class IssueWire : GithubIssue() {
    @field:JsonProperty("pull_request") var pullRequest: Map<String, Any?>? = null
}
internal class UserIdentity { var id: Long = 0 }
internal class CommentWire : GithubIssueComment() { var body: String? = null }

class GitHubSession internal constructor(
    val repo: RepoKey, val accountId: String, internal val userId: Long,
    internal val server: GithubServerPath, internal val executor: GithubApiRequestExecutor
) {
    val accountKey = "${repo.server}:$userId"
    internal val issuesUrl = "${server.toApiUrl().trimEnd('/')}/repos/${repo.owner}/${repo.repo}/issues"
}

class GitHubFailure(message: String, val outcomeUnknown: Boolean = false) : IOException(message)

private fun currentAccounts() = service<GHAccountManager>().accountsState.value

/** Call discover on a background thread. Remote calls use the selected IDE account's executor. */
class GitHubGateway(private val project: Project) {
    fun discover(): Discovery {
        val accounts = currentAccounts()
        val roots = GitRepositoryManager.getInstance(project).repositories
        val servers = (listOf(GithubServerPath.DEFAULT_SERVER) + accounts.map { it.server }).distinct()
        val repositories = buildList {
            for (root in roots) for (remote in root.remotes) for (url in remote.urls) for (server in servers) {
                val key = repositoryOf(url, server.toURI()) ?: continue
                add(RepoChoice(key, "${root.root.presentableUrl} / ${remote.name} / ${key.owner}/${key.repo}"))
            }
        }.distinctBy { it.key }
        return Discovery(repositories, accounts.map { AccountChoice(it.id, it.name, serverKey(it.server.toURI())) },
            GHAccountsUtil.getDefaultAccount(project)?.id, roots.isNotEmpty())
    }

    suspend fun connect(repo: RepoKey, accountId: String): GitHubSession = githubCall {
        if (discover().repositories.none { it.key == repo }) throw GitHubFailure("项目已不包含所选远程仓库，请重新选择。")
        val account = selectedAccount(accountId, repo)
        val executor = executorFor(account)
        val uid = userId(executor, account.server)
        val found = executor.executeSuspend(GithubApiRequests.Repos.get(account.server, repo.owner, repo.repo))
            ?: throw GitHubFailure("所选账号无法读取目标仓库。")
        val canonical = RepoKey(repo.server, found.userName, found.name)
        if (canonical != repo) throw GitHubFailure("仓库已更名或转移，请更新项目 remote 后重新选择。")
        GitHubSession(canonical, accountId, uid, account.server, executor)
    }

    suspend fun list(session: GitHubSession, state: IssueFilter = IssueFilter.ALL, page: Int = 1): Page<IssueReviewTask> = githubCall {
        require(page > 0)
        selectedAccount(session.accountId, session.repo)
        val url = "${session.issuesUrl}?state=${state.name.lowercase(Locale.ROOT)}&per_page=50&page=$page"
        val response = session.executor.executeSuspend(GithubApiRequest.Get.jsonPage<IssueWire>(url))
        Page(response.items.filter { it.pullRequest == null }.map { mapIssue(it, session, false) },
            if (response.hasNext) page + 1 else null)
    }

    suspend fun detail(session: GitHubSession, number: Long): IssueReviewTask = githubCall {
        require(number > 0)
        selectedAccount(session.accountId, session.repo)
        val issue = session.executor.executeSuspend(GithubApiRequest.Get.json<IssueWire>("${session.issuesUrl}/$number"))
        if (issue.pullRequest != null || issue.number != number) throw GitHubFailure("该编号不是此仓库的 Issue。")
        mapIssue(issue, session, true)
    }

    suspend fun comments(session: GitHubSession, number: Long, page: Int = 1): Page<ReviewComment> = githubCall {
        require(number > 0 && page > 0)
        selectedAccount(session.accountId, session.repo)
        val url = "${session.issuesUrl}/$number/comments?per_page=50&page=$page"
        val response = session.executor.executeSuspend(GithubApiRequest.Get.jsonPage<CommentWire>(url))
        Page(mergeComments(emptyList(), response.items.map { mapComment(it) }), if (response.hasNext) page + 1 else null)
    }

    suspend fun postComment(session: GitHubSession, number: Long, text: String): ReviewComment {
        require(number > 0 && text.isNotBlank())
        val verified = githubCall {
            val account = selectedAccount(session.accountId, session.repo)
            val executor = executorFor(account)
            if (userId(executor, account.server) != session.userId) throw GitHubFailure("账号身份已变更，请重新连接。")
            executor
        }
        val request = GithubApiRequest.Post.json<CommentWire>("${session.issuesUrl}/$number/comments", mapOf("body" to text))
        return githubCall(sending = true) { mapComment(verified.executeSuspend(request)) }
    }

    private fun selectedAccount(id: String, repo: RepoKey) =
        currentAccounts().firstOrNull { it.id == id && serverKey(it.server.toURI()) == repo.server }
            ?: throw GitHubFailure("所选账号已移除或服务地址不匹配，请打开 IDE GitHub 设置。")

    private suspend fun executorFor(account: GithubAccount): GithubApiRequestExecutor {
        val credential = service<GHAccountManager>().findCredentials(account)
            ?: throw GitHubFailure("此账号需在 IDE GitHub 设置中重新登录。")
        return GithubApiRequestExecutor.Factory.getInstance().create(account.server, credential)
    }

    private suspend fun userId(executor: GithubApiRequestExecutor, server: GithubServerPath): Long {
        val id = executor.executeSuspend(GithubApiRequest.Get.json<UserIdentity>("${server.toApiUrl().trimEnd('/')}/user")).id
        if (id <= 0) throw GitHubFailure("GitHub 未返回有效的认证用户编号。")
        return id
    }

    private fun mapIssue(wire: IssueWire, session: GitHubSession, detail: Boolean): IssueReviewTask {
        val expectedUrl = "${session.server.toUrl().trimEnd('/')}/${session.repo.owner}/${session.repo.repo}/issues/${wire.number}"
        if (!wire.htmlUrl.equals(expectedUrl, ignoreCase = true)) throw GitHubFailure("Issue 原文地址与当前仓库不匹配。")
        val names = wire.labels.orEmpty().map { it.name.lowercase(Locale.ROOT) }.toSet()
        val type = when {
            "bug" in names && "enhancement" !in names -> TaskType.BUG
            "enhancement" in names && "bug" !in names -> TaskType.FEATURE
            else -> TaskType.OTHER
        }
        val closed = when (wire.state) {
            GithubIssueState.open -> false
            GithubIssueState.closed -> true
            else -> throw GitHubFailure("Issue 开闭状态无法识别。")
        }
        return IssueReviewTask(session.repo, wire.number, wire.title, closed, if (detail) wire.body.orEmpty() else null,
            detail, wire.user?.login, wire.createdAt?.toInstant(), wire.updatedAt?.toInstant(), wire.htmlUrl, type)
    }

    private fun mapComment(wire: CommentWire): ReviewComment {
        if (wire.id <= 0 || wire.body == null) throw IOException("无有效评论回执。")
        return ReviewComment(wire.id, wire.user?.login, wire.createdAt?.toInstant(), wire.body.orEmpty())
    }

    private suspend fun <T> githubCall(sending: Boolean = false, action: suspend () -> T): T = try {
        action()
    } catch (e: GitHubFailure) {
        throw e
    } catch (_: GithubAuthenticationException) {
        throw GitHubFailure("GitHub 身份验证失败，请在 IDE GitHub 设置中重新登录。")
    } catch (_: GithubRateLimitExceededException) {
        throw GitHubFailure("GitHub 已限制请求频率，请等待配额恢复后再重试。")
    } catch (e: GithubStatusCodeException) {
        val code = e.statusCode
        val unknown = sending && code >= 500
        val message = when (code) {
            401 -> "身份已过期，请在 IDE GitHub 设置中重新登录。"
            403, 404 -> "目标不可见或权限不足，请核对账号和仓库。"
            429 -> "GitHub 已限制请求频率，请稍后重试。"
            else -> "GitHub 请求失败（HTTP $code）。"
        }
        throw GitHubFailure(message, unknown)
    } catch (_: IOException) {
        throw GitHubFailure(if (sending) "发送回执未知，请先到原 Issue 核对。" else "GitHub 请求失败，请检查网络并重试。", sending)
    }
}
