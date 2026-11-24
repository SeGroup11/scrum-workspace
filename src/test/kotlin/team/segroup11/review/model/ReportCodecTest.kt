package team.segroup11.review.model

import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.Assert.*
import org.junit.Test

internal val fixtureTitle = "导出搜索结果"
internal val fixtureBody = "希望将搜索结果导出为 CSV。导出要快。"
internal fun csvReport(): String = requireNotNull(ReportCodecTest::class.java.getResource("/model/csv-report.json")).readText()

class ReportCodecTest {
    @Test fun csvFeatureRetainsEvidenceAndActionableQuestionInRegistryOrder() {
        val report = ReportCodec.parse(csvReport(), fixtureTitle, fixtureBody)
        assertEquals("feature", report.kind)
        assertEquals(listOf("实际现象", "复现步骤", "预期结果", "目标", "验收标准", "边界情况"), report.rows.map { it.label })
        val row = report.rows[4]
        assertEquals("表述不清", row.statusLabel)
        assertEquals(Evidence("body", "导出要快"), row.evidence)
        assertTrue(row.suggestion!!.contains("数据量"))
        assertTrue(row.suggestion.contains("完成时间"))
        assertFalse(row.suggestion.any(Char::isDigit))
    }

    @Test fun otherKindsAndEmptyBody() {
        for (kind in listOf("bug", "mixed", "undetermined")) {
            val doc = ReportCodec.mapper.readTree(csvReport()) as ObjectNode
            doc.put("kind", kind)
            doc.put("kindReason", "测试夹具。")
            val checks = doc.get("checks") as ObjectNode
            for ((index, entry) in checks.fields().asSequence().withIndex()) {
                val inactive = kind == "bug" && index >= 3
                val row = entry.value as ObjectNode
                row.putNull("evidence")
                row.put("reason", if (inactive) "故障的判据由预期结果说明。" else "还需要描述。")
                row.put("status", if (inactive) "not_applicable" else "missing")
                if (inactive) row.putNull("suggestion") else row.put("suggestion", "能否说明这个细节？")
            }
            assertEquals(kind, ReportCodec.parse(doc.toString(), "问题", null).kind)
        }
    }

    @Test fun invalidReportsAreRejectedAsAWhole() {
        val original = csvReport()
        val negatives = listOf(
            original.replaceFirst("{", "{\"extra\":null,"),
            original.replaceFirst("{", "{\"schemaVersion\":\"1\","),
            original.replaceFirst("\"status\":\"not_applicable\"", "\"status\":\"missing\",\"status\":\"not_applicable\""),
            original.replace("\"goal\":", "\"gola\":"),
            original.replace("not_applicable", "N/A"),
            original.replace("\"source\":\"body\"", "\"source\":\"title\""),
            original.replace("\"quote\":\"导出要快\"", "\"quote\":\"导出须快\""),
            original.replace("\"当搜索结果为空时，您希望导出功能如何反馈？\"", "null"),
            original + "{}",
            "```json\n$original\n```",
            original.replace("\"kind\":\"feature\"", "\"kind\":\"bug\""),
            original.replace("\"schemaVersion\":\"1\"", "\"schemaVersion\":1"),
            original.replace("\"source\":\"body\"", "\"source\":\"comments\""),
            original.replace("\"suggestion\":null", "\"suggestion\":\"多余的问题\""),
            original.replace("\"quote\":\"导出要快\"", "\"quote\":\" \""),
            original.repeat(80),
            original.replace("提交者希望增加搜索结果导出功能。", " "),
            "",
            "null"
        )
        for ((index, input) in negatives.withIndex()) {
            assertThrows("negative fixture #$index", ModelFailure::class.java) {
                ReportCodec.parse(input, fixtureTitle, fixtureBody)
            }
        }
    }

    @Test fun hashUsesLengthPrefixedUtf8AndNormalizedLineEndings() {
        assertEquals(ReportCodec.inputHash("A", null), ReportCodec.inputHash("A", ""))
        assertEquals(ReportCodec.inputHash("中", "a\nb\n"), ReportCodec.inputHash("中", "a\r\nb\r"))
        assertNotEquals(ReportCodec.inputHash("ab", "c"), ReportCodec.inputHash("a", "bc"))
        assertNotEquals(ReportCodec.inputHash("A", "a"), ReportCodec.inputHash("A", "a "))
    }

    @Test fun credentialKeysCanonicalizeOnlyTheChosenEndpoint() {
        assertEquals("https://example.org/v1", endpoint(" HTTPS://EXAMPLE.ORG:443/v1/ "))
        assertEquals("http://[::1]:8111/v1", endpoint("http://[::1]:8111/v1"))
        assertNotEquals(endpoint("https://example.org/a"), endpoint("https://example.org/b"))
        for (url in listOf("http://example.org", "https://key@example.org", "https://example.org?api_key=x",
            "ftp://localhost", "https://example.org:99999", "/v1", "")) {
            assertThrows(ModelConfigurationException::class.java) { endpoint(url) }
        }
    }
}
