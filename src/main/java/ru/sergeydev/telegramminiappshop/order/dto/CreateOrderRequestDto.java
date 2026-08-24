package ru.sergeydev.telegramminiappshop.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequestDto(

        @NotBlank(message = "Имя клиента обязательно")
        String customerName,       // имя клиента
        @NotBlank(message = "Телефон клиента обязателен")
        String customerPhone,      // телефон клиента
        @Size(
                max = 1000,
                message = "Комментарий не должен превышать 1000 символов"
        )
        String customerComment,    // комментарий клиента

        @NotEmpty(message = "Заказ должен содержать хотя бы один товар")
        @Valid
        List<CreateOrderItemRequestDto> items // товары в заказе
) {
}