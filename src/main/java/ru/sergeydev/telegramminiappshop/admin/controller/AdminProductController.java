package ru.sergeydev.telegramminiappshop.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.product.dto.AdminProductResponseDto;
import ru.sergeydev.telegramminiappshop.product.dto.CreateProductRequestDto;
import ru.sergeydev.telegramminiappshop.product.dto.UpdateProductRequestDto;
import ru.sergeydev.telegramminiappshop.product.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public List<AdminProductResponseDto> getAdminProducts() {
        return productService.getAllProductsForAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminProductResponseDto createProduct(
            @Valid @RequestBody CreateProductRequestDto request
    ) {
        return productService.createProduct(request);
    }

    @GetMapping("/{productId}")
    public AdminProductResponseDto getAdminProduct(
            @PathVariable Long productId
    ) {
        return productService.getAdminProductById(productId);
    }

    @PutMapping("/{productId}")
    public AdminProductResponseDto updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequestDto request
    ) {
        return productService.updateProduct(productId, request);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideProduct(@PathVariable Long productId) {
        productService.hideProduct(productId);
    }
}