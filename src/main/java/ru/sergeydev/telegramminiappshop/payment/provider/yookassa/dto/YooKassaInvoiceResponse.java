package ru.sergeydev.telegramminiappshop.payment.provider.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YooKassaInvoiceResponse(
        String id,
        String status,

        @JsonProperty("delivery_method")
        DeliveryMethod deliveryMethod,

        @JsonProperty("payment_details")
        PaymentDetails paymentDetails,

        @JsonProperty("expires_at")
        String expiresAt
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeliveryMethod(
            String type,
            String url
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentDetails(
            String id,
            String status
    ) {}
}