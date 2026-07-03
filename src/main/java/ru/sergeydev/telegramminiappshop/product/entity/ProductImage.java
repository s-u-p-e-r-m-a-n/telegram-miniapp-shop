package ru.sergeydev.telegramminiappshop.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id изображения

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // товар, которому принадлежит фото

    @Column(name = "image_url", nullable = false)
    private String imageUrl; // ссылка на фото

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0; // порядок показа фото

    @Column(name = "main_image", nullable = false)
    private Boolean mainImage = false; // главное фото товара
}