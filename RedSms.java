import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class RedSms {
    public static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Задайте " + name);
        return value;
    }

    public static void request(String method, String path, Map<String, String> fields) throws Exception {
        boolean send = method.equals("POST");
        Map<String, String> sortedFields = new TreeMap<>(fields);
        String form = sortedFields.entrySet().stream()
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        String ts = UUID.randomUUID().toString();
        String input = String.join("", sortedFields.values()) + ts + required("REDSMS_API_KEY");
        byte[] digest = MessageDigest.getInstance("MD5").digest(input.getBytes(StandardCharsets.UTF_8));
        String url = "https://cp.redsms.ru/api/" + path;
        if (!send && !form.isEmpty()) url += "?" + form;
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("login", required("REDSMS_LOGIN"))
            .header("ts", ts)
            .header("sig", HexFormat.of().formatHex(digest));
        if (send) {
            request.header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form));
        }
        HttpResponse<String> response = HttpClient.newHttpClient().send(
            request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }
        System.out.println(response.body());
    }
}
