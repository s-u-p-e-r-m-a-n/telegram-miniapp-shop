package ru.sergeydev.telegramminiappshop.order.mapper;

import org.springframework.stereotype.Component;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderSummaryResponseDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderItemResponseDto;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderItem;

@Component
public class OrderMapper {

    public OrderDetailsResponseDto toDetailsDto(Order order) {
        return new OrderDetailsResponseDto(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems()
                        .stream()
                        .map(this::toItemDto)
                        .toList()
        );
    }

    public AdminOrderSummaryResponseDto toAdminSummaryDto(Order order) {
        return new AdminOrderSummaryResponseDto(
                order.getId(),
                order.getCreatedAt(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getTotalAmount(),
                order.getStatus()
        );
    }

    public AdminOrderDetailsResponseDto toAdminDetailsDto(Order order) {
        return new AdminOrderDetailsResponseDto(
                order.getId(),
                order.getSource(),

                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerComment(),

                order.getStatus(),
                order.getTotalAmount(),

                order.getCreatedAt(),
                order.getUpdatedAt(),

                order.getItems()
                        .stream()
                        .map(this::toItemDto)
                        .toList()
        );
    }

    private OrderItemResponseDto toItemDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getProduct().getId(),
                item.getProductName(),
                item.getProductPrice(),
                item.getQuantity(),
                item.getTotalPrice()
        );
    }
}