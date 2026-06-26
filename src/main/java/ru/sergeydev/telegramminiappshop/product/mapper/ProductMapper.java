package ru.sergeydev.telegramminiappshop.product.mapper;

import org.mapstruct.*;
import ru.sergeydev.telegramminiappshop.product.dto.ProductResponseDto;
import ru.sergeydev.telegramminiappshop.product.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Product Entity -> ProductResponseDto
    // categoryId берём из product.category.id
    // categoryName берём из product.category.name
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductResponseDto toResponseDto(Product product);
}