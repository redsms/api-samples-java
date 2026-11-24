import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.List;
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

public class SendSms {
    record Message(String uuid) {}
    record SendResult(boolean success, List<Message> items, List<JsonElement> errors) {}

    public static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Задайте " + name);
        return value;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            throw new IllegalArgumentException("Использование: java -cp gson.jar SendSms.java");
        }
        String url = "https://cp.redsms.ru/api/message";
        String recipient = required("REDSMS_TO");
        if (!recipient.matches("[+]?[1-9][0-9]{1,14}")) throw new IllegalArgumentException("Укажите один номер в международном формате, например +12025550123.");
        Map<String, String> fields = new TreeMap<>(Map.of(
            "from", required("REDSMS_FROM"),
            "route", "sms",
            "text", "Тестовое сообщение от redsms.ru",
            "to", recipient
        ));
        String values = String.join("", fields.values());
        String form = fields.entrySet().stream()
            .map(field -> field.getKey() + "=" + URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));
        String ts = UUID.randomUUID().toString();
        String input = values + ts + required("REDSMS_API_KEY");
        byte[] digest = MessageDigest.getInstance("MD5").digest(input.getBytes(StandardCharsets.UTF_8));
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("login", required("REDSMS_LOGIN"))
            .header("ts", ts)
            .header("sig", HexFormat.of().formatHex(digest));
        request.header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(form));
        HttpResponse<String> response = HttpClient.newHttpClient().send(
            request.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }
        SendResult result = new Gson().fromJson(response.body(), SendResult.class);
        if (result == null) throw new IllegalStateException("Пустой ответ API");
        if (result.errors() != null) result.errors().forEach(System.err::println);
        if (result.items() == null || result.items().isEmpty()) {
            throw new IllegalStateException("Сервер не вернул принятых сообщений");
        }
        for (Message item : result.items()) {
            if (item == null || item.uuid() == null || item.uuid().isBlank()) {
                throw new IllegalStateException("В ответе отсутствует UUID сообщения");
            }
            System.out.println(item.uuid());
        }
        if (!result.success() || (result.errors() != null && !result.errors().isEmpty())) {
            System.exit(1);
        }
    }
}
