package team.segroup11.review.state

import org.jdom.Element
import org.jdom.input.SAXBuilder
import org.jdom.output.XMLOutputter
import org.junit.Assert.*
import org.junit.Test
import team.segroup11.review.github.ReviewComment
import team.segroup11.review.model.*
import java.io.StringReader
import java.time.Instant

class ReviewStateTest {
    private val a = ViewPointer("user:1", "github.com/group/exporter", 42, 1)
    private val b = a.copy(number = 43, tab = 0)
    private val comment = ReviewComment(7, "fixture-user", Instant.EPOCH, "旧稿\n ")
    private val result: ModelResult
        get() {
            val text = csvReport()
            return ModelResult(ReportCodec.parse(text, fixtureTitle, fixtureBody), text,
                ReportCodec.inputHash(fixtureTitle, fixtureBody), "http://127.0.0.1/v1", "fixture")
        }

    private fun save(data: ReviewData, archive: ReviewArchive = ReviewArchive()): Element =
        archive.write(data.lastView, data.tasks, data.records, data.pending, data.futureVersion)

    private fun reload(xml: Element): ReviewData {
        val serialized = XMLOutputter().outputString(xml)
        return ReviewArchive(SAXBuilder().build(StringReader(serialized)).rootElement).load()
    }

    @Test fun nativeContextClearPreservesOnlyTheExplicitIncomingTaskOnce() {
        val data = ReviewData()
        data.select(a)
        data.edit(a, "最后一字  ")
        data.beginTaskActivation("task-A", a, "Default")
        assertEquals(a, data.tasks["task-A"])
        assertTrue(data.consumeActivationClear("task-A"))
        assertEquals(a, data.lastView)
        assertEquals("最后一字  ", data.snapshot(a).draftText)
        assertFalse(data.consumeActivationClear("task-A"))
        data.beginTaskActivation("task-A", a, "task-A")
        assertFalse(data.consumeActivationClear("task-A"))
        data.beginTaskActivation("task-B", b, "task-A")
        assertFalse(data.consumeActivationClear("unrelated"))
        assertFalse(data.consumeActivationClear("task-B"))
    }

    @Test fun roundTripPreservesEveryDraftAndOnlyStoresContextLocators() {
        val data = ReviewData()
        val otherRepo = a.copy(repoCanonical = "github.com/other/exporter")
        val otherUser = a.copy(accountKey = "user:2")
        val drafts = mapOf(a to "开头\n\n行尾  \n最后一字", b to " \r\nX\t", otherRepo to "库二", otherUser to "账号二")
        drafts.forEach { (key, draft) -> data.edit(key, draft) }
        data.select(a)
        val older = a.toElement("view")
        assertEquals(setOf("account", "repo", "number", "tab"), older.attributes.map { it.name }.toSet())
        data.tasks["issue-A"] = a
        data.select(b)
        data.edit(a, drafts.getValue(a) + "新")
        val after = reload(save(data))
        assertEquals(b, after.lastView)
        assertEquals(a, after.tasks["issue-A"])
        after.select(pointerFrom(older))
        assertEquals(drafts.getValue(a) + "新", after.snapshot(a).draftText)
        drafts.filterKeys { it != a }.forEach { (key, text) -> assertEquals(text, after.snapshot(key).draftText) }
        after.edit(a, "")
        assertEquals("", reload(save(after)).snapshot(a).draftText)
        assertTrue(after.pending.isEmpty())
        assertFalse(after.snapshot(a).running)
    }

    @Test fun doubleClickAndLateCompletionNeverOverwriteAnotherRecordOrNewerDraft() {
        val data = ReviewData()
        val first = data.startAnalysis(a)!!
        assertNull(data.startAnalysis(a))
        assertTrue(data.snapshot(a).running)
        data.select(b)
        data.edit(a, "在途修改")
        data.edit(b, "B 的草稿")
        data.finishAnalysis(a, first, result, null, false)
        val initial = data.snapshot(a).report!!
        assertEquals(b, data.lastView)
        assertNull(data.snapshot(b).report)
        assertEquals("B 的草稿", data.snapshot(b).draftText)
        assertEquals("在途修改", data.snapshot(a).draftText)
        val second = data.startAnalysis(a)!!
        data.finishAnalysis(a, first, null, "迟到", false)
        assertTrue(data.snapshot(a).running)
        data.finishAnalysis(a, second, null, "超时", false)
        assertEquals("超时", data.snapshot(a).analysisError)
        assertEquals(initial, data.snapshot(a).report)
        val persisted = reload(save(data)).snapshot(a)
        assertEquals(initial, persisted.report)
        assertEquals(ReportCodec.inputHash(fixtureTitle, fixtureBody), persisted.report!!.inputHash)
        assertNotEquals(ReportCodec.inputHash(fixtureTitle, fixtureBody + "修订"), persisted.report.inputHash)
        assertEquals("在途修改", persisted.draftText)
    }

