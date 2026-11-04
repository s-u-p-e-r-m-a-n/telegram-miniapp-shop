package ru.sergeydev.telegramminiappshop.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sergeydev.telegramminiappshop.product.dto.AdminProductResponseDto;
import ru.sergeydev.telegramminiappshop.product.dto.CreateProductRequestDto;
import ru.sergeydev.telegramminiappshop.product.dto.UpdateProductRequestDto;
import ru.sergeydev.telegramminiappshop.product.dto.UploadImageResponseDto;
import ru.sergeydev.telegramminiappshop.product.service.ProductImageStorageService;
import ru.sergeydev.telegramminiappshop.product.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductImageStorageService imageStorageService;

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

    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadImageResponseDto uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = imageStorageService.store(file);
        return new UploadImageResponseDto(imageUrl);
    }
}