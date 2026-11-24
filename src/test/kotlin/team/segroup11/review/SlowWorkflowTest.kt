package team.segroup11.review

import com.sun.net.httpserver.HttpServer
import org.junit.Assert.*
import org.junit.Test
import team.segroup11.review.model.*
import team.segroup11.review.state.*
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.*

class SlowWorkflowTest {
    @Test fun tabPreservesSelectionAndConsumesExactlyOneSuggestion() {
        SwingUtilities.invokeAndWait {
            val draft = JTextArea("前文  保留\n后文")
            draft.select(2, 6)
            var preview: String? = "第一条"
            installSuggestionTab(draft) { preview.also { preview = null } }
            val tab = draft.actionMap.get("acceptSuggestion")
            assertEquals("前文  保留\n后文", draft.text)
            tab.actionPerformed(null)
            assertEquals("前文  保留第一条\n后文", draft.text)
            assertEquals(9, draft.caretPosition)
            tab.actionPerformed(null)
            assertEquals("前文  保留第一条\n后文", draft.text)
            preview = "、第二条"
            tab.actionPerformed(null)
            assertEquals("前文  保留第一条、第二条\n后文", draft.text)
        }
    }

    @Test fun slowSuccessAndErrorKeepEditsAndOnlyReplaceTheOriginalReportOnSuccess() {
        val a = ViewPointer("github.example:7", "github.example/group11/exporter", 42)
        val b = a.copy(number = 43)
        val data = ReviewData()
        val calls = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val response = ReportCodec.mapper.writeValueAsBytes(mapOf(
            "id" to "controlled", "object" to "chat.completion", "created" to 0, "model" to "controlled",
            "choices" to listOf(mapOf("index" to 0, "finish_reason" to "stop",
                "message" to mapOf("role" to "assistant", "content" to csvReport())))
        ))
        val received = listOf(CountDownLatch(1), CountDownLatch(1))
        server.createContext("/v1/chat/completions") { request ->
            request.use {
                request.requestBody.use { it.readAllBytes() }
                val n = calls.getAndIncrement()
                received[n].countDown()
                Thread.sleep(10_000)
                val body = if (n == 0) response else """{"error":{"message":"controlled"}}""".toByteArray()
                request.responseHeaders.set("Content-Type", "application/json")
                request.sendResponseHeaders(if (n == 0) 200 else 503, body.size.toLong())
                request.responseBody.use { it.write(body) }
            }
        }
        server.start()
        try {
            val profile = ModelProfile(ModelOptions("http://127.0.0.1:${server.address.port}/v1", "test"), "loopback")
            repeat(2) { round ->
                data.select(a)
                val requestId = data.startAnalysis(a)!!
                assertNull(data.startAnalysis(a))
                val old = data.snapshot(a).report
                val start = System.nanoTime()
                val reply = CompletableFuture.supplyAsync { runCatching { ModelGateway().evaluate(fixtureTitle, fixtureBody, profile) } }
                assertTrue(received[round].await(10, TimeUnit.SECONDS))
                SwingUtilities.invokeAndWait {
                    assertFalse(reply.isDone)
                    data.edit(a, "A\n最后一字  $round")
                    data.select(b)
                    data.edit(b, "B\n\n  $round")
                }
                val result = reply.get(25, TimeUnit.SECONDS)
                val elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
                assertTrue(elapsed >= 10_000)
                data.finishAnalysis(a, requestId, result.getOrNull(), result.exceptionOrNull()?.message, false)
                assertEquals(b, data.lastView)
                assertNull(data.snapshot(b).report)
                assertEquals("B\n\n  $round", data.snapshot(b).draftText)
                assertEquals("A\n最后一字  $round", data.snapshot(a).draftText)
                if (round == 0) assertNotNull(data.snapshot(a).report) else {
                    assertEquals(old, data.snapshot(a).report)
                    assertNotNull(data.snapshot(a).analysisError)
                }
                println("controlled HTTP round=${round + 1} calls=${calls.get()} elapsed_ms=$elapsed")
            }
            assertEquals(2, calls.get())
        } finally { server.stop(0) }
    }
}
