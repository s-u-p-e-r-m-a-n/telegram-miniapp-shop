package ru.sergeydev.telegramminiappshop.order.repository;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    //поиск заказа по уникальному ключу
    Optional<Order> findByIdempotencyKey(UUID idempotencyKey);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select o
    from Order o
    where o.id = :orderId
    """)
    Optional<Order> findByIdForUpdate(
            @Param("orderId") Long orderId
    );


}