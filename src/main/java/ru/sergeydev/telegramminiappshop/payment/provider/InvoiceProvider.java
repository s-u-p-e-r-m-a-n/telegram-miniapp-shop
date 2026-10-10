package ru.sergeydev.telegramminiappshop.payment.provider;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

//интерфейс создания и получения инвойса с описанием дто в интерфейсе
public interface InvoiceProvider {

   // создаёт счёт, возвращает его ID и ссылку.
    CreatedInvoice createInvoice(
            Long orderId,
            BigDecimal amount,
            List<InvoiceItem> items,
            OffsetDateTime expiresAt,
            UUID idempotencyKey
    );
   //получает состояние счёта и ID связанного платежа, если он уже известен
    InvoiceInfo getInvoice(String providerInvoiceId);

    record InvoiceItem(
            String name,
            BigDecimal price,
            int quantity
    ) {}

    record CreatedInvoice(
            String providerInvoiceId,
            String paymentUrl
    ) {}

    record InvoiceInfo(
            String providerInvoiceId,
            InvoiceStatus status,
            String providerPaymentId
    ) {}

    enum InvoiceStatus {
        PENDING,
        SUCCEEDED,
        CANCELLED
    }
}