package ru.sergeydev.telegramminiappshop.product.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class UploadDirectoryConfig {

    @Bean
    public Path productImageUploadDirectory(
            @Value("${app.upload-dir}") String directory
    ) {
        return Path.of(directory).toAbsolutePath().normalize();
    }
}