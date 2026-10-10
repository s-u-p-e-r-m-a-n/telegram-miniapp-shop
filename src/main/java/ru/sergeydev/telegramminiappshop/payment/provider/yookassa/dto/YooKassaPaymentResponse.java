package ru.sergeydev.telegramminiappshop.payment.provider.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YooKassaPaymentResponse(
        String id,
        String status,
        Amount amount,
        @JsonProperty("invoice_details")
        InvoiceDetails invoiceDetails
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(
            String value,
            String currency
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InvoiceDetails(
            String id
    ) {
    }
}