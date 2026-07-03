package ru.sergeydev.telegramminiappshop.product.mapper;

import org.mapstruct.*;
import ru.sergeydev.telegramminiappshop.product.dto.ProductResponseDto;
import ru.sergeydev.telegramminiappshop.product.entity.Product;
import ru.sergeydev.telegramminiappshop.product.entity.ProductImage;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "images", target = "images")
    ProductResponseDto toResponseDto(Product product);

    // ProductImage -> String
    // Берём только ссылки на фото и отдаём их во frontend
    default List<String> mapImages(List<ProductImage> images) {
        if (images == null) {
            return List.of();
        }

        return images.stream()
                .sorted(Comparator.comparing(ProductImage::getSortOrder))
                .map(ProductImage::getImageUrl)
                .toList();
    }
}