package ru.sergeydev.telegramminiappshop.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.product.dto.ProductResponseDto;
import ru.sergeydev.telegramminiappshop.product.repository.ProductRepository;
import ru.sergeydev.telegramminiappshop.product.mapper.ProductMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {


    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // Получить все активные товары
    public List<ProductResponseDto> getActiveProducts() {
        return productRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    // Получить один активный товар по id
    public ProductResponseDto getActiveProductById(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(productMapper::toResponseDto)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));
    }

    // Получить рекомендуемые товары
    public List<ProductResponseDto> getFeaturedProducts() {
        return productRepository.findByActiveTrueAndFeaturedTrueOrderBySortOrderAsc()
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    // Получить активные товары по категории
    public List<ProductResponseDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrueOrderBySortOrderAsc(categoryId)
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }
}