    @Test fun explicitSendCapturesOriginalTargetTextAndRevisionUntilReceipt() {
        val data = ReviewData()
        data.edit(a, comment.body)
        val attempt = data.startSend(a)!!
        assertEquals(comment.body, attempt.sentText)
        assertNull(data.startSend(a))
        data.edit(a, "发送后修改")
        data.select(b)
        data.sent(a, attempt.attemptId, comment)
        assertEquals("发送后修改", data.snapshot(a).draftText)
        assertEquals(comment.remoteId, data.snapshot(a).receipt?.remoteId)
        assertNull(data.snapshot(b).receipt)
        val rejected = data.startSend(a)!!
        data.sendFailed(a, rejected.attemptId, false, "403")
        assertEquals("发送后修改", data.snapshot(a).draftText)
        val retry = data.startSend(a)!!
        data.sent(a, retry.attemptId, comment)
        assertEquals("", data.snapshot(a).draftText)
        assertNull(data.startSend(a))
    }

    @Test fun lostReceiptAndInterruptedSendRequireFreshGetAndHumanDecision() {
        val data = ReviewData()
        data.edit(a, comment.body)
        val attempt = data.startSend(a)!!
        // A restart during Sending is indistinguishable from a lost response.
        var restored = reload(save(data))
        assertEquals(attempt, restored.snapshot(a).unknown)
        assertFalse(restored.snapshot(a).sending)
        assertNull(restored.startSend(a))
        restored.reconcile(a, attempt.attemptId, comment)
        assertNotNull(restored.snapshot(a).unknown)
        restored.checked(b, attempt.attemptId)
        assertFalse(restored.snapshot(a).canResolve)
        restored.checked(a, attempt.attemptId)
        restored.edit(a, "确认时另有新输入")
        restored.reconcile(a, attempt.attemptId, comment)
        assertEquals("确认时另有新输入", restored.snapshot(a).draftText)
        assertNull(restored.snapshot(a).unknown)
        val another = restored.startSend(a)!!
        restored.sendFailed(a, another.attemptId, true, "响应丢失")
        restored = reload(save(restored))
        restored.checked(a, another.attemptId)
        restored.reconcile(a, another.attemptId, null)
        assertNull(restored.snapshot(a).unknown)
        assertFalse(restored.snapshot(a).sending)
        assertEquals("确认时另有新输入", restored.startSend(a)!!.sentText)
    }

    @Test fun damagedIndividualReportAndUnknownStorageVersionPreserveOriginalData() {
        val data = ReviewData()
        data.edit(a, "草稿 A")
        data.edit(b, "草稿 B")
        data.finishAnalysis(a, data.startAnalysis(a)!!, result, null, false)
        val damaged = save(data)
        damaged.getChildren("record").single { it.getAttributeValue("key") == a.key }.getChild("report").text = "??"
        val archive = ReviewArchive(damaged)
        val recovered = archive.load()
        assertEquals("草稿 A", recovered.snapshot(a).draftText)
        assertNull(recovered.snapshot(a).report)
        assertNotNull(recovered.snapshot(a).warning)
        recovered.edit(b, "修改 B")
        assertEquals("修改 B", reload(save(recovered, archive)).snapshot(b).draftText)
        assertEquals("??", save(recovered, archive).getChildren("record").single { it.getAttributeValue("key") == a.key }
            .getChild("report").text)
        damaged.setAttribute("version", "999")
        val future = ReviewArchive(damaged)
        val readOnly = future.load()
        readOnly.edit(a, "覆盖")
        assertNull(readOnly.startAnalysis(a))
        assertNull(readOnly.startSend(a))
        assertEquals(XMLOutputter().outputString(damaged), XMLOutputter().outputString(save(readOnly, future)))
    }
}
