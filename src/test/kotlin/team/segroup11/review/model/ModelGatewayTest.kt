package team.segroup11.review.model

import com.fasterxml.jackson.databind.JsonNode
import com.sun.net.httpserver.HttpServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

class ModelGatewayTest {
    private lateinit var server: HttpServer
    private lateinit var url: String
    private val requests = CopyOnWriteArrayList<JsonNode>()
    @Volatile private var responseStatus = 200
    @Volatile private var content = ""

    @Before fun start() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        url = "http://127.0.0.1:${server.address.port}/v1"
        server.createContext("/v1/chat/completions") { exchange ->
            try {
                val received = exchange.requestBody.use { ReportCodec.mapper.readTree(it) }
                requests.add(received)
                val bytes = content.toByteArray()
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(responseStatus, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            } finally {
                exchange.close()
            }
        }
        server.start()
    }

    @After fun stop() { if (::server.isInitialized) server.stop(0) }

    private fun response(finish: String = "stop", message: Map<String, Any?> = mapOf("content" to csvReport())) =
        ReportCodec.mapper.writeValueAsString(mapOf(
            "id" to "mock-1", "object" to "chat.completion", "created" to 0, "model" to "fixture",
            "choices" to listOf(mapOf("index" to 0, "finish_reason" to finish,
                "message" to (message + ("role" to "assistant"))))
        ))

    private fun profile(mode: OutputMode = OutputMode.JSON_OBJECT, window: Int = 32768) =
        ModelProfile(ModelOptions(url, "fixture", mode, window), "local-fixture-only")

    @Test fun sdkSendsOnlyTheTwoFieldsAndExplicitResponseMode() {
        content = response()
        for (mode in OutputMode.values()) {
            val result = ModelGateway().evaluate(fixtureTitle, fixtureBody, profile(mode))
            assertEquals("feature", result.report.kind)
            assertEquals(ReportCodec.inputHash(fixtureTitle, fixtureBody), result.inputHash)
            assertEquals("fixture", result.modelId)
            val request = requests.last()
            val messages = request.get("messages")
            assertEquals(2, messages.size())
            assertEquals("system", messages[0].get("role").textValue())
            assertFalse(messages[0].get("content").textValue().contains(fixtureBody))
            assertEquals("user", messages[1].get("role").textValue())
            val user = ReportCodec.mapper.readTree(messages[1].get("content").textValue())
            assertEquals(setOf("title", "body"), user.fieldNames().asSequence().toSet())
            assertEquals(fixtureTitle, user["title"].textValue())
            assertEquals(fixtureBody, user["body"].textValue())
            assertEquals("fixture", request["model"].textValue())
            assertEquals(1, request["n"].intValue())
            assertNull(request.get("tools"))
            assertFalse(request.path("stream").asBoolean(false))
            val format = request.get("response_format")
            if (mode == OutputMode.STRICT_SCHEMA) {
                assertEquals("json_schema", format["type"].textValue())
                assertTrue(format["json_schema"]["strict"].booleanValue())
                assertEquals("issue_review_v1", format["json_schema"]["name"].textValue())
                assertEquals(ReportCodec.mapper.readTree(ModelProtocol.SCHEMA), format["json_schema"]["schema"])
            } else assertEquals("json_object", format["type"].textValue())
        }
        assertEquals(2, requests.size)
    }

    @Test fun malformedCompletionAndTransportFailuresIssueExactlyOneRequestEach() {
        val cases = listOf(
            200 to response("length"),
            200 to response(message = mapOf("content" to null, "refusal" to "fixture refusal")),
            200 to response(message = mapOf("content" to "{}")),
            200 to response(message = mapOf("content" to csvReport(), "tool_calls" to listOf(
                mapOf("id" to "1", "type" to "function", "function" to mapOf("name" to "extra", "arguments" to "{}"))
            ))),
            200 to "{\"choices\":[]}",
            401 to """{"error":{"message":"fixture diagnostic"}}""",
            429 to """{"error":{"message":"fixture diagnostic"}}""",
            500 to """{"error":{"message":"fixture diagnostic"}}"""
        )
        cases.forEachIndexed { index, (status, envelope) ->
            responseStatus = status
            content = envelope
            val failure = assertThrows(ModelFailure::class.java) {
                ModelGateway().evaluate(fixtureTitle, fixtureBody, profile())
            }
            assertFalse(failure.message.orEmpty().contains("fixture diagnostic"))
            assertEquals(index + 1, requests.size)
        }
    }

    @Test fun insufficientCapacitySendsNoRequest() {
        assertThrows(ModelConfigurationException::class.java) {
            ModelGateway().evaluate(fixtureTitle, fixtureBody, profile(window = 4100))
        }
        assertTrue(requests.isEmpty())
    }
}
