package ru.sergeydev.telegramminiappshop.payment.provider;

import ru.sergeydev.telegramminiappshop.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {

    CreatedPayment createPayment(
            Long orderId,
            BigDecimal amount,
            UUID idempotencyKey
    );


    //содержит id платежного сервиса/шлюза и url для оплаты
    record CreatedPayment(
            String providerPaymentId,
            String confirmationUrl
    ) {
    }
    //показывает реальное текущее состояние уже существующего платежа.
    PaymentInfo getPayment(String providerPaymentId);
    record PaymentInfo(
            String providerPaymentId,
            PaymentStatus status,
            BigDecimal amount,
            String currency,
            String providerInvoiceId
    ) {
    }


}