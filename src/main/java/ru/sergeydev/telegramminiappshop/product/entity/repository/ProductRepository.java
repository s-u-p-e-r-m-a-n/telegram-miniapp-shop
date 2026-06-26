package ru.sergeydev.telegramminiappshop.product.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sergeydev.telegramminiappshop.product.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {


        // Все активные товары для каталога
        List<Product> findByActiveTrueOrderBySortOrderAsc();

        // Один активный товар по id
        Optional<Product> findByIdAndActiveTrue(Long id);

        // Рекомендуемые активные товары
        List<Product> findByActiveTrueAndFeaturedTrueOrderBySortOrderAsc();

        // Активные товары конкретной категории
        List<Product> findByCategoryIdAndActiveTrueOrderBySortOrderAsc(Long categoryId);
}
