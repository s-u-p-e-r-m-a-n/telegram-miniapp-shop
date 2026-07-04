package ru.sergeydev.telegramminiappshop.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sergeydev.telegramminiappshop.order.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Заказы конкретного пользователя Telegram
    List<Order> findByTelegramUserIdOrderByCreatedAtDesc(Long telegramUserId);
}