package team.segroup11.review.github

import com.intellij.icons.AllIcons
import com.intellij.tasks.Comment
import com.intellij.tasks.Task
import com.intellij.tasks.TaskType
import java.net.URI
import java.time.Instant
import java.util.Date
import java.util.Locale

private val segment = Regex("[A-Za-z0-9_.-]+")

data class RepoKey(val server: String, val owner: String, val repo: String) {
    init {
        require(listOf(owner, repo).all { segment.matches(it) && it != "." && it != ".." })
    }
    val canonical = "${server.lowercase(Locale.ROOT)}/${owner.lowercase(Locale.ROOT)}/${repo.lowercase(Locale.ROOT)}"
    override fun equals(other: Any?) = other is RepoKey && canonical == other.canonical
    override fun hashCode() = canonical.hashCode()
}

data class RepoChoice(val key: RepoKey, val label: String) {
    override fun toString() = label
}
data class AccountChoice(val id: String, val login: String, val server: String) {
    override fun toString() = "$login ($server)"
}
data class Discovery(val repositories: List<RepoChoice>, val accounts: List<AccountChoice>,
                     val defaultAccountId: String?, val hasGitRoots: Boolean)
data class Page<T>(val items: List<T>, val nextPage: Int?)
enum class IssueFilter { ALL, OPEN, CLOSED }

/** Match the configured server exactly, including a non-default web port and enterprise path. */
internal fun repositoryOf(url: String, web: URI): RepoKey? {
    val uri = try {
        if ("://" in url) URI(url)
        else {
            val match = Regex("(?:[^@/:]+@)?([A-Za-z0-9.-]+):([^\\\\]+)").matchEntire(url) ?: return null
            URI("ssh", null, match.groupValues[1], -1, "/" + match.groupValues[2], null, null)
        }
    } catch (_: Exception) { return null }
    if (uri.host == null || !uri.host.equals(web.host, true) || uri.query != null || uri.fragment != null) return null
    val ssh = uri.scheme.equals("ssh", true)
    if (!ssh && !uri.scheme.equals(web.scheme, true)) return null
    fun webPort(address: URI) = if (address.port > 0) address.port else if (address.scheme == "https") 443 else 80
    if (!ssh && webPort(uri) != webPort(web)) return null
    if (ssh && uri.port !in listOf(-1, 22)) return null
    val root = web.rawPath.orEmpty().trimEnd('/')
    if (!uri.rawPath.startsWith("$root/")) return null
    val parts = uri.rawPath.substring(root.length + 1).trimEnd('/').removeSuffix(".git").split('/')
    if (parts.size != 2 || !parts.all { segment.matches(it) && it != "." && it != ".." }) return null
    return RepoKey(serverKey(web), parts[0], parts[1])
}

internal fun serverKey(web: URI): String {
    val port = web.port.takeIf { it > 0 && !(web.scheme == "https" && it == 443) && !(web.scheme == "http" && it == 80) }
    return web.host.lowercase(Locale.ROOT) + (port?.let { ":$it" } ?: "") + web.rawPath.orEmpty().trimEnd('/')
}

data class ReviewComment(val remoteId: Long, val login: String?, val createdAt: Instant?, val body: String) : Comment() {
    init { require(remoteId > 0) }
    override fun getText() = body
    override fun getAuthor() = login ?: "未知用户"
    override fun getDate(): Date? = createdAt?.let(Date::from)
}

fun mergeComments(previous: List<ReviewComment>, page: List<ReviewComment>): List<ReviewComment> =
    (previous + page).associateBy { it.remoteId }.values.sortedWith(compareBy({ it.createdAt }, { it.remoteId }))

class IssueReviewTask internal constructor(
    val repoKey: RepoKey, val numberValue: Long, private val title: String,
    private val closed: Boolean, private val rawBody: String?, val detailLoaded: Boolean,
    val authorLogin: String?, private val createdAt: Instant?, private val updatedAt: Instant?,
    private val url: String?, private val taskType: TaskType,
    private val loadedComments: List<ReviewComment> = emptyList()
) : Task() {
    init { require(numberValue > 0) }
    override fun getId() = "${repoKey.canonical}#$numberValue"
    override fun getPresentableId() = "${repoKey.owner}/${repoKey.repo} #$numberValue"
    override fun getNumber() = numberValue.toString()
    override fun getSummary() = title
    override fun getDescription() = rawBody
    override fun getComments(): Array<Comment> = loadedComments.toTypedArray()
    override fun getIcon() = AllIcons.Nodes.Tag
    override fun getType() = taskType
    override fun getUpdated(): Date? = updatedAt?.let(Date::from)
    override fun getCreated(): Date? = createdAt?.let(Date::from)
    override fun isClosed() = closed
    override fun isIssue() = true
    override fun getIssueUrl() = url
    fun withComments(comments: List<ReviewComment>) = IssueReviewTask(
        repoKey, numberValue, title, closed, rawBody, detailLoaded, authorLogin, createdAt,
        updatedAt, url, taskType, mergeComments(loadedComments, comments)
    )
    fun matches(query: String): Boolean {
        val input = query.trim()
        val digits = input.removePrefix("#")
        return if (digits.isNotEmpty() && digits.all { it in '0'..'9' }) digits.toLongOrNull() == numberValue
        else title.contains(input, ignoreCase = true)
    }
}
