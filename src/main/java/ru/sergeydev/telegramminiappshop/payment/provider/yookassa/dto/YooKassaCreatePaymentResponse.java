package ru.sergeydev.telegramminiappshop.payment.provider.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)//игнорирует дополнительные поля которые могут быть в ответе
public record YooKassaCreatePaymentResponse(

        String id,

        String status,

        Confirmation confirmation//подверждение
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Confirmation(

            String type,

            @JsonProperty("confirmation_url")
            String confirmationUrl
    ) {
    }
}