package ru.sergeydev.telegramminiappshop.telegram.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.sergeydev.telegramminiappshop.order.event.OrderStatusChangedEvent;
import ru.sergeydev.telegramminiappshop.telegram.service.TelegramNotificationService;
import ru.sergeydev.telegramminiappshop.telegram.service.TelegramOrderService;

@Component
@RequiredArgsConstructor
public class TelegramOrderStatusChangedListener {

    private final TelegramOrderService telegramOrderService;
    private final TelegramNotificationService telegramNotificationService;

    //слушатель событий,слушаем транзакцию,если транзакция зафиксирована метод выполняется
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderStatusChangedEvent event) {

        telegramOrderService.findChatIdByOrderId(event.orderId())
                .ifPresent(chatId ->
                        telegramNotificationService
                                .sendOrderStatusChangedToCustomer(
                                        event.orderId(),
                                        chatId,
                                        event.status()
                                )
                );
    }
}