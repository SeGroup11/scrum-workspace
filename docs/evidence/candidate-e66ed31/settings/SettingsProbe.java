package fixture;

import com.intellij.ide.actions.ShowSettingsUtilImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.swing.JLabel;
import javax.swing.JTextField;

/** Standalone test plugin, loaded only in a new QA profile. Emits no credential values. */
public final class SettingsProbe implements ApplicationStarter {
    @Override public boolean isHeadless() { return true; }

    private static JTextField labeled(Component root, String label) {
        if (root instanceof JLabel text && label.equals(text.getText())) return (JTextField) text.getLabelFor();
        if (root instanceof Container parent) {
            for (Component child : parent.getComponents()) {
                JTextField result = labeled(child, label);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static String[] readSettings(Project project) {
        Configurable entry = ShowSettingsUtilImpl.getConfigurables(project, true, true).stream()
                .filter(candidate -> "GitHub Issue 质量评审".equals(candidate.getDisplayName()))
                .findFirst().orElseThrow();
        Component panel = entry.createComponent();
        entry.reset();
        try {
            return new String[] { labeled(panel, "服务地址（Base URL）").getText(), labeled(panel, "模型 ID").getText() };
        } finally {
            entry.disposeUIResources();
        }
    }

    private static Method named(Class<?> type, String prefix, int params) {
        return Arrays.stream(type.getMethods())
                .filter(m -> m.getName().startsWith(prefix) && m.getParameterCount() == params)
                .findFirst().orElseThrow();
    }

    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    @Override public void main(List<String> args) {
        Path base = Path.of(System.getProperty("review.probe.path"));
        List<String> output = new ArrayList<>();
        int exit = 1;
        try {
            ClassLoader loader = getClass().getClassLoader();
            String pkg = "team.segroup11.review.model.";
            Class<?> type = Class.forName(pkg + "ModelSettings", true, loader);
            Object settings = ApplicationManager.getApplication().getService(type);
            Class<?> mode = Class.forName(pkg + "OutputMode", true, loader);
            var options = Class.forName(pkg + "ModelOptions", true, loader)
                    .getConstructor(String.class, String.class, mode, int.class, int.class);
            String unique = "/group11-isolated-settings-test/" + UUID.randomUUID();
            String urlA = "http://127.0.0.1:18535" + unique;
            String urlB = "http://localhost:18535" + unique;
            String testKey = "synthetic-" + UUID.randomUUID();
            Object a = options.newInstance(urlA, "slow-success", mode.getEnumConstants()[0], 32768, 4096);
            Object b = options.newInstance(urlB, "slow-success", mode.getEnumConstants()[0], 32768, 4096);
            Method save = named(type, "save", 3);
            Method profile = named(type, "profile", 0);
            try {
                save.invoke(settings, a, testKey.toCharArray(), false);
                Object first = profile.invoke(settings);
                Method readKey = named(first.getClass(), "getApiKey", 0);
                check(testKey.equals(readKey.invoke(first)), "A credential mismatch");
                output.add("A: saved and resolved the synthetic credential.");
                save.invoke(settings, b, new char[0], false);
                try {
                    profile.invoke(settings);
                    throw new AssertionError("B reused A's credential");
                } catch (InvocationTargetException e) {
                    check(e.getCause().getClass().getName().equals(pkg + "ModelConfigurationException")
                                    && e.getCause().getMessage().contains("此服务地址保存 API 密钥"),
                            "Unexpected error at endpoint B");
                }
                output.add("B: changing host without a key produced the endpoint-specific missing-key error.");
                save.invoke(settings, a, new char[0], false);
                check(testKey.equals(readKey.invoke(profile.invoke(settings))), "A credential was lost");
                output.add("A: switching back without a new key restored the original synthetic credential.");
                ProjectManager manager = ProjectManager.getInstance();
                Project one = manager.createProject("Project-One", Files.createDirectory(base.resolve("Project-One")).toString());
                Project two = manager.createProject("Project-Two", Files.createDirectory(base.resolve("Project-Two")).toString());
                check(one != null && two != null && one != two, "Separate platform projects required");
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    for (Project project : List.of(one, two)) {
                        String[] values = readSettings(project);
                        check(urlA.equals(values[0]) && "slow-success".equals(values[1]), "Project settings mismatch");
                        output.add(project.getName() + ": actual registered Settings form shows the same base URL and model.");
                    }
                });
            } finally {
                save.invoke(settings, b, new char[0], true);
                save.invoke(settings, a, new char[0], true);
            }
            output.add("PASS");
            exit = 0;
        } catch (Throwable error) {
            java.io.StringWriter trace = new java.io.StringWriter();
            error.printStackTrace(new java.io.PrintWriter(trace));
            output.add(trace.toString());
        } finally {
            try { Files.write(base.resolve("result.txt"), output); } catch (Exception e) { e.printStackTrace(); }
            System.exit(exit);
        }
    }
}
