package ru.sergeydev.telegramminiappshop.order.dto;

import java.util.List;

public record CreateOrderRequestDto(
        Long telegramUserId,       // временно принимаем с frontend
        Long telegramChatId,       // id чата для уведомлений
        String customerName,       // имя клиента
        String customerPhone,      // телефон клиента
        String customerComment,    // комментарий клиента
        List<CreateOrderItemRequestDto> items // товары в заказе
) {
}