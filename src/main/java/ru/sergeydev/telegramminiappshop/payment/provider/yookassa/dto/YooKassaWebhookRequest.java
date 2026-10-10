package ru.sergeydev.telegramminiappshop.payment.provider.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YooKassaWebhookRequest(
        String type,
        String event,
        PaymentObject object
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentObject(
            String id,
            String status
    ) {
    }
}