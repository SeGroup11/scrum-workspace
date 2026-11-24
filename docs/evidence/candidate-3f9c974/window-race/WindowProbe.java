package fixture;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ThrowableConvertor;
import java.awt.Component;
import java.awt.Container;
import java.io.*;
import java.lang.reflect.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javax.swing.*;
import org.jetbrains.plugins.github.api.*;
import team.segroup11.review.ReviewWindow;
import team.segroup11.review.github.*;
import kotlin.coroutines.Continuation;

/** Two in-memory account transports, the installed decoder and actual ReviewWindow controls. */
public final class WindowProbe implements ApplicationStarter {
    private final List<String> trace = new CopyOnWriteArrayList<>();
    private final CountDownLatch enteredA = new CountDownLatch(2), releaseA = new CountDownLatch(1), returnedA = new CountDownLatch(2);
    private final RepoKey a = new RepoKey("github.com", "qa", "alpha"), b = new RepoKey("github.com", "qa", "beta");
    private ReviewWindow window;
    private boolean hold = true;
    @Override public boolean isHeadless() { return true; }
    @Override public int getRequiredModality() { return NOT_IN_EDT; }

    @SuppressWarnings("unchecked")
    private static <T> T field(Object object, String name) {
        try {
            var f = object.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(object);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    private static void edt(Runnable r) { ApplicationManager.getApplication().invokeAndWait(r); }
    private void waitFor(String what, BooleanSupplier test) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
        while (System.nanoTime() < deadline) {
            boolean[] yes = {false}; edt(() -> yes[0] = test.getAsBoolean());
            if (yes[0]) { trace.add("observed " + what); return; }
            Thread.sleep(35);
        }
        throw new AssertionError("Timeout: " + what);
    }
    private static JButton button(Container c, String label) {
        for (Component child : c.getComponents()) {
            if (child instanceof JButton b && label.equals(b.getText())) return b;
            if (child instanceof Container nested) {
                JButton found = button(nested, label); if (found != null) return found;
            }
        }
        return null;
    }
    private static String issue(RepoKey r, int id, boolean pr, boolean closed) {
        return "{\"number\":"+id+",\"title\":\""+r.getRepo()+"-"+id+"\",\"body\":\"BODY-"+r.getRepo()+"\",\"html_url\":\"https://github.com/qa/"+r.getRepo()+"/issues/"+id+"\",\"state\":\""+(closed?"closed":"open")+"\""+(pr?",\"pull_request\":{}":"")+"}";
    }
    private final class Access implements ConnectionAccess {
        public Discovery discover() {
            return new Discovery(List.of(new RepoChoice(a,"Alpha"),new RepoChoice(b,"Beta")),
                    List.of(new AccountChoice("one","one","github.com"),new AccountChoice("two","two","github.com")),null,true);
        }
        public boolean isAvailable(String id, RepoKey r) { return List.of("one","two").contains(id) && List.of(a,b).contains(r); }
        public boolean reauthenticate(String id, RepoKey r, Component c) { throw new AssertionError("No authentication in replay"); }
        public Object bind(String account, RepoKey repo, Continuation<? super BoundAccount> c) {
            return new BoundAccount(GithubServerPath.DEFAULT_SERVER,new GitHubTransport() {
                @SuppressWarnings({"unchecked","rawtypes"})
                public <T> Object execute(GithubApiRequest<? extends T> req, Continuation<? super T> c) {
                    URI uri=URI.create(req.getUrl());
                    String path=uri.getPath(), q=Objects.toString(uri.getQuery(),"");
                    if (req instanceof GithubApiRequest.Post) throw new AssertionError("Unexpected POST");
                    trace.add("GET "+account+" "+path+"?"+q);
                    String body, next=null; int id=repo.equals(a)?16:17;
                    boolean blocked=repo.equals(a) && hold && (path.endsWith("/16")||path.endsWith("/16/comments"));
                    if (blocked) {
                        enteredA.countDown();
                        try { if (!releaseA.await(15,TimeUnit.SECONDS)) throw new AssertionError("A not released"); }
                        catch (InterruptedException e) { throw new IllegalStateException(e); }
                    }
                    if(path.equals("/user")) body="{\"id\":"+(account.equals("one")?101:202)+"}";
                    else if(path.equals("/repos/qa/"+repo.getRepo())) body="{\"name\":\""+repo.getRepo()+"\",\"owner\":{\"login\":\"qa\"}}";
                    else if(path.endsWith("/issues") && q.endsWith("page=1")) {
                        body="["+issue(repo,90,true,false)+"]"; next=req.getUrl().split("\\?")[0]+"?page=2";
                    } else if(path.endsWith("/issues")) body="["+issue(repo,id,false,q.contains("state=closed"))+"]";
                    else if(path.endsWith("/comments")) body="[{\"id\":"+id+",\"body\":\"COMMENT-"+repo.getRepo()+"\",\"user\":{\"login\":\""+account+"\"}}]";
                    else if(path.endsWith("/"+id)) body=issue(repo,id,false,false);
                    else throw new AssertionError("Unconfigured GET "+path);
                    final String link=next;
                    var response=(GithubApiResponse)Proxy.newProxyInstance(GithubApiResponse.class.getClassLoader(),new Class[]{GithubApiResponse.class},(p,m,args)->switch(m.getName()){
                        case "findHeader" -> "link".equalsIgnoreCase((String)args[0]) && link!=null?"<"+link+">; rel=\"next\"":null;
                        case "readBody" -> ((ThrowableConvertor)args[0]).convert(new StringReader(body));
                        case "handleBody" -> ((ThrowableConvertor)args[0]).convert(new ByteArrayInputStream(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                        default -> throw new AssertionError(m.getName());
                    });
                    try {
                        Object decoded=req.extractResult(response);
                        if(blocked) { trace.add("delayed A decoded "+path); returnedA.countDown(); }
                        return decoded;
                    } catch(IOException e) { throw new UncheckedIOException(e); }
                }
            });
        }
    }
    private boolean showingB() {
        String title=((JLabel)field(window,"title")).getText(), body=((JTextArea)field(window,"discussion")).getText();
        return title.contains("#17") && title.contains("beta-17") && body.contains("BODY-beta") && body.contains("COMMENT-beta")
                && !body.contains("alpha") && ((JLabel)field(window,"commentCount")).getText().startsWith("1 ");
    }
    private void traceView(String when, Project p) {
        edt(()->{
            Object edit=field(window,"editor");
            trace.add(when+" connection="+((JLabel)field(window,"connection")).getText()+"; title="+((JLabel)field(window,"title")).getText()+
                    "; target="+((JLabel)field(edit,"targetLabel")).getText()+"; draft="+((JTextArea)field(edit,"editor")).getText()+
                    "; pending="+field(window,"awaitingRestore")+"; latest="+p.getService(team.segroup11.review.state.ReviewStore.class).getLastView());
        });
    }
    private void openReplay(Project project) throws Exception {
        edt(()->window=new ReviewWindow(project));
        waitFor("blank isolated IDE discovered no remote",()->((JLabel)field(window,"connection")).getText().contains("没有"));
        var gateway=(GitHubGateway)field(window,"gateway");
        var seam=gateway.getClass().getDeclaredField("access"); seam.setAccessible(true); seam.set(gateway,new Access());
        edt(()->button(window.getPanel(),"重新检测").doClick());
    }
    @Override public void main(List<String> args) {
        var app=ApplicationManager.getApplication(); Path root=Path.of(System.getProperty("review.probe.path"));
        try {
            var projects=ProjectManager.getInstance();
            Project project=projects.loadAndOpenProject(root.resolve("Window-QA").toString());
            openReplay(project);
            JComboBox<?> repos=field(window,"repoBox"), accounts=field(window,"accountBox"), filter=field(window,"filter");
            JList<IssueReviewTask> list=field(window,"list");
            waitFor("two repository choices",()->repos.getItemCount()==2);
            edt(()->{repos.setSelectedIndex(0); accounts.setSelectedIndex(0); button(window.getPanel(),"连接").doClick();});
            waitFor("A PR-only page advances to #16",()->list.getModel().getSize()==1 && list.getModel().getElementAt(0).getNumberValue()==16);
            if(trace.stream().anyMatch(s->s.contains("/comments"))) throw new AssertionError("Unexpected comment GET for list");
            edt(()->list.setSelectedIndex(0));
            if(!enteredA.await(7,TimeUnit.SECONDS)) throw new AssertionError("A detail and comments did not both start");
            traceView("A blocked",project);
            edt(()->{repos.setSelectedIndex(1);accounts.setSelectedIndex(1);button(window.getPanel(),"连接").doClick();});
            waitFor("B PR-only page advances to #17",()->list.getModel().getSize()==1 && list.getModel().getElementAt(0).getNumberValue()==17);
            traceView("B connected before explicit selection",project);
            edt(()->list.setSelectedIndex(0)); waitFor("B detail and comment completed while A is blocked",this::showingB);
            edt(()->((JTextArea)field(field(window,"editor"),"editor")).setText("Beta draft 17"));
            traceView("B manually selected",project);
            releaseA.countDown();
            if(!returnedA.await(6,TimeUnit.SECONDS)) throw new AssertionError("A did not finish");
            // Drain asynchronous UI work after both official request decoders returned.
            for(int i=0;i<12;i++){Thread.sleep(40);edt(()->{});}
            waitFor("A returned; B title, body, author comment and count still intact",this::showingB);
            traceView("A settled after B",project);
            hold=false;
            edt(()->filter.setSelectedItem(IssueFilter.CLOSED));
            waitFor("closed filter follows PR-only page",()->list.getModel().getSize()==1 && list.getModel().getElementAt(0).isClosed());
            JTextField query=field(window,"query"); edt(()->query.setText("#4"));
            waitFor("exact number #4 does not match #17",()->list.getModel().getSize()==0);
            edt(()->query.setText("#17")); waitFor("exact number #17 matches",()->list.getModel().getSize()==1);
            var props=PropertiesComponent.getInstance(project);
            if(!b.getCanonical().equals(props.getValue("issueReview.repo")) || !"two".equals(props.getValue("issueReview.account"))) throw new AssertionError("Wrong saved selection");
            edt(()->button(window.getPanel(),"重新检测").doClick());
            waitFor("rediscovery connected saved account two and loaded Beta list",()->list.getModel().getSize()==1 && ((RepoChoice)repos.getSelectedItem()).getKey().equals(b) && "two".equals(((AccountChoice)accounts.getSelectedItem()).getId()) && trace.stream().filter(s->s.equals("GET two /user?")).count()==2 && field(window,"session")!=null);
            Thread.sleep(500); traceView("after explicit rediscovery",project);
            boolean[] restored={false};
            edt(()->restored[0]=showingB() && ((JTextArea)field(field(window,"editor"),"editor")).getText().equals("Beta draft 17"));
            trace.add("same-window selected B survived rediscovery="+restored[0]);
            if(trace.stream().filter(s->s.startsWith("GET one /repos/qa/alpha/issues/16?")).count()!=1) throw new AssertionError("unexpected duplicate A detail");
            edt(()->Disposer.dispose(window));project.save();
            edt(()->{if(!projects.closeAndDispose(project)) throw new AssertionError("Native close failed");});
            Project reopened=projects.loadAndOpenProject(root.resolve("Window-QA").toString());
            openReplay(reopened);
            waitFor("closed project reopened and remembered account, repo, Issue B, latest draft",()->showingB() && ((JTextArea)field(field(window,"editor"),"editor")).getText().equals("Beta draft 17"));
            traceView("after normal project close and reopen",reopened);
            JComboBox<?> reopenedFilter=field(window,"filter");
            JList<IssueReviewTask> reopenedList=field(window,"list");
            edt(()->reopenedFilter.setSelectedItem(IssueFilter.OPEN));
            waitFor("open filter requests state=open and displays open #17 after PR-only page",()->trace.stream().anyMatch(s->s.equals("GET two /repos/qa/beta/issues?state=open&per_page=50&page=2")) && reopenedList.getModel().getSize()==1 && !reopenedList.getModel().getElementAt(0).isClosed() && reopenedList.getModel().getElementAt(0).getNumberValue()==17);
            edt(()->Disposer.dispose(window));
            edt(()->{if(!projects.closeAndDispose(reopened)) throw new AssertionError("Native close failed");});
            trace.add(restored[0]?"PASS":"FAIL: explicit same-window rediscovery did not preserve B selection");
        } catch(Throwable e) {
            var out=new StringWriter();e.printStackTrace(new PrintWriter(out));trace.add(out.toString());trace.add("FAIL");
        } finally {
            releaseA.countDown();
            try {Files.write(root.resolve("result.txt"),trace);}catch(Exception e){e.printStackTrace();}
            app.invokeLater(app::exit);
        }
    }
}
