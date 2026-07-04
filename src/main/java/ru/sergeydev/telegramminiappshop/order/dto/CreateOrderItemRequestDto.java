package ru.sergeydev.telegramminiappshop.order.dto;

public record CreateOrderItemRequestDto(
        Long productId,   // id товара
        Integer quantity  // количество
) {
}