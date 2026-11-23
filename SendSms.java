import java.util.Map;

public class SendSms {
    public static void main(String[] args) throws Exception {
        if (args.length != 0) throw new IllegalArgumentException("Использование: java SendSms");
        RedSms.request("POST", "message", Map.of(
            "route", "sms",
            "from", RedSms.required("REDSMS_FROM"),
            "to", RedSms.required("REDSMS_TO"),
            "text", "Тестовое сообщение от redsms.ru"
        ));
    }
}
