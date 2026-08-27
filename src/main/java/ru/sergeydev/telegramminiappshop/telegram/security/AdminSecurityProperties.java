package ru.sergeydev.telegramminiappshop.telegram.security;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminSecurityProperties(
        String username,
        String password
) {
}