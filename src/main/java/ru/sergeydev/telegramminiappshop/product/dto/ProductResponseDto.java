package ru.sergeydev.telegramminiappshop.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDto(
        Long id,                 // id товара
        Long categoryId,         // id категории
        String categoryName,     // название категории
        String name,             // название товара
        String description,      // описание товара
        BigDecimal price,        // цена товара
        List<String> images,     // список фото товара
        Boolean featured,        // показывать в "Рекомендуем"
        Integer stockQuantity,   // остаток товара
        Boolean trackStock       // учитывать остатки или нет
) {
}