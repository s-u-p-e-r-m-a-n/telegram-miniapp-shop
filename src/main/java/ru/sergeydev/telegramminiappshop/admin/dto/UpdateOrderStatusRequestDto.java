package ru.sergeydev.telegramminiappshop.admin.dto;

import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

public record UpdateOrderStatusRequestDto(
        OrderStatus status // новый статус заказа
) {
}