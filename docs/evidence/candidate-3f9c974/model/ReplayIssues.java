import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import team.segroup11.review.model.ModelGateway;
import team.segroup11.review.model.ModelOptions;
import team.segroup11.review.model.ModelProfile;
import team.segroup11.review.model.ModelResult;
import team.segroup11.review.model.OutputMode;

public class ReplayIssues {
    public static void main(String[] args) throws Exception {
        ObjectMapper json = new ObjectMapper();
        JsonNode expected = json.readTree(Path.of(args[2]).toFile());
        JsonNode cases = json.readTree(Path.of(args[1]).toFile());
        ModelGateway gateway = new ModelGateway();
        ModelProfile profile = new ModelProfile(
            new ModelOptions(args[0], "success", OutputMode.JSON_OBJECT, 32768, 4096),
            "loopback-fixture"
        );
        Class<?>[] signature = {String.class, String.class, ModelProfile.class};
        Method evaluate = Arrays.stream(ModelGateway.class.getMethods())
            .filter(method -> method.getName().startsWith("evaluate"))
            .filter(method -> Arrays.equals(method.getParameterTypes(), signature))
            .findFirst().orElseThrow();

        ObjectNode actual = json.createObjectNode();
        actual.put("commit", expected.required("commit").textValue());
        actual.put("zipSha256", args[4]);
        var reports = actual.putArray("reports");
        for (JsonNode issue : cases) {
            var result = (ModelResult) evaluate.invoke(gateway, issue.required("title").textValue(),
                issue.required("body").textValue(), profile);
            var entry = reports.addObject();
            entry.put("issue", issue.required("url").textValue());
            entry.put("inputHash", result.getInputHash());
            entry.put("modelMode", "loopback/success/json_object");
            entry.set("validatedReport", json.readTree(result.getReportJson()));
        }
        Files.writeString(Path.of(args[3]), json.writerWithDefaultPrettyPrinter().writeValueAsString(actual) + "\n");
        if (!actual.equals(expected)) {
            throw new AssertionError("The reports differ from the recorded candidate output.");
        }
        System.out.println("PASS: both parsed reports and their input hashes match the recorded candidate output.");
    }
}
