package ru.sergeydev.telegramminiappshop.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Все заказы для админки, от новых к старым
    List<Order> findAllByOrderByCreatedAtDesc();

    // Заказы по статусу для админки
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    //поиск по полученному статусу заказов
    List<Order> findByStatusInOrderByCreatedAtDesc(Collection<OrderStatus> statuses);



}