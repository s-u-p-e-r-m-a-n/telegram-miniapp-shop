package ru.sergeydev.telegramminiappshop.admin.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.category.dto.CategoryResponseDto;
import ru.sergeydev.telegramminiappshop.category.dto.CreateCategoryRequestDto;
import ru.sergeydev.telegramminiappshop.category.service.CategoryService;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategory(
            @Valid @RequestBody CreateCategoryRequestDto request
    ) {
        return categoryService.createCategory(request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }
}