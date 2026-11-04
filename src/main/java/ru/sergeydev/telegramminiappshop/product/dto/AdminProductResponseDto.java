package ru.sergeydev.telegramminiappshop.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminProductResponseDto(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal price,
        List<String> images,
        Boolean active,
        Boolean featured,
        Integer sortOrder,
        Integer stockQuantity,
        Boolean trackStock
) {
}