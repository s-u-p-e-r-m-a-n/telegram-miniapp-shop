package ru.sergeydev.telegramminiappshop.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.sergeydev.telegramminiappshop.telegram.service.TelegramNotificationService;

@Component
@RequiredArgsConstructor
public class OrderStatusChangedListener {

    private final TelegramNotificationService telegramNotificationService;
//слушатель событий,слушаем транзакцию,если транзакция зафиксирована метод выполняется
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderStatusChangedEvent event) {

        telegramNotificationService.sendOrderStatusChangedToCustomer(
                event.orderId(),
                event.telegramChatId(),
                event.status()
        );
    }
}