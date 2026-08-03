package ru.sergeydev.telegramminiappshop.common.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ValidationErrorResponseDto(
        OffsetDateTime timestamp,
        int status,
        String error,
        Map<String, String> fieldErrors,
        String path
) {
}