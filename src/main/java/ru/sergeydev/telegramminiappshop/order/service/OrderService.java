package ru.sergeydev.telegramminiappshop.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderSummaryResponseDto;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderView;
import ru.sergeydev.telegramminiappshop.common.exception.BadRequestException;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderItemRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderItem;
import ru.sergeydev.telegramminiappshop.order.entity.OrderSource;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.event.OrderCreatedEvent;
import ru.sergeydev.telegramminiappshop.order.event.OrderStatusChangedEvent;
import ru.sergeydev.telegramminiappshop.order.mapper.OrderMapper;
import ru.sergeydev.telegramminiappshop.order.repository.OrderRepository;
import ru.sergeydev.telegramminiappshop.product.entity.Product;
import ru.sergeydev.telegramminiappshop.product.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderMapper orderMapper;

    @Transactional
    public Order createOrder(OrderSource source, CreateOrderRequestDto request) {

        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("Заказ не может быть пустым");
        }

        Order order = new Order();
        order.setSource(source);
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setCustomerComment(request.customerComment());
        order.setStatus(OrderStatus.NEW);

        OffsetDateTime now = OffsetDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequestDto itemRequest : request.items()) {

            if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
                throw new BadRequestException("Количество товара должно быть больше 0");
            }

            Product product = productRepository.findByIdAndActiveTrue(itemRequest.productId())
                    .orElseThrow(() -> new NotFoundException("Товар не найден"));

            if (product.getTrackStock() && product.getStockQuantity() < itemRequest.quantity()) {
                throw new BadRequestException("Недостаточно товара на складе: " + product.getName());
            }
            //умножаем количество на цену
            BigDecimal itemTotalPrice = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()));

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order); // связываем позицию с заказом
            orderItem.setProduct(product); // ссылка на товар из каталога

            orderItem.setProductName(product.getName()); // снимок названия на момент заказа
            orderItem.setProductPrice(product.getPrice()); // снимок цены на момент заказа

            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setTotalPrice(itemTotalPrice);

            order.getItems().add(orderItem);

            totalAmount = totalAmount.add(itemTotalPrice);

        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(
                new OrderCreatedEvent(savedOrder.getId())
        );

        return savedOrder;

    }

    @Transactional(readOnly = true)
    public AdminOrderDetailsResponseDto getAdminOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        return orderMapper.toAdminDetailsDto(order);
    }

    @Transactional(readOnly = true)
    public List<AdminOrderSummaryResponseDto> getAdminOrders(AdminOrderView view) {

        // Для dashboard по умолчанию показывает активные заказы
        if (view == null) {
            view = AdminOrderView.ACTIVE;
        }

        List<Order> orders = switch (view) {
            case ACTIVE -> orderRepository.findByStatusInOrderByCreatedAtDesc(
                    List.of(OrderStatus.NEW, OrderStatus.IN_WORK)
            );

            case COMPLETED -> orderRepository.findByStatusInOrderByCreatedAtDesc(
                    List.of(OrderStatus.DONE, OrderStatus.CANCELLED)
            );

            case ALL -> orderRepository.findAllByOrderByCreatedAtDesc();
        };

        return orders.stream()
                .map(orderMapper::toAdminSummaryDto)
                .toList();
    }


    @Transactional
    public AdminOrderDetailsResponseDto updateOrderStatus(
            Long orderId,
            OrderStatus newStatus
    ) {
        if (newStatus == null) {
            throw new BadRequestException("Статус заказа не указан");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        OrderStatus currentStatus = order.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        // Если статус не изменился — ничего не делаем
        if (currentStatus == newStatus) {
            return orderMapper.toAdminDetailsDto(order);
        }

        // Менеджер принял заказ в работу — списываем товар
        if (currentStatus == OrderStatus.NEW
                && newStatus == OrderStatus.IN_WORK) {
            decreaseStock(order);
        }

        // Заказ отменили после принятия в работу — возвращаем товар
        if (currentStatus == OrderStatus.IN_WORK
                && newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(OffsetDateTime.now());
        // Публикуем событие внутри транзакции.
        // Сам OrderStatusChangedEvent создаётся сразу,
        // но обработчик с AFTER_COMMIT выполнится только после успешного commit.
        // Если транзакция откатится — Telegram-уведомление не отправится.
        eventPublisher.publishEvent(
                new OrderStatusChangedEvent(
                        order.getId(),
                        newStatus
                )
        );

        return orderMapper.toAdminDetailsDto(order);
    }

    private void decreaseStock(Order order) {

        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            // Если остатки этого товара не учитываются — пропускаем
            if (!product.getTrackStock()) {
                continue;
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException(
                        "Недостаточно товара на складе: " + product.getName()
                );
            }

            product.setStockQuantity(
                    product.getStockQuantity() - item.getQuantity()
            );
        }
    }

    private void restoreStock(Order order) {

        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            if (!product.getTrackStock()) {
                continue;
            }

            product.setStockQuantity(
                    product.getStockQuantity() + item.getQuantity()
            );
        }
    }

    //проверка статуса
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {

        if (currentStatus == newStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {
            case NEW -> newStatus == OrderStatus.IN_WORK || newStatus == OrderStatus.CANCELLED;
            case IN_WORK -> newStatus == OrderStatus.DONE || newStatus == OrderStatus.CANCELLED;
            case DONE, CANCELLED -> false;
        };

        if (!allowed) {
            throw new BadRequestException(
                    "Нельзя изменить статус с " + currentStatus + " на " + newStatus
            );
        }
    }

}