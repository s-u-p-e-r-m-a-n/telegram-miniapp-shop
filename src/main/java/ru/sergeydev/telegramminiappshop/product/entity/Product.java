package ru.sergeydev.telegramminiappshop.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sergeydev.telegramminiappshop.category.entity.Category;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id товара

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // категория товара

    @Column(nullable = false)
    private String name; // название товара

    private String description; // описание товара

    @Column(nullable = false)
    private BigDecimal price; // цена товара

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Column(nullable = false)
    private Boolean active = true; // товар активен или скрыт

    @Column(nullable = false)
    private Boolean featured = false; // показывать в блоке "Рекомендуем"

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0; // порядок отображения

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0; // остаток товара

    @Column(name = "track_stock", nullable = false)
    private Boolean trackStock = true; // учитывать остатки или нет
}