package ru.sergeydev.telegramminiappshop.order.dto;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long productId,             // id товара из каталога
        String productName,         // название товара на момент заказа
        BigDecimal productPrice,    // цена товара на момент заказа
        Integer quantity,           // количество
        BigDecimal totalPrice       // сумма по позиции: цена * количество
) {
}