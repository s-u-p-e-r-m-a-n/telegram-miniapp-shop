package ru.sergeydev.telegramminiappshop.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id заказа

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId; // id пользователя Telegram

    @Column(name = "telegram_chat_id")
    private Long telegramChatId; // id чата для отправки уведомлений

    @Column(name = "customer_name", nullable = false)
    private String customerName; // имя клиента

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone; // телефон клиента

    @Column(name = "customer_comment")
    private String customerComment; // комментарий к заказу

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW; // статус заказа

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount; // итоговая сумма заказа

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt; // дата создания

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt; // дата обновления

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>(); // товары внутри заказа
}