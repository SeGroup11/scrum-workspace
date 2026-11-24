package team.segroup11.review.state

import team.segroup11.review.github.ReviewComment
import team.segroup11.review.model.ModelResult
import team.segroup11.review.model.ReviewReport
import java.util.UUID

data class ViewPointer(val accountKey: String, val repoCanonical: String, val number: Long, val tab: Int = 0) {
    init { require(accountKey.isNotBlank() && repoCanonical.isNotBlank() && number > 0 && tab in 0..1) }
    val key: String get() = "${accountKey.length}:$accountKey${repoCanonical.length}:$repoCanonical#$number"
}

data class StoredReport(
    val report: ReviewReport, val requestId: String, val inputHash: String, val profileId: String,
    val modelId: String, val completedAt: Long, val promptVersion: String = "1"
)
data class PendingSend(val attemptId: String, val sentText: String, val startedAt: Long, val draftRevision: Long)
data class ReviewSnapshot(
    val draftText: String = "", val draftRevision: Long = 0, val report: StoredReport? = null,
    val running: Boolean = false, val analysisError: String? = null, val configError: Boolean = false,
    val sending: Boolean = false, val unknown: PendingSend? = null, val sendNotice: String? = null,
    val receipt: ReviewComment? = null, val canResolve: Boolean = false, val warning: String? = null,
    val tab: Int = 0
)

/** The project service serializes edits and completions under one lock. No I/O is performed here. */
internal class ReviewData {
    val records = mutableMapOf<String, ReviewSnapshot>()
    val tasks = mutableMapOf<String, ViewPointer>()
    var lastView: ViewPointer? = null
    private val analyses = mutableMapOf<String, String>()
    val pending = mutableMapOf<String, PendingSend>()
    val lockedKeys = mutableSetOf<String>()
    var futureVersion = false

    fun snapshot(p: ViewPointer): ReviewSnapshot {
        val value = records[p.key] ?: ReviewSnapshot()
        return if (futureVersion) value.copy(warning = "工作区记录来自较新的插件版本，已保护原记录。") else value
    }

    fun select(p: ViewPointer?) {
        if (futureVersion) return
        lastView = p
        if (p != null && p.key !in lockedKeys) records[p.key] = snapshot(p).copy(tab = p.tab)
    }

    fun edit(p: ViewPointer, text: String) {
        if (futureVersion || p.key in lockedKeys) return
        val row = snapshot(p)
        if (row.draftText != text) records[p.key] = row.copy(draftText = text, draftRevision = row.draftRevision + 1)
    }

    fun startAnalysis(p: ViewPointer): String? {
        if (futureVersion || p.key in lockedKeys || p.key in analyses) return null
        val id = UUID.randomUUID().toString()
        analyses[p.key] = id
        records[p.key] = snapshot(p).copy(running = true, analysisError = null, configError = false)
        return id
    }

    fun finishAnalysis(p: ViewPointer, id: String, result: ModelResult?, error: String?, configError: Boolean) {
        if (!analyses.remove(p.key, id)) return
        val row = snapshot(p)
        val success = result?.let {
            StoredReport(it.report, id, it.inputHash, it.profileId, it.modelId, System.currentTimeMillis())
        }
        records[p.key] = row.copy(report = success ?: row.report, running = false, analysisError = error,
            configError = configError)
    }

    fun startSend(p: ViewPointer): PendingSend? {
        val row = snapshot(p)
        if (futureVersion || p.key in lockedKeys || p.key in pending || row.draftText.isBlank()) return null
        val attempt = PendingSend(UUID.randomUUID().toString(), row.draftText, System.currentTimeMillis(), row.draftRevision)
        pending[p.key] = attempt
        records[p.key] = row.copy(sending = true, unknown = null, sendNotice = null, canResolve = false)
        return attempt
    }

    fun sent(p: ViewPointer, attemptId: String, comment: ReviewComment) {
        val attempt = pending[p.key]?.takeIf { it.attemptId == attemptId } ?: return
        val row = snapshot(p)
        val clear = row.draftRevision == attempt.draftRevision
        pending.remove(p.key)
        records[p.key] = row.copy(draftText = if (clear) "" else row.draftText,
            draftRevision = if (clear) row.draftRevision + 1 else row.draftRevision,
            sending = false, unknown = null, sendNotice = "已发送评论 #${comment.remoteId}。", receipt = comment, canResolve = false)
    }

    fun sendFailed(p: ViewPointer, id: String, unknown: Boolean, error: String) {
        val attempt = pending[p.key]?.takeIf { it.attemptId == id } ?: return
        if (!unknown) pending.remove(p.key)
        records[p.key] = snapshot(p).copy(sending = false, unknown = if (unknown) attempt else null,
            canResolve = false, sendNotice = error)
    }

    fun checked(p: ViewPointer, id: String) {
        val row = snapshot(p)
        if (row.unknown?.attemptId == id) records[p.key] = row.copy(canResolve = true)
    }

    fun reconcile(p: ViewPointer, id: String, comment: ReviewComment?) {
        val row = snapshot(p)
        if (!row.canResolve || row.unknown?.attemptId != id) return
        if (comment != null) {
            sent(p, id, comment)
        } else {
            pending.remove(p.key)
            records[p.key] = row.copy(unknown = null, canResolve = false, sendNotice = "已核对。如需重发，请点击发送。")
        }
    }
}
