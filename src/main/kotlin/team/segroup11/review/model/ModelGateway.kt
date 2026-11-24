package team.segroup11.review.model

import com.fasterxml.jackson.core.type.TypeReference
import com.intellij.openapi.components.service
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.core.JsonValue
import com.openai.core.LogLevel
import com.openai.errors.OpenAIException
import com.openai.errors.OpenAIIoException
import com.openai.errors.OpenAIServiceException
import com.openai.models.ResponseFormatJsonObject
import com.openai.models.ResponseFormatJsonSchema
import com.openai.models.chat.completions.ChatCompletion
import com.openai.models.chat.completions.ChatCompletionCreateParams
import java.io.InterruptedIOException
import java.time.Duration
import java.util.concurrent.CancellationException

data class ModelResult(
    val report: ReviewReport, val reportJson: String, val inputHash: String,
    val profileId: String, val modelId: String
)

/** Call on a background thread. Each invocation takes its own settings snapshot and sends at most one request. */
class ModelGateway {
    fun evaluate(title: String, body: String?): ModelResult =
        evaluate(title, body, service<ModelSettings>().profile())

    internal fun evaluate(title: String, body: String?, profile: ModelProfile): ModelResult {
        val options = profile.options
        val input = ReportCodec.mapper.writeValueAsString(
            mapOf("title" to ReportCodec.normalize(title), "body" to ReportCodec.normalize(body))
        )
        validateBudget(options)
        // UTF-8 bytes upper-bound input tokens for byte-based tokenizers; allow room for message framing.
        // Strict mode also carries the schema as a separate structured-output parameter.
        val schemaCost = if (options.mode == OutputMode.STRICT_SCHEMA) ModelProtocol.SCHEMA.toByteArray().size else 0
        val total = ModelProtocol.systemMessage.toByteArray().size.toLong() + input.toByteArray().size +
            options.outputTokens + schemaCost + 128
        if (total > options.contextTokens) throw ModelConfigurationException("原文超出当前模型容量，请调整上下文窗口或选择其他模型。")

        val params = ChatCompletionCreateParams.builder()
            .model(options.modelId)
            .addSystemMessage(ModelProtocol.systemMessage)
            .addUserMessage(input)
            .maxCompletionTokens(options.outputTokens.toLong())
            .n(1)
        when (options.mode) {
            OutputMode.JSON_OBJECT -> params.responseFormat(ResponseFormatJsonObject.builder().build())
            OutputMode.STRICT_SCHEMA -> params.jsonSchemaResponseFormat(strictSchema)
        }
        val client = OpenAIOkHttpClient.builder()
            .baseUrl(endpoint(options.baseUrl))
            .apiKey(profile.apiKey)
            .followRedirects(false)
            .maxRetries(0)
            .timeout(Duration.ofSeconds(60))
            .logLevel(LogLevel.OFF)
            .build()
        try {
            val response = client.chat().completions().create(params.build())
            val choice = response.choices().singleOrNull() ?: throw ModelFailure("模型必须返回一个候选结果。")
            if (choice.finishReason() != ChatCompletion.Choice.FinishReason.STOP) throw ModelFailure("模型响应未正常结束，可能已截断。")
            val message = choice.message()
            if (message.refusal().isPresent) throw ModelFailure("模型拒绝了本次分析。")
            if (message.toolCalls().orElse(emptyList()).isNotEmpty() || message.functionCall().isPresent) {
                throw ModelFailure("模型返回了未请求的工具调用。")
            }
            val content = message.content().orElseThrow { ModelFailure("模型未返回报告内容。") }
            val report = ReportCodec.parse(content, title, body)
            return ModelResult(report, content, ReportCodec.inputHash(title, body), options.baseUrl, options.modelId)
        } catch (e: OpenAIServiceException) {
            when (e.statusCode()) {
                400, 404, 422 -> throw ModelConfigurationException("模型服务拒绝当前配置，请核对地址、模型 ID、输出模式与预算。")
                401, 403 -> throw ModelConfigurationException("模型服务认证或授权失败，请核对当前地址及其密钥。")
                429 -> throw ModelFailure("模型服务限流，请稍后重试。")
                else -> throw ModelFailure("模型服务返回 HTTP ${e.statusCode()}。")
            }
        } catch (e: OpenAIIoException) {
            if (Thread.currentThread().isInterrupted) throw CancellationException("本次分析已取消。")
            if (generateSequence<Throwable>(e) { it.cause }.any { it is InterruptedIOException }) {
                throw ModelFailure("模型请求超时，可手动重试。")
            }
            throw ModelFailure("模型服务连接失败。")
        } catch (_: OpenAIException) {
            throw ModelFailure("模型服务返回了无法读取的响应。")
        } finally {
            client.close()
        }
    }

    private companion object {
        val strictSchema by lazy {
            val doc = ReportCodec.mapper.readValue(ModelProtocol.SCHEMA, object : TypeReference<Map<String, Any?>>() {})
            val schema = ResponseFormatJsonSchema.JsonSchema.Schema.builder()
                .putAllAdditionalProperties(doc.mapValues { JsonValue.from(it.value) }).build()
            ResponseFormatJsonSchema.JsonSchema.builder().name("issue_review_v1").strict(true).schema(schema).build()
        }
    }
}
