package ru.sergeydev.telegramminiappshop.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Заказы конкретного пользователя Telegram
    List<Order> findByTelegramUserIdOrderByCreatedAtDesc(Long telegramUserId);

    // Все заказы для админки, от новых к старым
    List<Order> findAllByOrderByCreatedAtDesc();

    // Заказы по статусу для админки
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

}