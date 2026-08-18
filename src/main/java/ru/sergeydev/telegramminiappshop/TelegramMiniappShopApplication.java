package ru.sergeydev.telegramminiappshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.sergeydev.telegramminiappshop.telegram.config.TelegramBotProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramMiniappShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramMiniappShopApplication.class, args);
    }

}
