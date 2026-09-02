package ru.sergeydev.telegramminiappshop.order.event;

import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

public record OrderStatusChangedEvent(
        Long orderId,
        OrderStatus status
) {
}
