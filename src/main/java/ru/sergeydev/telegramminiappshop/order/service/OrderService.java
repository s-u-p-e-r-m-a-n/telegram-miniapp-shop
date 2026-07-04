package ru.sergeydev.telegramminiappshop.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderItemRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderResponseDto;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderItem;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.repository.OrderRepository;
import ru.sergeydev.telegramminiappshop.product.entity.Product;
import ru.sergeydev.telegramminiappshop.product.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto request) {

        if (request.items() == null || request.items().isEmpty()) {
            throw new RuntimeException("Заказ не может быть пустым");
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
                throw new RuntimeException("Количество товара должно быть больше 0");
            }

            Product product = productRepository.findByIdAndActiveTrue(itemRequest.productId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            if (product.getTrackStock() && product.getStockQuantity() < itemRequest.quantity()) {
                throw new RuntimeException("Недостаточно товара на складе: " + product.getName());
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

            if (product.getTrackStock()) {
                product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());
            }
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        return new OrderResponseDto(
                savedOrder.getId(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount(),
                savedOrder.getCreatedAt()
        );
    }
}