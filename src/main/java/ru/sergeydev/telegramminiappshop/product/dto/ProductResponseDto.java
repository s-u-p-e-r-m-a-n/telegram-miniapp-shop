package ru.sergeydev.telegramminiappshop.product.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
        Long id,                 // id товара
        Long categoryId,         // id категории
        String categoryName,     // название категории
        String name,             // название товара
        String description,      // описание товара
        BigDecimal price,        // цена товара
        String imageUrl,         // ссылка на фото
        Boolean featured,        // показывать в "Рекомендуем"
        Integer stockQuantity,   // остаток товара
        Boolean trackStock       // учитывать остатки или нет
) {
}