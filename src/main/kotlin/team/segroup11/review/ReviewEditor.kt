package team.segroup11.review

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import team.segroup11.review.github.*
import team.segroup11.review.model.CheckRow
import team.segroup11.review.model.ModelConfigurable
import team.segroup11.review.state.*
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import javax.swing.*

/** Swing owns selection, caret and preview. The project service owns drafts, reports and requests. */
internal class ReviewEditor(
    project: Project, parent: Disposable, private val gateway: GitHubGateway,
    restore: (ViewPointer?) -> Unit, private val receipt: (ReviewComment) -> Unit,
    private val reconcile: (PendingSend) -> Unit
) {
    private val store = project.service<ReviewStore>()
    private val editor = textArea(3)
    private val targetLabel = JLabel("回复目标")
    private val previewLabel = JLabel(" ")
    private val stateLabel = JLabel(" ")
    private val reportRows = DefaultListModel<CheckRow>()
    private val rows = JList(reportRows)
    private val reason = textArea(5).apply { isEditable = false }
    private val version = JLabel(" ")
    private val use = button("用于回复") {
        val row = rows.selectedValue ?: return@button
        val suggestion = row.suggestion ?: return@button
        preview = suggestion
        previewLabel.text = "Tab 采纳 · $suggestion"
        editor.requestFocusInWindow()
    }
    val settings = button("模型设置") { ShowSettingsUtil.getInstance().showSettingsDialog(project, ModelConfigurable::class.java) }
    private var clickNanos = 0L
    private var timedKey: String? = null
    private val log = Logger.getInstance(ReviewEditor::class.java)
    val analyze: JButton = object : JButton("分析") {
        override fun paintComponent(g: java.awt.Graphics) {
            super.paintComponent(g)
            if (clickNanos != 0L && timedKey == pointer?.key && text == "分析中…" && isShowing) {
                log.info("IssueReview first paint elapsed_ms=" + (System.nanoTime() - clickNanos) / 1_000_000)
                clickNanos = 0L
            }
        }
    }
    private val send = button("发送评论") {
        val p = pointer ?: return@button
        val capturedSession = session ?: return@button
        store.send(p) { body -> gateway.postComment(capturedSession, p.number, body) }
        update()
    }
    private val check = button("核对原 Issue 的评论") {
        val p = pointer ?: return@button
        store.snapshot(p).unknown?.let(reconcile)
    }
    val footer = column(targetLabel, previewLabel, JBScrollPane(editor), line(send, check, stateLabel))
    private val reportSplit = JSplitPane(JSplitPane.VERTICAL_SPLIT, JBScrollPane(rows),
        JPanel(BorderLayout()).apply { add(JBScrollPane(reason)); add(line(use), BorderLayout.SOUTH) }).apply { resizeWeight = 0.55 }
    val report = JPanel(BorderLayout()).apply { add(version, BorderLayout.NORTH); add(reportSplit) }

    private var pointer: ViewPointer? = null
    private var session: GitHubSession? = null
    private var currentDetail: IssueReviewTask? = null
    private var detailHash: String? = null
    private var reportId: String? = null
    private var preview: String? = null
    private var shownReceipt: Long? = null
    private var binding = false

    init {
        analyze.addActionListener {
            val p = pointer ?: return@addActionListener
            val task = currentDetail ?: return@addActionListener
            val clicked = System.nanoTime()
            if (store.analyze(p, task.summary, task.description)) {
                timedKey = p.key
                clickNanos = clicked
            }
            update()
        }
        listOf(targetLabel, version, stateLabel, previewLabel).forEach { it.putClientProperty("html.disable", true) }
        rows.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(l: JList<*>, v: Any?, i: Int, s: Boolean, f: Boolean): java.awt.Component {
                super.getListCellRendererComponent(l, v, i, s, f)
                if (v is CheckRow) text = "${v.label}  ·  ${v.statusLabel}"
                return this
            }
        }
        rows.addListSelectionListener {
            val row = rows.selectedValue
            use.isEnabled = row?.suggestion != null
            reason.text = if (row == null) "" else buildString {
                append(row.reason)
                row.evidence?.let { append("\n\n").append(if (it.source == "body") "正文" else "标题").append("原文\n").append(it.quote) }
                row.suggestion?.let { append("\n\n建议\n").append(it) }
            }
            reason.caretPosition = 0
        }
        editor.document.addDocumentListener(textListener {
            if (!binding) pointer?.let { p ->
                store.edit(p, editor.text)
                send.isEnabled = canSend(p)
            }
        })
        installSuggestionTab(editor) {
            val p = pointer
            if (p != null && reportId == store.snapshot(p).report?.requestId) preview.also { clearPreview() }
            else null
        }
        store.subscribe(parent, { key -> if (key == pointer?.key) update() }, restore)
        bind(null, null)
    }

    fun bind(p: ViewPointer?, s: GitHubSession?) {
        pointer = p
        session = s
        currentDetail = null
        detailHash = null
        reportId = null
        shownReceipt = null
        clearPreview()
        reportRows.clear()
        reason.text = ""
        binding = true
        editor.text = p?.let { store.snapshot(it).draftText }.orEmpty()
        binding = false
        targetLabel.text = if (p != null && s != null) "回复至 ${s.repo.owner}/${s.repo.repo} #${p.number}" else "请选择 Issue"
        editor.isEnabled = p != null
        update()
    }

    fun loaded(task: IssueReviewTask, hash: String) { currentDetail = task; detailHash = hash; update() }

    private fun clearPreview() { preview = null; previewLabel.text = " " }
    private fun canSend(p: ViewPointer): Boolean {
        val snap = store.snapshot(p)
        return currentDetail?.numberValue == p.number && !snap.sending && snap.unknown == null && editor.text.isNotBlank()
    }

    private fun update() {
        val p = pointer
        val snap = p?.let(store::snapshot)
        val stored = snap?.report
        if (snap != null && editor.text != snap.draftText) {
            binding = true
            editor.text = snap.draftText
            binding = false
        }
        if (reportId != stored?.requestId) {
            reportId = stored?.requestId
            clearPreview()
            reportRows.clear()
            stored?.report?.rows?.sortedBy { it.status == "not_applicable" }?.forEach(reportRows::addElement)
            if (!reportRows.isEmpty) rows.selectedIndex = 0
        }
        analyze.isEnabled = currentDetail != null && snap?.running == false
        analyze.text = if (snap?.running == true) "分析中…" else "分析"
        send.isEnabled = p != null && canSend(p)
        check.isVisible = snap?.unknown != null
        version.text = when {
            stored == null -> "尚无评估报告"
            detailHash != null && detailHash != stored.inputHash -> "基于较早版本 · ${stored.modelId}"
            else -> "${stored.report.kind} · ${stored.modelId}"
        }
        stateLabel.text = listOfNotNull(snap?.warning, snap?.analysisError,
            if (snap?.sending == true) "正在发送…" else snap?.sendNotice,
            if (snap?.unknown != null) "发布结果待确认" else null).joinToString(" · ").ifEmpty { " " }
        snap?.receipt?.let { if (it.remoteId != shownReceipt) { shownReceipt = it.remoteId; receipt(it) } }
    }
}

internal fun installSuggestionTab(editor: JTextArea, takePreview: () -> String?) {
    editor.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet())
    val tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)
    editor.getInputMap(JComponent.WHEN_FOCUSED).put(tab, "acceptSuggestion")
    editor.actionMap.put("acceptSuggestion", object : AbstractAction() {
        override fun actionPerformed(e: java.awt.event.ActionEvent?) {
            val text = takePreview()
            if (text == null) {
                editor.transferFocus()
                return
            }
            val end = editor.selectionEnd
            editor.caretPosition = end
            editor.insert(text, end)
            editor.caretPosition = end + text.length
        }
    })
}
