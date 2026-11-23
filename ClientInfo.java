import java.util.Arrays;
import java.util.Map;

public class ClientInfo {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            RedSms.request("GET", "client/info", Map.of());
        } else if (args[0].equals("send")) {
            SendSms.main(Arrays.copyOfRange(args, 1, args.length));
        } else if (args[0].equals("status")) {
            MessageStatus.main(Arrays.copyOfRange(args, 1, args.length));
        } else {
            throw new IllegalArgumentException("Использование: java ClientInfo");
        }
    }
}
