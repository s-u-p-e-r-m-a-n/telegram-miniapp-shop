package ru.sergeydev.telegramminiappshop.telegram.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sergeydev.telegramminiappshop.common.exception.UnauthorizedException;
import ru.sergeydev.telegramminiappshop.telegram.config.TelegramBotProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
@RequiredArgsConstructor
public class TelegramInitDataValidator {

    private static final long INIT_DATA_MAX_AGE_SECONDS = 24 * 60 * 60;
    private final TelegramBotProperties properties;
    private final ObjectMapper objectMapper;

    public boolean validate(String initData) {

        if (initData == null || initData.isBlank()) {
            return false;
        }

        Map<String, String> data = parseInitData(initData);

        String telegramHash = data.get("hash");

        if (telegramHash == null || telegramHash.isBlank()) {
            return false;
        }

        String dataCheckString = buildDataCheckString(data);

        String expectedHash = calculateExpectedHash(dataCheckString);
        boolean hashValid = MessageDigest.isEqual(
                telegramHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );

        if (!hashValid) {
            return false;
        }

        return isAuthDateValid(data);

    }

    public Long extractTelegramUserId(String initData) {

        if (!validate(initData)) {
            throw new UnauthorizedException(
                    "Некорректные Telegram initData"
            );
        }

        Map<String, String> data = parseInitData(initData);

        String userJson = data.get("user");

        if (userJson == null || userJson.isBlank()) {
            throw new UnauthorizedException(
                    "Telegram user отсутствует в initData"
            );
        }

        try {
            JsonNode userNode = objectMapper.readTree(userJson);

            JsonNode idNode = userNode.get("id");

            if (idNode == null || !idNode.canConvertToLong()) {
                throw new UnauthorizedException(
                        "Telegram user ID отсутствует или имеет неверный формат"
                );
            }

            return idNode.asLong();

        } catch (JsonProcessingException e) {
            throw new UnauthorizedException(
                    "Некорректный формат Telegram user"
            );
        }
    }

    //преобразование сырой строки инициализации telegram в Map<String, String>
    private Map<String, String> parseInitData(String initData) {
        return Arrays.stream(initData.split("&"))
                .map(param -> param.split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(
                        parts -> URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                        parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                ));
    }

    // получаем корректную строку для проверки
    private String buildDataCheckString(Map<String, String> data) {
        return data.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("hash"))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    private String calculateExpectedHash(String dataCheckString) {

        byte[] secretKey = hmacSha256(
                //"WebAppData" - для вычисления ключа по условиям ТГ
                // подставляется и на стороне бека и на стороне ТГ одна и та же строка
                "WebAppData".getBytes(StandardCharsets.UTF_8),
                properties.token().getBytes(StandardCharsets.UTF_8)
        );

        byte[] hash = hmacSha256(
                secretKey,
                dataCheckString.getBytes(StandardCharsets.UTF_8)
        );

        return HexFormat.of().formatHex(hash);
    }

    private byte[] hmacSha256(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(
                    new SecretKeySpec(key, "HmacSHA256")
            );
            // берет data вычисляет с полученным ключем HMAC и возвращает итоговый рез-т
            return mac.doFinal(data);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Не удалось вычислить HMAC-SHA256",
                    e
            );
        }
    }
    // валидация  времени через INIT_DATA_MAX_AGE_SECONDS(24 часа в секундах)
    private boolean isAuthDateValid(Map<String, String> data) {

        String authDateValue = data.get("auth_date");

        if (authDateValue == null || authDateValue.isBlank()) {
            return false;
        }

        try {
            long authDate = Long.parseLong(authDateValue);
            long now = Instant.now().getEpochSecond();

            if (authDate > now) {
                return false;
            }

            return now - authDate <= INIT_DATA_MAX_AGE_SECONDS;

        } catch (NumberFormatException e) {
            return false;
        }
    }
}
