package ru.sergeydev.telegramminiappshop.payment.dto;

import ru.sergeydev.telegramminiappshop.payment.entity.PaymentStatus;

public record CreatePaymentResponseDto(
        Long paymentId,
        PaymentStatus status,
        String confirmationUrl
) {
}