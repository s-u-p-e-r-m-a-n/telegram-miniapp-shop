package ru.sergeydev.telegramminiappshop.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sergeydev.telegramminiappshop.category.dto.CategoryResponseDto;
import ru.sergeydev.telegramminiappshop.category.mapper.CategoryMapper;
import ru.sergeydev.telegramminiappshop.category.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.sergeydev.telegramminiappshop.category.dto.CreateCategoryRequestDto;
import ru.sergeydev.telegramminiappshop.category.entity.Category;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.category.entity.Category;
import ru.sergeydev.telegramminiappshop.common.exception.ConflictException;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    public List<CategoryResponseDto> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public CategoryResponseDto createCategory(CreateCategoryRequestDto request) {
        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Категория с таким названием уже существует"
            );
        }

        String imageUrl = request.imageUrl();

        if (imageUrl != null) {
            imageUrl = imageUrl.trim();

            if (imageUrl.isEmpty()) {
                imageUrl = null;
            }
        }

        int nextSortOrder = categoryRepository.findAll()
                .stream()
                .mapToInt(Category::getSortOrder)
                .max()
                .orElse(0) + 1;

        Category category = new Category();
        category.setName(name);
        category.setImageUrl(imageUrl);
        category.setActive(true);
        category.setSortOrder(nextSortOrder);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toResponseDto(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Категория с ID " + categoryId + " не найдена"
                        )
                );

        if (productRepository.existsByCategory_Id(categoryId)) {
            throw new ConflictException(
                    "Нельзя удалить категорию: сначала перенесите её товары в другую категорию"
            );
        }

        categoryRepository.delete(category);
    }

}