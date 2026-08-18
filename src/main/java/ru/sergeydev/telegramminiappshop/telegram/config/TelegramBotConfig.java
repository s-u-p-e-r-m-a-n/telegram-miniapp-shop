package ru.sergeydev.telegramminiappshop.telegram.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    private final TelegramBotProperties properties;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(properties.token());
    }
}