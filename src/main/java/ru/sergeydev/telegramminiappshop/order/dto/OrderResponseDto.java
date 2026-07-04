package ru.sergeydev.telegramminiappshop.order.dto;

import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderResponseDto(
        Long id,                 // id созданного заказа
        OrderStatus status,      // статус заказа
        BigDecimal totalAmount,  // итоговая сумма
        OffsetDateTime createdAt // дата создания
) {
}