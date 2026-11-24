package fixture;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskListener;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Unit;
import team.segroup11.review.github.IssueReviewTask;
import team.segroup11.review.github.RepoKey;
import team.segroup11.review.state.ReviewStore;
import team.segroup11.review.state.ViewPointer;

/** Genuine Tasks API and context storage, fresh project, synthetic tasks, no Git or model transport. */
public final class TaskProbe implements ApplicationStarter {
    private final List<String> trace = new CopyOnWriteArrayList<>();
    private final AtomicBoolean passed = new AtomicBoolean(true);
    private final RepoKey repo = new RepoKey("github.com", "fixture", "task-regression");
    private final ViewPointer a = new ViewPointer("fixture", repo.getCanonical(), 16, 0);
    private final ViewPointer b = new ViewPointer("fixture", repo.getCanonical(), 17, 1);
    private TaskManager manager;
    private ReviewStore store;
    private String phase;
    @Override public boolean isHeadless() { return true; }

    private IssueReviewTask issue(ViewPointer ref) {
        return new IssueReviewTask(repo, ref.getNumber(), "Sample " + ref.getNumber(), false, "Fixture only", true,
                "qa", null, null, null, TaskType.FEATURE, List.of());
    }
    private static String view(ViewPointer p) {
        return p == null ? "<empty>" : p.getNumber() + "/tab" + p.getTab();
    }
    private void observe(String event) {
        trace.add(phase + " " + event + " active=" + manager.getActiveTask().getId() + " view=" + view(store.getLastView()));
    }
    private void verify(boolean truth, String label) {
        trace.add(phase + " " + label + "=" + truth);
        if (!truth) passed.set(false);
    }
    private void selectAndActivate(ViewPointer ref, IssueReviewTask task) {
        store.select(ref);
        try {
            // On the fixed candidate, call exactly the same entry point as the production button.
            store.getClass().getMethod("activateTask", Task.class, ViewPointer.class).invoke(store, task, ref);
            trace.add(phase + " called ReviewStore.activateTask(Task, ViewPointer)");
        } catch (NoSuchMethodException legacy) {
            // 0.2.2's production button performs these two public calls inline.
            try {
                store.getClass().getMethod("bindTask", String.class, ViewPointer.class).invoke(store, task.getId(), ref);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Legacy task binding failed", failure);
            }
            manager.activateTask(task, true);
            trace.add(phase + " called 0.2.2 native binding sequence");
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Production task activation failed", failure);
        }
    }
    private void run(String label, ViewPointer pointer, Runnable activate) throws Exception {
        phase = label;
        var app = ApplicationManager.getApplication();
        app.invokeAndWait(activate);
        // Allow contexts scheduled from the two platform listeners to reach the EDT.
        Thread.sleep(1200);
        app.invokeAndWait(() -> {
            observe("settled");
            verify(manager.getActiveTask().getId().equals(issue(pointer).getId()), "nativeTaskMatches");
            verify(pointer.equals(store.getLastView()), "viewMatches");
            verify(store.snapshot(a).getDraftText().equals("A 的最后一字甲  ") &&
                    store.snapshot(b).getDraftText().equals("B 的独立草稿乙"), "bothDraftsIntact");
        });
    }
    @Override public void main(List<String> args) {
        Path root = Path.of(System.getProperty("review.probe.path"));
        var app = ApplicationManager.getApplication();
        try {
            ProjectManager projects = ProjectManager.getInstance();
            Project project = projects.loadAndOpenProject(root.resolve("Task-QA").toString());
            manager = TaskManager.getManager(project);
            store = project.getService(ReviewStore.class);
            manager.addTaskListener(new TaskListener() {
                public void taskDeactivated(LocalTask t) { observe("deactivated(" + t.getId() + ")"); }
                public void taskActivated(LocalTask t) { observe("activated(" + t.getId() + ")"); }
                public void taskAdded(LocalTask t) { observe("added(" + t.getId() + ")"); }
                public void taskRemoved(LocalTask t) { observe("removed(" + t.getId() + ")"); }
            }, project);
            store.subscribe(project, k -> Unit.INSTANCE, p -> { observe("restore(" + view(p) + ")"); return Unit.INSTANCE; });
            store.edit(a, "A 的最后一字甲  "); store.edit(b, "B 的独立草稿乙");
            IssueReviewTask first = issue(a), second = issue(b);
            phase = "fresh";
            verify(manager.findTask(first.getId()) == null && manager.findTask(second.getId()) == null, "noExistingTasks");
            run("firstA", a, () -> selectAndActivate(a, first));
            run("firstB", b, () -> selectAndActivate(b, second));
            run("backA", a, () -> manager.activateTask(manager.findTask(first.getId()), true));
            run("backB", b, () -> manager.activateTask(manager.findTask(second.getId()), true));
            project.save();
            app.invokeAndWait(() -> { if (!projects.closeAndDispose(project)) throw new AssertionError("Native close failed"); });
            trace.add(passed.get() ? "PASS" : "FAIL");
        } catch (Throwable t) {
            var stack = new java.io.StringWriter(); t.printStackTrace(new java.io.PrintWriter(stack));
            trace.add(stack.toString()); trace.add("FAIL");
        } finally {
            try { Files.write(root.resolve("result.txt"), trace); } catch(Exception e) { e.printStackTrace(); }
            app.invokeLater(app::exit);
        }
    }
}
