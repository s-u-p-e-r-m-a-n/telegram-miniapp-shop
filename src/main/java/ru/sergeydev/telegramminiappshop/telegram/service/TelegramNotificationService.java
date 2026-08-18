package ru.sergeydev.telegramminiappshop.telegram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.telegram.config.TelegramBotProperties;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService {

    private final TelegramClient telegramClient;
    private final TelegramBotProperties properties;

    public void sendOrderCreatedToCustomer(Order order) {

        String text = """
                Ваш заказ №%d успешно оформлен.

                Итого: %s ₽
                Статус: %s

                Менеджер свяжется с вами для подтверждения.
                """.formatted(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus()
        );

        SendMessage message = SendMessage.builder()
                .chatId(order.getTelegramChatId())
                .text(text)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error(
                    "Failed to send order created notification, orderId={}",
                    order.getId(),
                    e
            );
        }
    }
    public void sendOrderCreatedToAdmin(Order order) {

        String text = """
            Новый заказ №%d

            Клиент: %s
            Телефон: %s
            Сумма: %s ₽
            Статус: %s
            """.formatted(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getTotalAmount(),
                order.getStatus()
        );

        SendMessage message = SendMessage.builder()
                .chatId(properties.adminChatId())
                .text(text)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error(
                    "Failed to send order notification to admin, orderId={}",
                    order.getId(),
                    e
            );
        }
    }
    public void sendOrderStatusChangedToCustomer( Long orderId,
                                                  Long telegramChatId,
                                                  OrderStatus status) {

        String statusText = switch (status) {
            case NEW -> "Новый";
            case IN_WORK -> "Принят в работу";
            case DONE -> "Выполнен";
            case CANCELLED -> "Отменён";
        };

        String text = """
            Статус заказа №%d изменён.

            Новый статус: %s
            """.formatted(
                orderId,
                statusText
        );

        SendMessage message = SendMessage.builder()
                .chatId(telegramChatId)
                .text(text)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error(
                    "Failed to send order status notification, orderId={}",
                    orderId,
                    e
            );
        }
    }
}