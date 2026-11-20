import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

class ClientInfo {
    public static void main(String[] args) throws Exception {
        String login = System.getenv("REDSMS_LOGIN");
        String key = System.getenv("REDSMS_API_KEY");
        if (login == null || login.isBlank() || key == null || key.isBlank()) {
            throw new IllegalArgumentException("Задайте REDSMS_LOGIN и REDSMS_API_KEY");
        }
        String ts = UUID.randomUUID().toString();
        byte[] digest = MessageDigest.getInstance("MD5").digest((ts + key).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://cp.redsms.ru/api/client/info"))
            .timeout(Duration.ofSeconds(15))
            .header("login", login)
            .header("ts", ts)
            .header("sig", HexFormat.of().formatHex(digest))
            .GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }
        System.out.println(response.body());
    }
}
