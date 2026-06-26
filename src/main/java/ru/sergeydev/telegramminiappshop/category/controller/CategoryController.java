package ru.sergeydev.telegramminiappshop.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sergeydev.telegramminiappshop.category.dto.CategoryResponseDto;
import ru.sergeydev.telegramminiappshop.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDto> getActiveCategories() {
        return categoryService.getActiveCategories();
    }
}