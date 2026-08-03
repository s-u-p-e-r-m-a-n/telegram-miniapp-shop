package ru.sergeydev.telegramminiappshop.admin.dto;

import jakarta.validation.constraints.NotNull;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

public record UpdateOrderStatusRequestDto(
        @NotNull(message = "Статус заказа обязателен")
        OrderStatus status // новый статус заказа
) {
}