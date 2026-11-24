package team.segroup11.review.state

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskManager
import com.intellij.tasks.context.WorkingContextProvider
import org.jdom.Element

class ReviewContextProvider : WorkingContextProvider() {
    override fun getId() = "segroup11-issue-review"
    override fun getDescription() = "GitHub Issue 质量评审"
    override fun saveContext(project: Project, element: Element) {
        val active = TaskManager.getManager(project).activeTask.id
        val pointer = project.service<ReviewStore>().taskContext(active)
        pointer?.let { element.addContent(it.toElement("view")) }
    }
    override fun loadContext(project: Project, element: Element) {
        val store = project.service<ReviewStore>()
        val saved = pointerFrom(element.getChild("view"))
        store.restore(saved ?: store.boundTask(TaskManager.getManager(project).activeTask.id))
    }
    override fun clearContext(project: Project) {
        project.service<ReviewStore>().clearTaskContext(TaskManager.getManager(project).activeTask.id)
    }
}
