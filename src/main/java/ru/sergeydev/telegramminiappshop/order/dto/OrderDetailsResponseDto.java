package ru.sergeydev.telegramminiappshop.order.dto;

import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDetailsResponseDto(
        Long id,                         // id заказа
        OrderStatus status,              // статус заказа
        BigDecimal totalAmount,          // итоговая сумма заказа
        OffsetDateTime createdAt,        // дата создания
        List<OrderItemResponseDto> items // товары внутри заказа
) {
}