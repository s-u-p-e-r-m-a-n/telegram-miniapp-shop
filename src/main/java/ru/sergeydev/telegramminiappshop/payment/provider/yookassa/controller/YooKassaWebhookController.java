package ru.sergeydev.telegramminiappshop.payment.provider.yookassa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.payment.provider.yookassa.dto.YooKassaWebhookRequest;
import ru.sergeydev.telegramminiappshop.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments/yookassa")
@RequiredArgsConstructor
public class YooKassaWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody YooKassaWebhookRequest request
    ) {

        if (!"notification".equals(request.type())) {
            return ResponseEntity.badRequest().build();
        }
//корректный запрос, но не успешная оплата подтверждаем получение, но игнор
        if (!"payment.succeeded".equals(request.event())) {
            return ResponseEntity.ok().build();
        }

        if (request.object() == null
                || request.object().id() == null) {
            return ResponseEntity.badRequest().build();
        }

        paymentService.markPaymentAsPaid(
                request.object().id()
        );

        return ResponseEntity.ok().build();
    }
}