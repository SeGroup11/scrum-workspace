package team.segroup11.review.state

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import org.jdom.Element
import team.segroup11.review.github.GitHubFailure
import team.segroup11.review.github.ReviewComment
import team.segroup11.review.model.ModelConfigurationException
import team.segroup11.review.model.ModelFailure
import team.segroup11.review.model.ModelGateway
import java.util.concurrent.CopyOnWriteArrayList

@Service(Service.Level.PROJECT)
@State(name = "Group11IssueReview", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ReviewStore(private val project: Project, private val scope: CoroutineScope) : PersistentStateComponent<Element>, Disposable {
    private val lock = Any()
    private var archive = ReviewArchive()
    private var data = ReviewData()
    private data class Listener(val changed: (String) -> Unit, val restored: (ViewPointer?) -> Unit)
    private val listeners = CopyOnWriteArrayList<Listener>()
    @Volatile private var disposed = false

    val lastView get() = synchronized(lock) { data.lastView }
    fun snapshot(p: ViewPointer): ReviewSnapshot = synchronized(lock) { data.snapshot(p) }
    fun select(p: ViewPointer?) { synchronized(lock) { data.select(p) } }
    fun edit(p: ViewPointer, text: String) { synchronized(lock) { data.edit(p, text) } }

    fun subscribe(parent: Disposable, changed: (String) -> Unit, restored: (ViewPointer?) -> Unit) {
        val listener = Listener(changed, restored)
        listeners.add(listener)
        Disposer.register(parent, Disposable { listeners.remove(listener) })
    }

    fun restore(p: ViewPointer?) {
        // Restoring a locator is safe before the tool window exists. Only the window may bind
        // records after checking that the selected IDE identity still matches the locator.
        synchronized(lock) { if (!data.futureVersion) data.lastView = p }
        onEdt { listeners.forEach { it.restored(p) } }
    }

    fun bindTask(taskId: String, p: ViewPointer) {
        synchronized(lock) { if (!data.futureVersion) data.tasks[taskId] = p }
    }
    fun boundTask(taskId: String): ViewPointer? = synchronized(lock) { data.tasks[taskId] }
    internal fun taskContext(taskId: String): ViewPointer? = synchronized(lock) {
        val visible = data.lastView
        val bound = data.tasks[taskId]
        if (bound == null || bound.key == visible?.key) visible else bound.copy(tab = data.records[bound.key]?.tab ?: bound.tab)
    }

    fun analyze(p: ViewPointer, title: String, body: String?): Boolean {
        val id = synchronized(lock) { data.startAnalysis(p) } ?: return false
        changed(p)
        scope.launch(Dispatchers.IO) {
            val started = System.nanoTime()
            try {
                val result = runInterruptible { ModelGateway().evaluate(title, body) }
                synchronized(lock) { data.finishAnalysis(p, id, result, null, false) }
            } catch (e: CancellationException) {
                synchronized(lock) { data.finishAnalysis(p, id, null, "分析已取消。", false) }
                throw e
            } catch (e: Exception) {
                val reason = if (e is ModelFailure) e.message else "分析失败，可手动重试。"
                synchronized(lock) { data.finishAnalysis(p, id, null, reason, e is ModelConfigurationException) }
            } finally {
                com.intellij.openapi.diagnostic.Logger.getInstance(ReviewStore::class.java)
                    .info("IssueReview analysis completed elapsed_ms=" + (System.nanoTime() - started) / 1_000_000)
                changed(p)
            }
        }
        return true
    }

    fun send(p: ViewPointer, post: suspend (String) -> ReviewComment): Boolean {
        val attempt = synchronized(lock) { data.startSend(p) } ?: return false
        changed(p)
        scope.launch(Dispatchers.IO) {
            try {
                val receipt = post(attempt.sentText)
                synchronized(lock) { data.sent(p, attempt.attemptId, receipt) }
            } catch (e: Exception) {
                val unknown = e !is GitHubFailure || e.outcomeUnknown
                val reason = if (unknown) "发布结果待确认，请先核对原 Issue 的评论。" else e.message ?: "评论发送失败。"
                synchronized(lock) { data.sendFailed(p, attempt.attemptId, unknown, reason) }
                if (e is CancellationException) throw e
            } finally {
                changed(p)
            }
        }
        return true
    }

    /** Called only after fetching the original issue's comments for this attempt. */
    fun checked(p: ViewPointer, attemptId: String) {
        synchronized(lock) { data.checked(p, attemptId) }
        changed(p)
    }
    fun confirmPublished(p: ViewPointer, attemptId: String, comment: ReviewComment) {
        synchronized(lock) { data.reconcile(p, attemptId, comment) }
        changed(p)
    }
    fun allowResend(p: ViewPointer, attemptId: String) {
        synchronized(lock) { data.reconcile(p, attemptId, null) }
        changed(p)
    }

    private fun changed(p: ViewPointer) = onEdt { listeners.forEach { it.changed(p.key) } }
    private fun onEdt(action: () -> Unit) {
        if (disposed || project.isDisposed) return
        val app = ApplicationManager.getApplication()
        if (app.isDispatchThread) action()
        else app.invokeLater({ if (!disposed && !project.isDisposed) action() }, ModalityState.any())
    }

    override fun loadState(state: Element) {
        val nextArchive = ReviewArchive(state.clone())
        val nextData = nextArchive.load()
        synchronized(lock) { archive = nextArchive; data = nextData }
    }

    override fun getState(): Element {
        // Immutable records can be serialized off the edit lock, including large reports.
        val encode = synchronized(lock) {
            val raw = archive
            val view = data.lastView
            val tasks = data.tasks.toMap()
            val records = data.records.toMap()
            val pending = data.pending.toMap()
            val future = data.futureVersion
            return@synchronized { raw.write(view, tasks, records, pending, future) }
        }
        return encode()
    }

    override fun dispose() { disposed = true; listeners.clear() }
}
