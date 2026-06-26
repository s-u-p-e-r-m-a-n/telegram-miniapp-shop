package ru.sergeydev.telegramminiappshop.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sergeydev.telegramminiappshop.category.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

//найти активные категории и отсортировать по порядку вывода.
    List<Category> findByActiveTrueOrderBySortOrderAsc();
}
