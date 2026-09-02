package ru.sergeydev.telegramminiappshop.telegram.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sergeydev.telegramminiappshop.order.entity.Order;

//храним отдельные данные телеграм c Id-заказом
@Entity
@Table(name = "telegram_order_data")
@Getter
@Setter
@NoArgsConstructor
public class TelegramOrderDataEntity {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId//кладем полученный Id в Long orderId которое выше.
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private TelegramUserEntity telegramUser;
}