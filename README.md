# RED SMS · Java

В репозитории три самостоятельных примера. Каждый можно скачать и запустить отдельно. Требуется JDK 17+. Для разбора ответа на отправку используется Gson.

Укажите логин и API-ключ из личного кабинета в переменных окружения `REDSMS_LOGIN` и `REDSMS_API_KEY`. Все команды запускаются из корня репозитория.

## Информация об аккаунте · GET /api/client/info

```sh
java ClientInfo.java
```

При успехе API возвращает JSON с объектом `info`. Все три примера используют заголовки `login`, `ts` и `sig`. Подпись считается по значениям параметров в порядке их имён, затем добавляются `ts` и ключ.

## Отправка SMS · POST /api/message

Задайте в окружении `REDSMS_FROM` (зарегистрированное имя отправителя) и `REDSMS_TO` (свой номер в международном формате). Команда отправляет одно платное SMS с текстом «Тестовое сообщение от redsms.ru».

```sh
curl -fLo gson.jar https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.13.2/gson-2.13.2.jar
java -cp gson.jar SendSms.java
```

При HTTP 200 сохраните UUID каждого принятого сообщения из `items`. В `errors` могут одновременно быть причины отказа для других номеров.

## Статус SMS · GET /api/message/{uuid}

После отправки возьмите UUID из `items[0].uuid`. Передайте его в запрос статуса и прочитайте `item.status`: значение `delivered` подтверждает доставку.

```sh
java MessageStatus.java UUID
```

При `created`, `moderation` и `progress` можно позже повторить только команду статуса. Для SMS статус `delivered` подтверждает доставку. При HTTP 420 проверьте баланс, при 401 — подпись и разрешённые IP. При тайм-ауте запроса отправки автоматический повтор может создать дубликат.

[Справочник отправки SMS](https://docs.redsms.ru/http/api/post-message/) · [Статусы](https://docs.redsms.ru/http/api/message/) · [Документация HTTP API](https://docs.redsms.ru/http/)
