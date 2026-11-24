package team.segroup11.review.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.util.ThrowableConvertor
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.github.api.GithubApiRequest
import org.jetbrains.plugins.github.api.GithubApiResponse
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.exceptions.GithubStatusCodeException
import kotlin.test.*

import team.segroup11.review.state.ReviewArchive
import team.segroup11.review.state.ReviewData
import team.segroup11.review.state.ViewPointer
import java.awt.Component
import java.io.IOException
import java.io.InputStream
import java.io.Reader
import java.net.URI

/** Controlled server ledger. The real GitHub gateway creates the requests and decodes responses. */
class CommentRecoveryProbe {
    private class Ledger : ConnectionAccess, GitHubTransport {
        val repo = RepoKey("github.com", "group11", "recovery")
        val rows = mutableMapOf<Long, MutableList<Map<String, Any>>>()
        val calls = mutableListOf<String>()
        var fault = "reject"
        private val json = ObjectMapper()

        override fun discover() = Discovery(listOf(RepoChoice(repo, repo.canonical)), listOf(AccountChoice("test", "fixture", repo.server)), "test", true)
        override fun isAvailable(id: String, repo: RepoKey) = id == "test" && repo == this.repo
        override fun reauthenticate(id: String, repo: RepoKey, parent: Component) = false
        override suspend fun bind(id: String, repo: RepoKey) = BoundAccount(GithubServerPath.DEFAULT_SERVER, this)

        override suspend fun <T> execute(request: GithubApiRequest<T>): T {
            val path = URI(request.url).path
            val post = request is GithubApiRequest.Post
            calls += (if (post) "POST " else "GET ") + path
            val data: Any = when {
                path == "/user" -> mapOf("id" to 11)
                path == "/repos/group11/recovery" -> mapOf("name" to "recovery", "owner" to mapOf("login" to "group11"))
                path.matches(Regex("/repos/group11/recovery/issues/[0-9]+/comments")) -> {
                    val number = path.split('/')[5].toLong()
                    val comments = rows.getOrPut(number) { mutableListOf() }
                    if (request is GithubApiRequest.Post) {
                        if (fault == "reject") throw GithubStatusCodeException("fixture", 403)
                        val text = json.readTree(request.body).get("body").textValue()
                        val record = mapOf("id" to (7100 + comments.size), "body" to text,
                            "user" to mapOf("login" to "fixture"), "created_at" to "2026-11-24T00:00:00Z")
                        comments += record
                        if (fault == "after-commit") throw IOException("fixture disconnected after durable acceptance")
                        record
                    } else comments
                }
                else -> error("Unexpected request: " + path)
            }
            val raw = json.writeValueAsString(data)
            return request.extractResult(object : GithubApiResponse {
                override fun findHeader(headerName: String): String? = null
                override fun <R> readBody(reader: ThrowableConvertor<Reader, R, IOException>): R = reader.convert(raw.reader())
                override fun <R> handleBody(reader: ThrowableConvertor<InputStream, R, IOException>): R =
                    reader.convert(raw.byteInputStream())
            })
        }
    }

    fun rejectRetryAndLoseReceiptAfterRemoteCommit() = runBlocking {
        val server = Ledger()
        val gateway = GitHubGateway(server)
        val session = gateway.connect(server.repo, "test")
        val a = ViewPointer(session.accountKey, server.repo.canonical, 42, 0)
        val b = a.copy(number = 43)
        var state = ReviewData()

        suspend fun clickSend(): GitHubFailure? {
            val attempt = state.startSend(a) ?: return null
            assertNull(state.startSend(a))
            return try {
                state.sent(a, attempt.attemptId, gateway.postComment(session, a.number, attempt.sentText))
                null
            } catch (failure: GitHubFailure) {
                state.sendFailed(a, attempt.attemptId, failure.outcomeUnknown, failure.message.orEmpty())
                failure
            }
        }
        val original = "需要数据量与时限。  "
        state.edit(a, original)
        assertFalse(clickSend()!!.outcomeUnknown)
        assertEquals(original, state.snapshot(a).draftText)
        assertEquals(0, server.rows.getValue(42).size)
        server.fault = "none"
        assertNull(clickSend())
        assertEquals(listOf(original), server.rows.getValue(42).map { it["body"] })
        val lost = "第二次独立场景\n保留末尾空格  "
        state.edit(a, lost)
        server.fault = "after-commit"
        assertTrue(clickSend()!!.outcomeUnknown)
        assertEquals(listOf(original, lost), server.rows.getValue(42).map { it["body"] })
        val saved = ReviewArchive().write(state.lastView, state.tasks, state.records, state.pending, state.futureVersion)
        state = ReviewArchive(saved).load()
        val pending = state.snapshot(a).unknown!!
        state.edit(a, "新编辑  ")
        state.select(b)
        state.edit(b, "B 独立草稿")
        assertNull(state.startSend(a))
        val remote = gateway.comments(session, a.number, 1)
        val reread = gateway.comments(session, a.number, 1)
        val merged = mergeComments(remote.items, reread.items)
        assertEquals(2, merged.size)
        val match = merged.single { it.body == lost }
        state.reconcile(a, pending.attemptId, match)
        assertNotNull(state.snapshot(a).unknown)
        state.checked(a, pending.attemptId)
        state.reconcile(a, pending.attemptId, match)
        assertNull(state.snapshot(a).unknown)
        assertEquals(match.remoteId, state.snapshot(a).receipt!!.remoteId)
        assertEquals("新编辑  ", state.snapshot(a).draftText)
        assertEquals("B 独立草稿", state.snapshot(b).draftText)
        assertTrue(gateway.comments(session, b.number, 1).items.isEmpty())
        assertEquals(3, server.calls.count { it.startsWith("POST ") })
        assertEquals(2, server.calls.count { it == "GET /repos/group11/recovery/issues/42/comments" })
        assertEquals(0, server.rows.getValue(43).size)
        println("Definite 403 -> 0 persisted; explicit retry -> 1; second scenario accepted before disconnect -> total 2. Two GETs on #42 deduped to 2, #43 remained 0. Three explicit POST attempts; duplicate clicks and restart produced 0 POST. Original-target receipt " + match.remoteId + "; both newer drafts retained.")
        server.calls.forEach(::println)
    }
}


fun main() { CommentRecoveryProbe().rejectRetryAndLoseReceiptAfterRemoteCommit() }
