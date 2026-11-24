package fixture;

import com.intellij.openapi.application.ApplicationStarter;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Probe implements ApplicationStarter {
    @Override public boolean isHeadless() { return true; }
    @Override public void main(List<String> args) {
        Path dir = Path.of(System.getProperty("review.probe.path"));
        List<String> record = new ArrayList<>();
        HttpServer http = null;
        int exit = 1;
        try {
            ClassLoader loader = Class.forName("team.segroup11.review.model.ModelGateway", true, getClass().getClassLoader()).getClassLoader();
            for (String cn : List.of("kotlin.Unit", "kotlin.reflect.jvm.internal.ReflectionFactoryImpl", "com.fasterxml.jackson.databind.ObjectMapper", "com.openai.client.okhttp.OpenAIOkHttpClient", "okhttp3.OkHttpClient")) {
                Class<?> c = Class.forName(cn, false, loader);
                record.add(cn + " -> " + c.getResource("/" + cn.replace('.', '/') + ".class") + " [" + c.getClassLoader().getClass().getName() + "]");
            }
            http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            AtomicInteger count = new AtomicInteger();
            byte[] response = Files.readAllBytes(dir.resolve("response.json"));
            http.createContext("/v1/chat/completions", e -> {
                int requestNumber = count.incrementAndGet();
                try (var request = e.getRequestBody()) {
                    Files.write(dir.resolve("request-" + requestNumber + ".json"), request.readAllBytes());
                }
                e.getResponseHeaders().set("Content-Type", "application/json");
                e.sendResponseHeaders(200, response.length);
                try(var out = e.getResponseBody()) { out.write(response); }
            });
            http.start();
            String p = "team.segroup11.review.model.";
            Class<?> mode = Class.forName(p+"OutputMode",true,loader);
            for (String requested : List.of("JSON_OBJECT","STRICT_SCHEMA")) {
                Object option = Class.forName(p+"ModelOptions",true,loader)
                    .getConstructor(String.class,String.class,mode,int.class,int.class)
                    .newInstance("http://127.0.0.1:"+http.getAddress().getPort()+"/v1","fixture",Enum.valueOf((Class)mode,requested),32768,4096);
                Object profile = Class.forName(p+"ModelProfile",true,loader)
                    .getConstructor(option.getClass(),String.class).newInstance(option,"fixture");
                Object gateway = Class.forName(p+"ModelGateway",true,loader).getConstructor().newInstance();
                var evaluate=Arrays.stream(gateway.getClass().getDeclaredMethods()).filter(m -> m.getName().startsWith("evaluate") && m.getParameterCount()==3).findFirst().orElseThrow();
                Object result=evaluate.invoke(gateway,Files.readString(dir.resolve("title.txt")),Files.readString(dir.resolve("body.txt")),profile);
                Object report=result.getClass().getMethod("getReport").invoke(result);
                Object kind=report.getClass().getMethod("getKind").invoke(report);
                if (!"feature".equals(kind)) throw new AssertionError("Unexpected kind: "+kind);
                record.add(requested+" -> feature");
            }
            if (count.get()!=2) throw new AssertionError("Requests: "+count);
            record.add("requests=2");
            record.add("PASS");
            exit=0;
        } catch(Throwable t) {
            java.io.StringWriter out = new java.io.StringWriter();
            t.printStackTrace(new java.io.PrintWriter(out));
            record.add(out.toString());
        } finally {
            if (http!=null) http.stop(0);
            try { Files.write(dir.resolve("result.txt"),record,StandardCharsets.UTF_8); } catch(Exception e) { e.printStackTrace(); }
            System.exit(exit);
        }
    }
}
