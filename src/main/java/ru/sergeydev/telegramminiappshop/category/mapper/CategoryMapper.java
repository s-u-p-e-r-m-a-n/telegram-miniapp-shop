package ru.sergeydev.telegramminiappshop.category.mapper;

import org.mapstruct.*;
import ru.sergeydev.telegramminiappshop.category.dto.CategoryResponseDto;
import ru.sergeydev.telegramminiappshop.category.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDto toResponseDto(Category category);
}