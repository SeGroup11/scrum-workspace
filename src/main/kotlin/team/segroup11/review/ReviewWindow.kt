package team.segroup11.review

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import kotlinx.coroutines.*
import team.segroup11.review.github.*
import java.awt.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

internal fun JTextField.onChange(action: () -> Unit) = document.addDocumentListener(textListener(action))
internal fun textListener(action: () -> Unit) = object : DocumentListener {
    override fun insertUpdate(e: DocumentEvent) = action()
    override fun removeUpdate(e: DocumentEvent) = action()
    override fun changedUpdate(e: DocumentEvent) = action()
}
internal fun line(vararg items: Component) = JPanel(FlowLayout(FlowLayout.LEADING, 4, 2)).apply {
    items.forEach { add(it) }
}
internal fun button(label: String, click: () -> Unit) = JButton(label).apply { addActionListener { click() } }
internal fun textArea(rows: Int = 0) = JTextArea(rows, 0).apply {
    lineWrap = true
    wrapStyleWord = true
}
internal fun column(vararg items: JComponent) = JPanel().apply {
    layout = BoxLayout(this, BoxLayout.Y_AXIS)
    items.forEach { it.alignmentX = Component.LEFT_ALIGNMENT; add(it) }
}

@Service(Service.Level.PROJECT)
internal class ReviewJobs(val scope: CoroutineScope)

class ReviewWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val pane = ReviewWindow(project)
        val content = ContentFactory.getInstance().createContent(pane.panel, "", false)
        content.setDisposer(pane)
        toolWindow.contentManager.addContent(content)
    }
}

internal class ReviewWindow(private val project: Project) : Disposable {
    private val scope = CoroutineScope(project.service<ReviewJobs>().scope.coroutineContext +
        SupervisorJob(project.service<ReviewJobs>().scope.coroutineContext[Job]))
    private val gateway = GitHubGateway(project)
    private val props = PropertiesComponent.getInstance(project)
    private val repoBox = JComboBox<RepoChoice>()
    private val accountBox = JComboBox<AccountChoice>()
    private val connection = JLabel("正在查找 Git 仓库与 IDE 账号")
    private val query = JTextField().apply { toolTipText = "按标题或完整编号搜索" }
    private val filter = JComboBox(IssueFilter.values())
    private val listModel = DefaultListModel<IssueReviewTask>()
    private val list = JBList(listModel)
    private val listStatus = JLabel("请选择连接")
    private val more = button("下一页") { loadPage() }
    private val issues = linkedMapOf<Long, IssueReviewTask>()
    private val title = JLabel("请选择 Issue")
    private val discussion = textArea().apply { isEditable = false }
    private val discussionStatus = JLabel(" ")
    private val moreComments = button("加载评论") { loadComments() }
    private val open = button("GitHub 原文") { detail?.issueUrl?.let { BrowserUtil.browse(it) } }
    private val retryDetail = button("刷新详情") { selectIssue(number) }
    private val tabs = JTabbedPane()
    private val reportPanel = JPanel(BorderLayout())
    private val footer = JPanel(BorderLayout())
    private val actions = line(open, retryDetail)
    private val panelRight = JPanel(BorderLayout(3, 3))
    val panel = JPanel(BorderLayout(4, 4))

    private var session: GitHubSession? = null
    private var number: Long? = null
    private var detail: IssueReviewTask? = null
    private val comments = linkedMapOf<Long, ReviewComment>()
    private var nextComments: Int? = null
    private var loadingComments = false
    private var nextPage: Int? = null
    private var paging = false
    private var epoch = 0L
    private var queryEpoch = 0L
    private var selection = 0L
    private var updating = false
    private var disposed = false
    private val searchTimer = Timer(220) { renderList(); if (query.text.isNotBlank()) loadPage() }.apply { isRepeats = false }

