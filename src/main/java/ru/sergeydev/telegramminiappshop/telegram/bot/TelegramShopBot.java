package ru.sergeydev.telegramminiappshop.telegram.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.sergeydev.telegramminiappshop.telegram.config.TelegramBotProperties;
import ru.sergeydev.telegramminiappshop.telegram.service.TelegramUserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramShopBot implements SpringLongPollingBot,
        LongPollingSingleThreadUpdateConsumer {

    private final TelegramBotProperties properties;
    private final TelegramUserService telegramUserService;
    private final TelegramClient telegramClient;

    @Override
    public String getBotToken() {
        return properties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            Long telegramUserId = update.getMessage().getFrom().getId();
            Long telegramChatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            if ("/start".equals(text)) {

                telegramUserService.saveOrUpdate(
                        telegramUserId,
                        telegramChatId
                );
                SendMessage message = SendMessage.builder()
                        .chatId(telegramChatId)
                        .text("Добро пожаловать! Скоро здесь будет доступен магазин.")
                        .build();

                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    log.error("Failed to send start message", e);
                }
                log.info(
                        "Telegram update: userId={}, chatId={}, text={}",
                        telegramUserId,
                        telegramChatId,
                        text
                );
            }
        }
    }
}
