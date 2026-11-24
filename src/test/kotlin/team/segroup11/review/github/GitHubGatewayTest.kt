package team.segroup11.review.github

import com.intellij.tasks.TaskType
import com.intellij.util.ThrowableConvertor
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.github.api.GithubApiRequest
import org.jetbrains.plugins.github.api.GithubApiResponse
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.exceptions.GithubAuthenticationException
import org.jetbrains.plugins.github.exceptions.GithubStatusCodeException
import org.junit.Assert.*
import org.junit.Test
import java.awt.Component
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.net.URI
import java.time.Instant

/** No HTTP or IDE credentials. Feed wire JSON and pagination headers into the actual SDK decoder. */
private class Replay : ConnectionAccess {
    val repo = RepoKey("github.com", "group11", "exporter")
    val alternate = RepoKey("github.com", "group11", "other")
    val users = mutableMapOf("primary" to 11L, "secondary" to 22L)
    val calls = mutableListOf<String>()
    var status: Int? = null
    var authFailureAt: String? = null
    var confirmLogin = false
    val reauthAccounts = mutableListOf<String>()
    var commentsPageFailure = false
    var uncertainPost = false

    override fun discover() = Discovery(listOf(repo, alternate).map { RepoChoice(it, it.canonical) },
        users.keys.map { AccountChoice(it, it, repo.server) }, null, true)
    override fun isAvailable(id: String, repo: RepoKey) = id in users && repo.server == "github.com"
    override fun reauthenticate(id: String, repo: RepoKey, parent: Component): Boolean {
        if (!isAvailable(id, repo)) throw GitHubFailure("账号已移除")
        reauthAccounts += id
        if (confirmLogin) authFailureAt = null
        return confirmLogin
    }
    override suspend fun bind(id: String, repo: RepoKey): BoundAccount {
        if (!isAvailable(id, repo)) throw GitHubFailure("账号已移除")
        val transport = object : GitHubTransport {
            override suspend fun <T> execute(request: GithubApiRequest<T>): T {
                val path = URI(request.url).path
                val query = URI(request.url).query.orEmpty()
                val method = if (request is GithubApiRequest.Post) "POST" else "GET"
                calls += "$id $method $path?$query"
                if (path == authFailureAt) throw GithubAuthenticationException("server response not for display")
                status?.let { throw GithubStatusCodeException("fixture", it) }
                var next: String? = null
                val body = when {
                    path == "/user" -> """{"id":${users.getValue(id)}}"""
                    path == "/repos/${repo.owner}/${repo.repo}" -> """{"name":"${repo.repo}","owner":{"login":"${repo.owner}"}}"""
                    method == "POST" && path.endsWith("/comments") -> {
                        if (uncertainPost) throw IOException("fixture lost receipt")
                        comment(1002, "2026-11-21T02:00:00Z")
                    }
                    path.endsWith("/comments") && query.contains("page=2") -> {
                        if (commentsPageFailure) throw IOException("fixture page failed")
                        "[${comment(900,"2026-11-21T02:00:00Z")},${comment(899,"2026-11-21T02:00:00Z")}]"
                    }
                    path.endsWith("/comments") -> {
                        next = "${request.url.substringBefore('?')}?page=2"
                        "[${comment(900,"2026-11-21T02:00:00Z")}]"
                    }
                    path.endsWith("/issues") && query.contains("page=1") -> {
                        next = "${request.url.substringBefore('?')}?page=2"
                        "[${issue(repo, 18, true)},${issue(repo, 19, true)}]"
                    }
                    path.endsWith("/issues") -> "[${issue(repo, 42)}]"
                    path.substringAfterLast('/').toLongOrNull() != null -> issue(repo, path.substringAfterLast('/').toLong())
                    else -> error("Unconfigured request: $method $path")
                }
                val response = object : GithubApiResponse {
                    override fun findHeader(name: String) = if (name.equals("link", true)) next?.let { "<$it>; rel=\"next\"" } else null
                    override fun <R> readBody(reader: ThrowableConvertor<Reader, R, IOException>): R = reader.convert(body.reader())
                    override fun <R> handleBody(reader: ThrowableConvertor<InputStream, R, IOException>): R = reader.convert(body.byteInputStream())
                }
                return request.extractResult(response)
            }
        }
        return BoundAccount(GithubServerPath.DEFAULT_SERVER, transport)
    }

    private fun issue(repo: RepoKey, number: Long, pr: Boolean = false): String {
        val marker = if (pr) ""","pull_request":{"url":"https://api.github.com/pulls/123"}""" else ""
        val author = if (number == 43L) "null" else """{"login":"contributor"}"""
        return """{"number":$number,"title":"CSV 导出","html_url":"https://github.com/${repo.owner}/${repo.repo}/issues/$number","state":"closed","user":$author,"labels":[{"name":"bug"}],"body":null,"updated_at":"2026-11-20T03:00:00Z"$marker}"""
    }

    private fun comment(id: Long, created: String) =
        """{"id":$id,"user":{"login":"author"},"created_at":"$created","body":"raw *Markdown*  "}"""
}

