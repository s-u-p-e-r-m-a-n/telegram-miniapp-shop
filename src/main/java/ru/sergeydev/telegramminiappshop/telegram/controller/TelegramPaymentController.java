package ru.sergeydev.telegramminiappshop.telegram.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.payment.dto.CreatePaymentResponseDto;
import ru.sergeydev.telegramminiappshop.payment.entity.Payment;
import ru.sergeydev.telegramminiappshop.payment.service.PaymentService;
import ru.sergeydev.telegramminiappshop.telegram.security.TelegramInitDataValidator;
import ru.sergeydev.telegramminiappshop.telegram.service.TelegramOrderService;

import java.util.UUID;

@RestController
@RequestMapping("/api/telegram/orders")
@RequiredArgsConstructor
public class TelegramPaymentController {

    private final TelegramInitDataValidator telegramInitDataValidator;
    private final TelegramOrderService telegramOrderService;
    private final PaymentService paymentService;

    @PostMapping("/{orderId}/payments")
    public CreatePaymentResponseDto createPayment(
            @PathVariable Long orderId,

            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData,

            @RequestHeader("Idempotency-Key")
            UUID idempotencyKey
    ) {

        Long telegramUserId = telegramInitDataValidator.extractTelegramUserId(initData);

        //бросается ошибка в случае отсутсвия
        telegramOrderService.validateOrderOwnership(
                orderId,
                telegramUserId
        );

        Payment payment = paymentService.createPayment(
                orderId,
                idempotencyKey
        );

        return new CreatePaymentResponseDto(
                payment.getId(),
                payment.getStatus(),
                payment.getConfirmationUrl()
        );
    }
}