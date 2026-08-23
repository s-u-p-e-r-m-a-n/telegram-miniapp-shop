package ru.sergeydev.telegramminiappshop.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSendMessageRequestDto(

        @NotBlank(message = "Сообщение не должно быть пустым")
        @Size(max = 2000, message = "Сообщение не должно превышать 2000 символов")
        String message
) {
}
