package ru.sergeydev.telegramminiappshop.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.common.exception.BadRequestException;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderItemRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderItemResponseDto;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderItem;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
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

    @Transactional
    public OrderDetailsResponseDto createOrder(CreateOrderRequestDto request) {

        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("Заказ не может быть пустым");
        }

        Order order = new Order();

        order.setTelegramUserId(request.telegramUserId());
        order.setTelegramChatId(request.telegramChatId());
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

        return toOrderDetailsResponseDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDetailsResponseDto> getOrdersByTelegramUserId(Long telegramUserId) {
        return orderRepository.findByTelegramUserIdOrderByCreatedAtDesc(telegramUserId)
                .stream()
                .map(this::toOrderDetailsResponseDto)// собираем детали заказа
                .toList();
    }

    private OrderDetailsResponseDto toOrderDetailsResponseDto(Order order) {
        return new OrderDetailsResponseDto(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems()
                        .stream()
                        .map(this::toOrderItemResponseDto)
                        .toList()
        );
    }

    //собираем список товаров в заказе
    private OrderItemResponseDto toOrderItemResponseDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getProduct().getId(),
                item.getProductName(),
                item.getProductPrice(),
                item.getQuantity(),
                item.getTotalPrice()
        );
    }

    @Transactional(readOnly = true)
    public List<AdminOrderDetailsResponseDto> getAdminOrders(OrderStatus status) {

        if (status == null) {
            return orderRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .map(this::toAdminOrderDetailsResponseDto)
                    .toList();
        }

        return orderRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::toAdminOrderDetailsResponseDto)
                .toList();
    }

    private AdminOrderDetailsResponseDto toAdminOrderDetailsResponseDto(Order order) {
        return new AdminOrderDetailsResponseDto(
                order.getId(),
                order.getTelegramUserId(),
                order.getTelegramChatId(),

                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerComment(),

                order.getStatus(),
                order.getTotalAmount(),

                order.getCreatedAt(),
                order.getUpdatedAt(),

                order.getItems()
                        .stream()
                        .map(this::toOrderItemResponseDto)
                        .toList()
        );
    }


    @Transactional
    public AdminOrderDetailsResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus) {

        if (newStatus == null) {
            throw new BadRequestException("Статус заказа не указан");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(OffsetDateTime.now());

        return toAdminOrderDetailsResponseDto(order);
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