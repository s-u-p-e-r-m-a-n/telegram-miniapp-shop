package ru.sergeydev.telegramminiappshop.common.error;

import java.time.OffsetDateTime;

public record ErrorResponseDto(
        OffsetDateTime timestamp, // время ошибки
        int status,               // HTTP статус: 400, 404, 500
        String error,             // название ошибки
        String message,           // понятное сообщение
        String path               // URL запроса
) {
}