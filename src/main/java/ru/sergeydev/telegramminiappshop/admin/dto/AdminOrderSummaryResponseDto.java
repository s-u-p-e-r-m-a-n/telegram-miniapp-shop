package ru.sergeydev.telegramminiappshop.admin.dto;


import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AdminOrderSummaryResponseDto(
        Long id,
        OffsetDateTime createdAt,
        String customerName,
        String customerPhone,
        BigDecimal totalAmount,
        OrderStatus status
) {
}
