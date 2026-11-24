package team.segroup11.review.state

import org.jdom.Element
import team.segroup11.review.model.ReportCodec
import java.util.Base64

internal fun ViewPointer.toElement(name: String): Element = Element(name)
    .setAttribute("account", accountKey).setAttribute("repo", repoCanonical)
    .setAttribute("number", number.toString()).setAttribute("tab", tab.toString())

internal fun pointerFrom(element: Element?): ViewPointer? = element?.let {
    runCatching {
        ViewPointer(requireNotNull(it.getAttributeValue("account")), requireNotNull(it.getAttributeValue("repo")),
            it.getAttributeValue("number").toLong(), it.getAttributeValue("tab")?.toInt() ?: 0)
    }.getOrNull()
}

/** Store arbitrary editor text without XML newline/attribute normalization. */
private fun encoded(s: String): String = Base64.getEncoder().encodeToString(s.toByteArray(Charsets.UTF_8))
private fun decoded(s: String): String = Base64.getDecoder().decode(s).toString(Charsets.UTF_8)
private fun Element.required(key: String): String = requireNotNull(getAttributeValue(key))

internal class ReviewArchive(private val original: Element? = null) {
    private val preserved = mutableMapOf<String, Element>()
    private val brokenReports = mutableMapOf<String, Element>()
    private val orphans = mutableListOf<Element>()

    fun load(): ReviewData {
        val data = ReviewData()
        val root = original ?: return data
        val version = root.getAttributeValue("version") ?: "1"
        if (version != "1") data.futureVersion = true
        data.lastView = pointerFrom(root.getChild("view"))
        for (task in root.getChildren("task")) {
            val id = task.getAttributeValue("id") ?: continue
            pointerFrom(task)?.let { data.tasks[id] = it }
        }
        for (record in root.getChildren("record")) {
            val key = record.getAttributeValue("key")
            if (key == null) {
                orphans.add(record)
                continue
            }
            try {
                val draft = record.getChildText("draft")?.let(::decoded).orEmpty()
                val revision = record.getAttributeValue("revision")?.toLong() ?: 0L
                require(revision >= 0)
                var row = ReviewSnapshot(draftText = draft, draftRevision = revision,
                    tab = record.getAttributeValue("tab")?.toIntOrNull()?.takeIf { it in 0..1 } ?: 0)
                data.records[key] = row
                record.getChild("report")?.let { element ->
                    try {
                        val report = StoredReport(ReportCodec.restore(decoded(element.text)), element.required("request"),
                            element.required("hash"), element.required("profile"), element.required("model"),
                            element.required("at").toLong(), element.required("prompt"))
                        row = row.copy(report = report)
                    } catch (_: RuntimeException) {
                        brokenReports[key] = element
                        row = row.copy(warning = "已恢复草稿。本地报告损坏，可重新分析。")
                    }
                }
                record.getChild("pending")?.let { element ->
                    val attempt = PendingSend(element.required("id"), decoded(element.text),
                        element.required("at").toLong(), element.required("revision").toLong())
                    data.pending[key] = attempt
                    row = row.copy(unknown = attempt, sendNotice = "发布结果待确认，请先核对原 Issue 的评论。")
                }
                data.records[key] = row
            } catch (_: RuntimeException) {
                preserved[key] = record
                data.lockedKeys.add(key)
                data.records[key] = (data.records[key] ?: ReviewSnapshot()).copy(warning = "此记录部分损坏，已保护原数据。")
            }
        }
        return data
    }

    fun write(view: ViewPointer?, tasks: Map<String, ViewPointer>, records: Map<String, ReviewSnapshot>,
              pending: Map<String, PendingSend>, future: Boolean): Element {
        if (future && original != null) return original.clone()
        val root = Element("state").setAttribute("version", "1")
        view?.let { root.addContent(it.toElement("view")) }
        tasks.forEach { (id, pointer) -> root.addContent(pointer.toElement("task").setAttribute("id", id)) }
        orphans.forEach { root.addContent(it.clone()) }
        for ((key, value) in records) {
            val locked = preserved[key]
            if (locked != null) {
                root.addContent(locked.clone())
                continue
            }
            val record = Element("record").setAttribute("key", key)
                .setAttribute("revision", value.draftRevision.toString()).setAttribute("tab", value.tab.toString())
            record.addContent(Element("draft").setText(encoded(value.draftText)))
            val report = value.report
            if (report == null) {
                brokenReports[key]?.let { record.addContent(it.clone()) }
            } else {
                record.addContent(Element("report").setAttribute("request", report.requestId)
                    .setAttribute("hash", report.inputHash).setAttribute("profile", report.profileId)
                    .setAttribute("model", report.modelId).setAttribute("at", report.completedAt.toString())
                    .setAttribute("prompt", report.promptVersion).setText(encoded(ReportCodec.store(report.report))))
            }
            pending[key]?.let {
                record.addContent(Element("pending").setAttribute("id", it.attemptId)
                    .setAttribute("revision", it.draftRevision.toString()).setAttribute("at", it.startedAt.toString())
                    .setText(encoded(it.sentText)))
            }
            root.addContent(record)
        }
        return root
    }
}
