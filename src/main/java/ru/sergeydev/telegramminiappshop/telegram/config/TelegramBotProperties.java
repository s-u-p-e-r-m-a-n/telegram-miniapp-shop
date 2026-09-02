package ru.sergeydev.telegramminiappshop.telegram.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramBotProperties(
        String token,
        Long adminChatId,
        String webAppUrl
) {
}