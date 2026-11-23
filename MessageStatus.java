import java.util.Map;

public class MessageStatus {
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !args[0].matches("(?i)[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}")) {
            throw new IllegalArgumentException("Использование: java MessageStatus UUID");
        }
        RedSms.request("GET", "message/" + args[0], Map.of("fields", "uuid,status,errCode"));
    }
}
