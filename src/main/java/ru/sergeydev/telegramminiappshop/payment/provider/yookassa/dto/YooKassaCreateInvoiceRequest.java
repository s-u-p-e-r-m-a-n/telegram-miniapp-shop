package ru.sergeydev.telegramminiappshop.payment.provider.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record YooKassaCreateInvoiceRequest(
        @JsonProperty("payment_data")
        PaymentData paymentData,

        List<CartItem> cart,

        @JsonProperty("delivery_method_data")
        DeliveryMethodData deliveryMethodData,

        @JsonProperty("expires_at")
        String expiresAt,

        String description,
        Map<String, String> metadata
) {

    public record PaymentData(
            Amount amount,
            boolean capture,
            String description,
            Map<String, String> metadata
    ) {}

    public record Amount(
            String value,
            String currency
    ) {}

    public record CartItem(
            String description,
            Amount price,
            BigDecimal quantity
    ) {}

    public record DeliveryMethodData(
            String type
    ) {}
}