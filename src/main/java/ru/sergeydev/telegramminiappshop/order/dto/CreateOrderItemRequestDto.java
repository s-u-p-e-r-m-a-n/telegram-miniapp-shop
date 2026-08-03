package ru.sergeydev.telegramminiappshop.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequestDto(
        @NotNull(message = "ID товара обязателен")
        Long productId,   // id товара
        @NotNull(message = "Количество товара обязательно")
        @Min(value = 1, message = "Количество товара должно быть не меньше 1")
        Integer quantity  // количество
) {
}