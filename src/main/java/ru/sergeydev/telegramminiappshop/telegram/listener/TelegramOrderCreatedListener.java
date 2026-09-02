package ru.sergeydev.telegramminiappshop.telegram.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.sergeydev.telegramminiappshop.order.event.OrderCreatedEvent;
import ru.sergeydev.telegramminiappshop.telegram.service.TelegramOrderService;

@Component
@RequiredArgsConstructor
public class TelegramOrderCreatedListener {

    private final TelegramOrderService telegramOrderService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCreatedEvent event) {

        telegramOrderService.sendOrderCreatedNotifications(
                event.orderId()
        );
    }
}