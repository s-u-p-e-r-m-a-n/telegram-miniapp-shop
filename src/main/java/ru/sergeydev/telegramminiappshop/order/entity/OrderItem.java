package ru.sergeydev.telegramminiappshop.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sergeydev.telegramminiappshop.product.entity.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id позиции заказа

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // заказ, которому принадлежит позиция

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // товар из каталога

    @Column(name = "product_name", nullable = false)
    private String productName; // название товара на момент заказа

    @Column(name = "product_price", nullable = false)
    private BigDecimal productPrice; // цена товара на момент заказа

    @Column(nullable = false)
    private Integer quantity; // количество

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice; // productPrice * quantity
}