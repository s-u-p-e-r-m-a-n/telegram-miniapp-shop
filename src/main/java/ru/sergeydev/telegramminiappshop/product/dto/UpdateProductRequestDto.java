package ru.sergeydev.telegramminiappshop.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductRequestDto(
        @NotNull @Positive
        Long categoryId,

        @NotBlank @Size(max = 255)
        String name,

        @Size(max = 5000)
        String description,

        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @NotNull
        List<@NotBlank String> images,

        @NotNull
        Boolean active,

        @NotNull
        Boolean featured,

        @NotNull @PositiveOrZero
        Integer sortOrder,

        @NotNull @PositiveOrZero
        Integer stockQuantity,

        @NotNull
        Boolean trackStock
) {
}