package ru.sergeydev.telegramminiappshop.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.yookassa")
public record YooKassaProperties(
        String shopId,
        String secretKey,
        String returnUrl
) {
}