class GitHubGatewayTest {
    @Test fun distinguishIdentityAndRepositoryAndReauthenticateSelectedAccountG1() = runBlocking {
        val fake = Replay()
        val gateway = GitHubGateway(fake)
        fun connectFailure() = assertThrows(GitHubFailure::class.java) {
            runBlocking { gateway.connect(fake.repo, "secondary") }
        }
        fake.authFailureAt = "/user"
        val identity = connectFailure()
        assertEquals(GitHubAuthStage.IDENTITY, identity.authStage)
        assertEquals(1, fake.calls.size)
        assertFalse(identity.message!!.contains("server response"))
        fake.authFailureAt = "/repos/group11/exporter"
        val repository = connectFailure()
        assertEquals(GitHubAuthStage.REPOSITORY, repository.authStage)
        assertNotEquals(identity.message, repository.message)
        val parent = javax.swing.JPanel()
        assertFalse(gateway.reauthenticate("secondary", fake.repo, parent))
        assertEquals(GitHubAuthStage.REPOSITORY, connectFailure().authStage)
        fake.confirmLogin = true
        assertTrue(gateway.reauthenticate("secondary", fake.repo, parent))
        assertEquals(22L, gateway.connect(fake.repo, "secondary").userId)
        assertEquals(listOf("secondary", "secondary"), fake.reauthAccounts)
        assertEquals(0, fake.calls.count { " POST " in it })
    }

    @Test fun remoteAndAccountIsolationG1() = runBlocking {
        val fake = Replay()
        val gateway = GitHubGateway(fake)
        val enterprise = URI("https://host.example:8443/git")
        assertEquals(RepoKey("host.example:8443/git", "foo", "bar"), repositoryOf("https://host.example:8443/git/FOO/BAR.git", enterprise))
        assertNull(repositoryOf("https://host.example/git/foo/bar.git", enterprise))
        assertNull(repositoryOf("git@alias:foo/bar.git", URI("https://github.com")))
        assertEquals(fake.repo, repositoryOf("git@github.com:Group11/EXPORTER.git", URI("https://github.com")))
        val first = gateway.connect(fake.repo, "primary")
        val second = gateway.connect(fake.repo, "secondary")
        assertNotEquals(first.accountKey, second.accountKey)
        val other = gateway.connect(fake.alternate, "primary")
        assertNotEquals(gateway.detail(first,42).id, gateway.detail(other,42).id)
        fake.users.remove("primary")
        val before = fake.calls.size
        assertThrows(GitHubFailure::class.java) { runBlocking { gateway.list(first) } }
        assertEquals(before, fake.calls.size)
        for (code in listOf(401, 403, 404, 429)) {
            fake.status = code
            assertThrows(GitHubFailure::class.java) { runBlocking { gateway.list(second) } }
        }
    }

    @Test fun prOnlyPageThenIssueAndNoCommentGetsI1() = runBlocking {
        val fake = Replay()
        val gateway = GitHubGateway(fake)
        val session = gateway.connect(fake.repo, "primary")
        val first = gateway.list(session)
        assertTrue(first.items.isEmpty())
        assertEquals(2, first.nextPage)
        val last = gateway.list(session, IssueFilter.CLOSED, first.nextPage!!)
        val task = last.items.single()
        assertNull(last.nextPage)
        assertTrue(fake.calls.last().contains("state=closed"))
        assertEquals("42", task.number)
        assertTrue(task.isClosed)
        assertTrue(task.matches("#42") && task.matches("csv") && !task.matches("#4"))
        assertEquals(TaskType.BUG, task.type)
        assertEquals(Instant.parse("2026-11-20T03:00:00Z"), task.updated?.toInstant())
        assertFalse(task.detailLoaded)
        assertNull(task.description)
        assertEquals(0, fake.calls.count { "/comments" in it })
    }

    @Test fun emptyBodyAndPartialCommentFailureI2() = runBlocking {
        val fake = Replay()
        val gateway = GitHubGateway(fake)
        val session = gateway.connect(fake.repo, "primary")
        val task = gateway.detail(session, 43)
        assertTrue(task.detailLoaded)
        assertEquals("", task.description)
        assertNull(task.authorLogin)
        assertNull(task.created)
        val first = gateway.comments(session, 43)
        fake.commentsPageFailure = true
        assertThrows(GitHubFailure::class.java) { runBlocking { gateway.comments(session, 43, first.nextPage!!) } }
        fake.commentsPageFailure = false
        val second = gateway.comments(session, 43, first.nextPage!!)
        val merged = mergeComments(first.items, second.items)
        assertEquals(listOf(899L,900L), merged.map { it.remoteId })
        assertEquals("raw *Markdown*  ", merged.first().text)
        assertEquals(2, task.withComments(merged).comments.size)
        assertEquals(1, fake.calls.count { it.endsWith("/issues/43?") })
    }

    @Test fun reverifyBeforePostAndNoAutomaticRetryC2() = runBlocking {
        val fake = Replay()
        val gateway = GitHubGateway(fake)
        val session = gateway.connect(fake.repo, "primary")
        fake.users["primary"] = 123
        assertThrows(GitHubFailure::class.java) { runBlocking { gateway.postComment(session, 42, "确认  ") } }
        assertEquals(0, fake.calls.count { " POST " in it })
        fake.users["primary"] = 11
        fake.uncertainPost = true
        val failure = assertThrows(GitHubFailure::class.java) { runBlocking { gateway.postComment(session, 42, "确认  ") } }
        assertTrue(failure.outcomeUnknown)
        assertEquals(1, fake.calls.count { " POST " in it })
    }
}