    init {
        panel.border = BorderFactory.createEmptyBorder(5, 6, 5, 6)
        panel.add(column(line(JLabel("仓库"), repoBox, JLabel("账号"), accountBox,
            button("连接") { connect() }, button("重新检测") { discover() },
            button("GitHub 账号") { ShowSettingsUtil.getInstance().showSettingsDialog(project, "GitHub") }), connection), BorderLayout.NORTH)
        val left = JPanel(BorderLayout(3, 3))
        left.add(column(JLabel("搜索 Issue"), query, filter), BorderLayout.NORTH)
        left.add(JBScrollPane(list))
        left.add(line(listStatus, more, button("刷新") { resetList() }), BorderLayout.SOUTH)
        list.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(l: JList<*>, v: Any?, i: Int, selected: Boolean, focus: Boolean): Component {
                val c = super.getListCellRendererComponent(l, v, i, selected, focus)
                if (v is IssueReviewTask) {
                    text = "#${v.numberValue}  ${v.summary}  ·  ${if (v.isClosed) "已关闭" else "打开"}"
                    toolTipText = "更新于 ${v.updated ?: "未知"}"
                }
                return c
            }
        }
        title.font = title.font.deriveFont(Font.BOLD, title.font.size2D + 2f)
        panelRight.add(column(title, actions), BorderLayout.NORTH)
        val thread = JPanel(BorderLayout())
        thread.add(JBScrollPane(discussion))
        thread.add(line(discussionStatus, moreComments), BorderLayout.SOUTH)
        tabs.addTab("讨论", thread)
        tabs.addTab("评估报告", reportPanel)
        panelRight.add(tabs)
        panelRight.add(footer, BorderLayout.SOUTH)
        panel.add(JBSplitter(false, 0.29f).apply { firstComponent = left; secondComponent = panelRight })
        repoBox.renderer = labelRenderer { (it as? RepoChoice)?.label ?: "选择仓库" }
        accountBox.renderer = labelRenderer { (it as? AccountChoice)?.login ?: "选择账号" }
        list.addListSelectionListener { if (!it.valueIsAdjusting && !updating) list.selectedValue?.let { row -> selectIssue(row.numberValue) } }
        filter.addActionListener { resetList() }
        query.onChange { searchTimer.restart() }
        repoBox.addActionListener { if (!updating) invalidateConnection() }
        accountBox.addActionListener { if (!updating) invalidateConnection() }
        tabs.addChangeListener { if (tabs.selectedIndex == 0 && comments.isEmpty()) loadComments() }
        discover()
    }

    private fun labelRenderer(label: (Any?) -> String) = object : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(l: JList<*>, value: Any?, i: Int, s: Boolean, f: Boolean): Component {
            super.getListCellRendererComponent(l, value, i, s, f)
            text = label(value)
            return this
        }
    }

    private fun <T> background(work: suspend () -> T, done: (T) -> Unit, failed: (String) -> Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val result = work()
                SwingUtilities.invokeLater { if (!disposed && !project.isDisposed) done(result) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                SwingUtilities.invokeLater { if (!disposed && !project.isDisposed) failed(e.message ?: "操作失败，请重试") }
            }
        }
    }

    private fun invalidateConnection() {
        epoch++
        session = null
        connection.text = "请选择仓库与账号，然后连接"
        queryEpoch++
        nextPage = null
        paging = false
        issues.clear()
        renderList()
        selectIssue(null)
    }

    private fun discover() {
        invalidateConnection()
        val e = epoch
        background({ gateway.discover() }, { found ->
            if (e != epoch) return@background
            updating = true
            repoBox.model = DefaultComboBoxModel(found.repositories.toTypedArray())
            accountBox.model = DefaultComboBoxModel(found.accounts.toTypedArray())
            repoBox.selectedItem = found.repositories.firstOrNull { it.key.toString() == props.getValue("issueReview.repo") }
                ?: found.repositories.singleOrNull()
            accountBox.selectedItem = found.accounts.firstOrNull { it.id == props.getValue("issueReview.account") }
                ?: found.accounts.singleOrNull()
            updating = false
            connection.text = when {
                found.repositories.isEmpty() -> "项目中没有可用的 GitHub fetch remote"
                found.accounts.isEmpty() -> "请在 IDE 的 GitHub 设置登录，然后重新检测"
                else -> "请选择仓库与账号"
            }
            if (repoBox.selectedItem != null && accountBox.selectedItem != null) connect()
        }, { if (e == epoch) connection.text = it })
    }

    private fun connect() {
        val target = repoBox.selectedItem as? RepoChoice ?: return
        val account = accountBox.selectedItem as? AccountChoice ?: return
        invalidateConnection()
        val e = epoch
        connection.text = "正在核对账号与仓库权限…"
        background({ gateway.connect(target.key, account.id) }, { result ->
            if (e != epoch) return@background
            session = result
            props.setValue("issueReview.repo", target.key.toString())
            props.setValue("issueReview.account", account.id)
            connection.text = "${target.label} · ${account.login}"
            resetList()
        }, { if (e == epoch) connection.text = it })
    }

    private fun resetList() {
        queryEpoch++
        issues.clear()
        paging = false
        nextPage = if (session != null) 1 else null
        renderList()
        loadPage()
    }

    private fun loadPage() {
        val s = session ?: return
        if (paging) return
        val p = nextPage ?: return
        val e = queryEpoch
        val state = filter.selectedItem as IssueFilter
        paging = true
        listStatus.text = if (query.text.isBlank()) "加载中…" else "正在搜索更多 Issue…"
        more.isEnabled = false
        background({ gateway.list(s, state, p) }, { page ->
            if (e != queryEpoch) return@background
            paging = false
            page.items.forEach { issues[it.numberValue] = it }
            nextPage = page.nextPage
            renderList()
            if (nextPage != null && (query.text.isNotBlank() || listModel.isEmpty)) loadPage()
        }, { if (e == queryEpoch) { paging = false; listStatus.text = it; more.isEnabled = true } })
    }

    private fun renderList() {
        updating = true
        listModel.clear()
        val q = query.text.trim()
        val n = q.removePrefix("#").toLongOrNull()
        issues.values.filter { if (n != null) it.numberValue == n else it.summary.contains(q, true) }.forEach {
            listModel.addElement(it)
            if (it.numberValue == number) list.selectedIndex = listModel.size() - 1
        }
        updating = false
        listStatus.text = if (listModel.isEmpty && nextPage == null) "无匹配 Issue" else "${listModel.size()} 条"
        more.isEnabled = nextPage != null && !paging
    }

    private fun selectIssue(id: Long?) {
        val e = ++selection
        number = id
        detail = null
        comments.clear()
        loadingComments = false
        nextComments = if (id != null) 1 else null
        val row = issues[id]
        title.text = row?.let { "#${it.numberValue}  ${it.summary}" } ?: if (id == null) "请选择 Issue" else "#$id"
        discussion.text = if (id == null) "" else "正在加载正文…"
        discussionStatus.text = " "
        open.isEnabled = false
        val s = session ?: return
        if (id == null) return
        background({ gateway.detail(s, id) }, { task ->
            if (e != selection) return@background
            detail = task
            title.text = "${s.repo.owner}/${s.repo.repo}  #$id  ${task.summary}  ·  ${if (task.isClosed) "已关闭" else "打开"}"
            open.isEnabled = task.issueUrl != null
            renderDiscussion()
        }, { if (e == selection) discussion.text = "正文读取失败：$it" })
        if (tabs.selectedIndex == 0) loadComments()
    }

    private fun loadComments() {
        val s = session ?: return
        val id = number ?: return
        val p = nextComments ?: return
        if (loadingComments) return
        val e = selection
        loadingComments = true
        discussionStatus.text = "正在加载评论…"
        moreComments.isEnabled = false
        background({ gateway.comments(s, id, p) }, { page ->
            if (e != selection) return@background
            loadingComments = false
            page.items.forEach { comments[it.remoteId] = it }
            nextComments = page.nextPage
            moreComments.isEnabled = nextComments != null
            discussionStatus.text = "${comments.size} 条评论"
            renderDiscussion()
        }, {
            if (e == selection) { loadingComments = false; moreComments.isEnabled = true; discussionStatus.text = "评论读取失败：$it" }
        })
    }

    private fun renderDiscussion() {
        val task = detail ?: return
        val caret = discussion.caretPosition
        discussion.text = buildString {
            append(task.authorLogin ?: "未知用户").append("  ·  ").append(task.created ?: "未知日期").append("\n\n")
            append(task.description?.takeIf { it.isNotEmpty() } ?: "未填写正文")
            comments.values.sortedWith(compareBy({ it.createdAt }, { it.remoteId })).forEach {
                append("\n\n────────\n").append(it.login ?: "未知用户").append("  ·  ").append(it.createdAt ?: "").append("  #").append(it.remoteId)
                append("\n").append(it.body)
            }
        }
        discussion.caretPosition = caret.coerceAtMost(discussion.document.length)
    }

    override fun dispose() { disposed = true; searchTimer.stop(); scope.cancel() }
}
