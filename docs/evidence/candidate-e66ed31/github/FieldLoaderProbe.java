import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

class FieldLoaderProbe {
    public static void main(String[] args) throws Exception {
        URL[] shared = {Path.of(args[0]).toUri().toURL(), Path.of(args[1]).toUri().toURL()};
        try (var plugin = new URLClassLoader(shared, FieldLoaderProbe.class.getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("team.segroup11.review.github.") || name.startsWith("com.fasterxml.jackson.annotation.")) {
                    var prior = findLoadedClass(name);
                    return prior == null ? findClass(name) : prior;
                }
                return super.loadClass(name, resolve);
            }
        }) {
            if(plugin.loadClass("com.fasterxml.jackson.annotation.JsonProperty") == Class.forName("com.fasterxml.jackson.annotation.JsonProperty")) {
                throw new IllegalStateException("Expected isolated plugin annotations");
            }
            var decode = Class.forName("org.jetbrains.plugins.github.api.GithubApiContentHelper")
                .getMethod("fromJson",String.class,Class.class,boolean.class);
            for (String type : new String[]{"IssueWire","CommentWire"}) {
                var wire = plugin.loadClass("team.segroup11.review.github."+type);
                String fixture = type.equals("IssueWire")
                    ? "{\"number\":17,\"title\":\"Export\",\"state\":\"open\",\"user\":{\"login\":\"segroup11-backlog[bot]\"},\"created_at\":\"2026-11-24T06:11:34Z\",\"updated_at\":\"2026-11-24T08:08:49Z\"}"
                    : "{\"id\":7125780145,\"user\":{\"login\":\"material4002\"},\"created_at\":\"2026-11-24T08:08:49Z\",\"body\":\"test\"}";
                var result = decode.invoke(null, fixture, wire, false);
                boolean old = args[2].equals("old");
                Object author = wire.getMethod(old?"getOptionalAuthor":"getUser").invoke(result);
                Object time = wire.getMethod(old?"getOptionalCreated":"getCreatedAt").invoke(result);
                String login = author==null ? null : (String) author.getClass().getMethod("getLogin").invoke(author);
                String created = time == null ? null : ((java.util.Date) time).toInstant().toString();
                System.out.println(type+" login="+login+" created="+created);
                String expectedLogin = type.equals("IssueWire") ? "segroup11-backlog[bot]" : "material4002";
                String expectedDate = type.equals("IssueWire") ? "2026-11-24T06:11:34Z" : "2026-11-24T08:08:49Z";
                if (old ? login != null || created != null : !expectedLogin.equals(login) || !expectedDate.equals(created)) {
                    throw new AssertionError(type);
                }
            }
        }
    }
}
