package ru.sergeydev.telegramminiappshop.admin.dto;

import ru.sergeydev.telegramminiappshop.order.dto.OrderItemResponseDto;
import ru.sergeydev.telegramminiappshop.order.entity.OrderSource;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record AdminOrderDetailsResponseDto(
        Long id,
        OrderSource source,

        String customerName,             // имя клиента
        String customerPhone,            // телефон клиента
        String customerComment,          // комментарий клиента

        OrderStatus status,              // статус заказа
        BigDecimal totalAmount,          // итоговая сумма

        OffsetDateTime createdAt,        // дата создания
        OffsetDateTime updatedAt,        // дата обновления

        List<OrderItemResponseDto> items // товары внутри заказа
) {
}