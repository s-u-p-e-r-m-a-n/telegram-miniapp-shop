package ru.sergeydev.telegramminiappshop.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.category.entity.Category;
import ru.sergeydev.telegramminiappshop.category.repository.CategoryRepository;
import ru.sergeydev.telegramminiappshop.common.exception.ConflictException;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.product.dto.AdminProductResponseDto;
import ru.sergeydev.telegramminiappshop.product.dto.CreateProductRequestDto;
import ru.sergeydev.telegramminiappshop.product.dto.ProductResponseDto;
import ru.sergeydev.telegramminiappshop.product.dto.UpdateProductRequestDto;
import ru.sergeydev.telegramminiappshop.product.entity.Product;
import ru.sergeydev.telegramminiappshop.product.entity.ProductImage;
import ru.sergeydev.telegramminiappshop.product.mapper.ProductMapper;
import ru.sergeydev.telegramminiappshop.product.repository.ProductImageRepository;
import ru.sergeydev.telegramminiappshop.product.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {


    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;


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

    //запрос администратора
    @Transactional(readOnly = true)
    public List<AdminProductResponseDto> getAllProductsForAdmin() {
        return productRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(productMapper::toAdminResponseDto)
                .toList();
    }

    @Transactional
    public AdminProductResponseDto createProduct(CreateProductRequestDto request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        Product product = new Product();
        product.setCategory(category);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());

        product.setActive(request.active() == null || request.active());//если пустое значение, то ставим true
        product.setFeatured(Boolean.TRUE.equals(request.featured()));
        product.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        product.setStockQuantity(
                request.stockQuantity() == null ? 0 : request.stockQuantity()
        );
        product.setTrackStock(request.trackStock() == null || request.trackStock());

        Product savedProduct = productRepository.save(product);
        List<String> imageUrls = request.images() == null
                ? List.of()
                : request.images();

        List<ProductImage> images = new ArrayList<>();

        for (int index = 0; index < imageUrls.size(); index++) {
            ProductImage image = new ProductImage();
            image.setProduct(savedProduct);
            image.setImageUrl(imageUrls.get(index));
            image.setSortOrder(index);
            image.setMainImage(index == 0);
            images.add(image);
        }

        if (!images.isEmpty()) {
            productImageRepository.saveAll(images);
            savedProduct.getImages().addAll(images);
        }

        return productMapper.toAdminResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public AdminProductResponseDto getAdminProductById(Long productId) {
        return productRepository.findById(productId)
                .map(productMapper::toAdminResponseDto)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));
    }

    @Transactional
    public AdminProductResponseDto updateProduct(
            Long productId,
            UpdateProductRequestDto request
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        product.setCategory(category);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setActive(request.active());
        product.setFeatured(request.featured());
        product.setSortOrder(request.sortOrder());
        product.setStockQuantity(request.stockQuantity());
        product.setTrackStock(request.trackStock());

        replaceProductImages(product, request.images());

        productRepository.save(product);

        return productMapper.toAdminResponseDto(product);
    }

    @Transactional
    public void hideProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Товар не найден"));
        if (productRepository.existsInOrders(productId)) {
            throw new ConflictException("Нельзя удалить товар: он есть в заказах № "
                    + productRepository.findByOrderId(productId));
        }

        productRepository.delete(product);

    }

    //замена изображений
    private void replaceProductImages(Product product, List<String> imageUrls) {
        List<ProductImage> existingImages = product.getImages();

        if (!existingImages.isEmpty()) {
            productImageRepository.deleteAll(existingImages);
            existingImages.clear();
        }

        List<ProductImage> newImages = new ArrayList<>();

        for (int index = 0; index < imageUrls.size(); index++) {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setImageUrl(imageUrls.get(index));
            image.setSortOrder(index);
            image.setMainImage(index == 0);
            newImages.add(image);
        }

        if (!newImages.isEmpty()) {
            productImageRepository.saveAll(newImages);
            existingImages.addAll(newImages);
        }
    }

}

