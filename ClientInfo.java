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

class ClientInfo {
    static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Задайте " + name);
        return value;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> fields = new TreeMap<>();
        String path = "client/info";
        boolean send = args.length == 1 && args[0].equals("send");
        if (send) {
            path = "message";
            fields.put("route", "sms");
            fields.put("from", required("REDSMS_FROM"));
            fields.put("to", required("REDSMS_TO"));
            fields.put("text", "Тестовое сообщение от redsms.ru");
        } else if (args.length == 2 && args[0].equals("status")
                && UUID.fromString(args[1]).toString().equalsIgnoreCase(args[1])) {
            path = "message/" + args[1];
            fields.put("fields", "uuid,status,errCode");
        } else if (args.length != 0) {
            throw new IllegalArgumentException("Использование: java ClientInfo.java [send | status UUID]");
        }

        String form = fields.entrySet().stream()
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        String ts = UUID.randomUUID().toString();
        String input = String.join("", fields.values()) + ts + required("REDSMS_API_KEY");
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
