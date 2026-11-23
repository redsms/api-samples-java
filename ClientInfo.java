import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

public class ClientInfo {
    public static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Задайте " + name);
        return value;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            throw new IllegalArgumentException("Использование: java ClientInfo.java");
        }
        String url = "https://cp.redsms.ru/api/client/info";
        String values = "";
        String ts = UUID.randomUUID().toString();
        String input = values + ts + required("REDSMS_API_KEY");
        byte[] digest = MessageDigest.getInstance("MD5").digest(input.getBytes(StandardCharsets.UTF_8));
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("login", required("REDSMS_LOGIN"))
            .header("ts", ts)
            .header("sig", HexFormat.of().formatHex(digest));
            HttpResponse<String> response = HttpClient.newHttpClient().send(
            request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }
        System.out.println(response.body());
    }
}
