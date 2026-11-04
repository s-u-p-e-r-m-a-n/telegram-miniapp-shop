package ru.sergeydev.telegramminiappshop.product.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.sergeydev.telegramminiappshop.common.exception.BadRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ProductImageStorageService {

    private final Path uploadDirectory;

    public ProductImageStorageService(
            @Value("${app.upload-dir}") String uploadDirectory
    ) {
        this.uploadDirectory = Path.of(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл изображения пустой");
        }

        String extension = switch (file.getContentType() == null
                ? ""
                : file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BadRequestException(
                    "Поддерживаются изображения JPG, PNG и WebP"
            );
        };

        String fileName = UUID.randomUUID() + extension;
        Path targetPath = uploadDirectory.resolve(fileName);

        try {
            Files.createDirectories(uploadDirectory);
            file.transferTo(targetPath);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Не удалось сохранить изображение",
                    exception
            );
        }

        return "/uploads/" + fileName;
    }
}