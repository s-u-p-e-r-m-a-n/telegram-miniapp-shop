package ru.sergeydev.telegramminiappshop.payment.provider.yookassa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record YooKassaCreatePaymentRequest(

        Amount amount,

        boolean capture, //true сразу списание, false двухстадийная оплата сначала деньги заморозили,потом магазин отдельно подтверждает списание

        Confirmation confirmation, //описывает, как клиент будет подтверждать оплату.

        String description, //дополнительные данные например id заказа

        Map<String, String> metadata
) {

    public record Amount(
            String value,
            String currency
    ) {
    }

    public record Confirmation(
            String type, //redirect

            @JsonProperty("return_url") //на какой адрес после успешной оплаты
            String returnUrl
    ) {
    }
}