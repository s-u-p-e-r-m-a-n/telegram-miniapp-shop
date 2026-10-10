package ru.sergeydev.telegramminiappshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.sergeydev.telegramminiappshop.telegram.config.TelegramBotProperties;
import ru.sergeydev.telegramminiappshop.telegram.security.AdminSecurityProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan//сканирует классы конфигураций
@EnableConfigurationProperties({TelegramBotProperties.class, AdminSecurityProperties.class})
public class TelegramMiniappShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramMiniappShopApplication.class, args);
    }

}
