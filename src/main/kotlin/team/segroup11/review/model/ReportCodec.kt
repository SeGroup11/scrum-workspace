package team.segroup11.review.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.HexFormat

open class ModelFailure(message: String) : RuntimeException(message)
class ModelConfigurationException(message: String) : ModelFailure(message)

data class Evidence(val source: String, val quote: String)
data class CheckRow(
    val id: String, val label: String, val status: String, val statusLabel: String,
    val reason: String, val evidence: Evidence?, val suggestion: String?
)
data class ReviewReport(val kind: String, val kindReason: String, val rows: List<CheckRow>)

object ReportCodec {
    internal val mapper = ObjectMapper()
        .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
    private val checks = linkedMapOf(
        "actual_behavior" to "实际现象", "reproduction_steps" to "复现步骤", "expected_result" to "预期结果",
        "goal" to "目标", "acceptance_criteria" to "验收标准", "edge_cases" to "边界情况"
    )
    private val statuses = mapOf("clear" to "清楚", "missing" to "缺失", "unclear" to "表述不清", "not_applicable" to "不适用")
    private val bugChecks = setOf("actual_behavior", "reproduction_steps", "expected_result")
    private val kinds = setOf("bug", "feature", "mixed", "undetermined")

    internal fun normalize(value: String?) = value.orEmpty().replace("\r\n", "\n").replace('\r', '\n')

    fun inputHash(title: String, body: String?): String {
        val digest = MessageDigest.getInstance("SHA-256")
        for (field in listOf(title, body)) {
            val bytes = normalize(field).toByteArray(Charsets.UTF_8)
            digest.update(ByteBuffer.allocate(4).putInt(bytes.size).array())
            digest.update(bytes)
        }
        return HexFormat.of().formatHex(digest.digest())
    }

    fun parse(reportJson: String, title: String, body: String?): ReviewReport =
        decode(reportJson, normalize(title), normalize(body))

    /** A local report has already passed original-text validation at analysis time. */
    internal fun restore(reportJson: String): ReviewReport = decode(reportJson, null, null)

    internal fun store(report: ReviewReport): String = mapper.writeValueAsString(
        mapOf("schemaVersion" to ModelProtocol.VERSION, "kind" to report.kind, "kindReason" to report.kindReason,
            "checks" to report.rows.associate { row ->
                row.id to mapOf("status" to row.status, "reason" to row.reason, "evidence" to row.evidence,
                    "suggestion" to row.suggestion)
            })
    )

    private fun decode(reportJson: String, normalizedTitle: String?, normalizedBody: String?): ReviewReport {
        if (reportJson.length > 65536 || reportJson.toByteArray(Charsets.UTF_8).size > 65536) {
            throw ModelFailure("模型报告超过 64 KiB。")
        }
        val root: JsonNode = try {
            mapper.readTree(reportJson) ?: throw ModelFailure("模型未返回报告。")
        } catch (_: JsonProcessingException) {
            throw ModelFailure("模型报告不是单个合法的 JSON 对象，或包含重复字段。")
        }
        root.requireKeys(setOf("schemaVersion", "kind", "kindReason", "checks"))
        if (root.text("schemaVersion") != ModelProtocol.VERSION) throw ModelFailure("模型报告协议版本不受支持。")
        val kind = root.text("kind")
        if (kind !in kinds) throw ModelFailure("模型报告包含未知的 Issue 类型。")
        val reasons = root.text("kindReason")
        val items = root.get("checks")
        items.requireKeys(checks.keys)
        val rows = checks.map { (id, label) ->
            val item = items.get(id)
            item.requireKeys(setOf("status", "reason", "evidence", "suggestion"))
            val status = item.text("status")
            val statusLabel = statuses[status] ?: throw ModelFailure("模型报告包含未知的检查状态。")
            val inactive = kind == "bug" && id !in bugChecks || kind == "feature" && id in bugChecks
            if (inactive && status != "not_applicable") throw ModelFailure("模型报告的类型与「${label}」适用范围矛盾。")
            if (kind != "undetermined" && !inactive && id != "edge_cases" && status == "not_applicable") {
                throw ModelFailure("「${label}」适用于当前类型，不能标为不适用。")
            }
            val evidenceNode = item.get("evidence")
            val evidence = if (status == "clear" || status == "unclear") {
                evidenceNode.requireKeys(setOf("source", "quote"))
                val source = evidenceNode.text("source")
                val quote = evidenceNode.text("quote")
                val original = when (source) {
                    "title" -> normalizedTitle
                    "body" -> normalizedBody
                    else -> throw ModelFailure("模型报告的引文来源无效。")
                }
                if (original != null && quote !in original) throw ModelFailure("模型返回的引文在指定的标题或正文中找不到。")
                Evidence(source, quote)
            } else {
                if (!evidenceNode.isNull) throw ModelFailure("「${label}」当前状态不能附带引文。")
                null
            }
            val suggestion = if (status == "missing" || status == "unclear") {
                item.text("suggestion")
            } else {
                if (!item.get("suggestion").isNull) throw ModelFailure("「${label}」当前状态不能附带建议。")
                null
            }
            CheckRow(id, label, status, statusLabel, item.text("reason"), evidence, suggestion)
        }
        return ReviewReport(kind, reasons, rows)
    }

    private fun JsonNode.requireKeys(keys: Set<String>) {
        if (!isObject || fieldNames().asSequence().toSet() != keys) throw ModelFailure("模型报告缺少必需字段或含额外字段。")
    }

    private fun JsonNode.text(name: String): String =
        get(name)?.takeIf { it.isTextual }?.textValue()?.takeIf { it.isNotBlank() }
            ?: throw ModelFailure("模型报告的 ${name} 必须包含非空文字。")
}
