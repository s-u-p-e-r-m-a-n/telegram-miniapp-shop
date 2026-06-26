package ru.sergeydev.telegramminiappshop.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sergeydev.telegramminiappshop.category.dto.CategoryResponseDto;
import ru.sergeydev.telegramminiappshop.category.mapper.CategoryMapper;
import ru.sergeydev.telegramminiappshop.category.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDto> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }
}