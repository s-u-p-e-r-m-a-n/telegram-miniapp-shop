package ru.sergeydev.telegramminiappshop.payment.dto;

import ru.sergeydev.telegramminiappshop.payment.provider.InvoiceProvider;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


//Данные для создания счета
public record InvoiceCreationData(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        List<InvoiceProvider.InvoiceItem> items,
        OffsetDateTime expiresAt,
        UUID idempotencyKey
) {
    public InvoiceCreationData {
        items = List.copyOf(items);
    }
}