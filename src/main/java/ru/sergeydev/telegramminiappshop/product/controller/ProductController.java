package ru.sergeydev.telegramminiappshop.product.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.product.dto.ProductResponseDto;
import ru.sergeydev.telegramminiappshop.product.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Все активные товары
    @GetMapping
    public List<ProductResponseDto> getActiveProducts() {
        return productService.getActiveProducts();
    }

    // Один товар по id
    @GetMapping("/{id}")
    public ProductResponseDto getActiveProductById(@PathVariable Long id) {
        return productService.getActiveProductById(id);
    }

    // Рекомендуемые товары
    @GetMapping("/featured")
    public List<ProductResponseDto> getFeaturedProducts() {
        return productService.getFeaturedProducts();
    }

    // Товары по категории
    @GetMapping("/category/{categoryId}")
    public List<ProductResponseDto> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }
}