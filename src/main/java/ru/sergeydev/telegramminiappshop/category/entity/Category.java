package ru.sergeydev.telegramminiappshop.category.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id категории

    @Column(nullable = false)
    private String name; // название категории

    @Column(name = "image_url")
    private String imageUrl; // ссылка на изображение категории

    @Column(nullable = false)
    private Boolean active = true; // активна категория или скрыта

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0; // порядок отображения
}