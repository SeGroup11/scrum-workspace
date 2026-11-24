package fixture;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import team.segroup11.review.state.ReviewSnapshot;
import team.segroup11.review.state.ReviewStore;
import team.segroup11.review.state.ViewPointer;

/** Checks native project storage and the registered Tasks extension in two separate IDE processes. */
public final class StateProbe implements ApplicationStarter {
    private static final String INITIAL = "初稿 A";
    private static final String LATEST = "开头\n\n正文带尾空格  \n\t最后一个字终  ";
    private static final ViewPointer A = new ViewPointer("fixture-account", "github.com/fixture/test", 16, 1);
    private static final ViewPointer B = new ViewPointer("fixture-account", "github.com/fixture/test", 17, 0);
    @Override public boolean isHeadless() { return true; }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void configureModel(Application app, String url, boolean clear) throws Exception {
        ClassLoader loader = StateProbe.class.getClassLoader();
        String pkg = "team.segroup11.review.model.";
        Class<?> type = Class.forName(pkg + "ModelSettings", true, loader);
        Class<?> mode = Class.forName(pkg + "OutputMode", true, loader);
        Object options = Class.forName(pkg + "ModelOptions", true, loader)
                .getConstructor(String.class, String.class, mode, int.class, int.class)
                .newInstance(url, "fixture", mode.getEnumConstants()[0], 32768, 4096);
        var save = Arrays.stream(type.getMethods()).filter(m -> m.getName().startsWith("save") && m.getParameterCount() == 3).findFirst().orElseThrow();
        save.invoke(app.getService(type), options, clear ? new char[0] : "fixture-only".toCharArray(), clear);
    }

    private static Object tasksProvider() {
        return ExtensionPointName.create("com.intellij.tasks.contextProvider").getExtensionList().stream()
                .filter(p -> p.getClass().getName().equals("team.segroup11.review.state.ReviewContextProvider"))
                .findFirst().orElseThrow();
    }

    private static void saveOldContext(Project project, Object provider, Path path) throws Exception {
        Element task = new Element("context");
        provider.getClass().getMethod("saveContext", Project.class, Element.class).invoke(provider, project, task);
        Element view = task.getChild("view");
        require(view != null && "16".equals(view.getAttributeValue("number")), "No A locator in task snapshot");
        require(view.getAttributes().stream().map(a -> a.getName()).collect(Collectors.toSet())
                .equals(Set.of("account", "repo", "number", "tab")), "Task snapshot includes mutable fields");
        Files.writeString(path, new XMLOutputter().outputString(task));
    }

    private static void seed(ReviewStore store, Project project, Path root, Object provider, AtomicInteger requests) throws Exception {
        store.select(A);
        store.edit(A, INITIAL);
        saveOldContext(project, provider, root.resolve("old-context.xml"));
        require(store.analyze(A, Files.readString(root.resolve("title.txt")), Files.readString(root.resolve("body.txt"))), "Analysis not started");
        long deadline = System.nanoTime() + 25_000_000_000L;
        while (store.snapshot(A).getRunning() && System.nanoTime() < deadline) Thread.sleep(50);
        var report = store.snapshot(A).getReport();
        require(report != null && requests.get() == 1, "Expected one completed fixture model request");
        Files.writeString(root.resolve("report-id.txt"), report.getRequestId());
        store.edit(A, LATEST);
        store.select(B);
        store.edit(B, "准备主动清空的 B");
        store.edit(B, "");
    }

    private static void verify(ReviewStore store, Project project, Path root, Object provider) throws Exception {
        require(B.equals(store.getLastView()), "Last B pointer/tab changed on restart");
        ReviewSnapshot b = store.snapshot(B);
        require(b.getDraftText().isEmpty() && b.getDraftRevision() == 2, "B's deliberate clear was lost");
        ReviewSnapshot a = store.snapshot(A);
        require(a.getDraftText().equals(LATEST) && a.getTab() == 1, "A whitespace/last character/tab mismatch");
        require(a.getReport() != null && a.getReport().getRequestId().equals(Files.readString(root.resolve("report-id.txt"))), "A report replaced");
        require(b.getReport() == null, "A report appeared on B");
        var element = new SAXBuilder().build(root.resolve("old-context.xml").toFile()).getRootElement();
        provider.getClass().getMethod("loadContext", Project.class, Element.class).invoke(provider, project, element);
        require(A.equals(store.getLastView()), "Old task snapshot failed to restore A locator/tab");
        require(store.snapshot(A).equals(a) && store.snapshot(B).equals(b), "Old snapshot overwrote current record");
    }

    @Override public void main(List<String> args) {
        String phase = System.getProperty("review.probe.phase");
        Path root = Path.of(System.getProperty("review.probe.path"));
        Application app = ApplicationManager.getApplication();
        List<String> record = new ArrayList<>();
        HttpServer server = null;
        String serviceUrl = null;
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            AtomicInteger model = new AtomicInteger();
            byte[] response = Files.readAllBytes(root.resolve("response.json"));
            server.createContext("/v1/chat/completions", e -> {
                model.incrementAndGet();
                try (var body = e.getRequestBody()) { body.readAllBytes(); }
                e.getResponseHeaders().set("Content-Type", "application/json");
                e.sendResponseHeaders(200, response.length);
                try (var body = e.getResponseBody()) { body.write(response); }
            });
            server.start();
            serviceUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
            configureModel(app, serviceUrl, false);
            ProjectManager manager = ProjectManager.getInstance();
            Project p = manager.loadAndOpenProject(root.resolve("QA-Project").toString());
            require(p != null, "Project was not opened");
            record.add("process=" + ProcessHandle.current().pid() + " phase=" + phase + " project=" + p.getName());
            ReviewStore store = p.getService(ReviewStore.class);
            Object provider = tasksProvider();
            record.add("registered Tasks extension=" + provider.getClass().getName());
            if ("write".equals(phase)) seed(store, p, root, provider, model);
            else verify(store, p, root, provider);
            p.save();
            app.invokeAndWait(() -> require(manager.closeAndDispose(p), "Native project close failed"));
            require(p.isDisposed(), "Project not disposed");
            record.add("nativeProjectClose=true");
            record.add("modelChatRequests=" + model.get());
            require(model.get() == ("write".equals(phase) ? 1 : 0), "Unexpected model replay");
            record.add("GitHub GET/POST uninstrumented; this project has no GitHub account or remote.");
            if ("read".equals(phase)) record.add("Last B tab and explicit empty edit revision, exact A draft with blank line, tab, terminal character and trailing spaces, saved A report identity, and old A-only Tasks locator verified.");
            record.add("PASS");
        } catch (Throwable failure) {
            var trace = new java.io.StringWriter();
            failure.printStackTrace(new java.io.PrintWriter(trace));
            record.add(trace.toString());
        } finally {
            if (serviceUrl != null) try { configureModel(app, serviceUrl, true); } catch (Exception e) { record.add(e.toString()); }
            if (server != null) server.stop(0);
            try { Files.write(root.resolve(phase + ".txt"), record); } catch (Exception e) { e.printStackTrace(); }
            app.invokeLater(app::exit);
        }
    }
}
