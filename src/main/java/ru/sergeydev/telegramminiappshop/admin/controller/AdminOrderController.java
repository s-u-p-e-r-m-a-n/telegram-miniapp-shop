package ru.sergeydev.telegramminiappshop.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    // Все заказы или заказы по конкретному статусу
    @GetMapping
    public List<AdminOrderDetailsResponseDto> getAdminOrders(
            @RequestParam(required = false) OrderStatus status
    ) {
        return orderService.getAdminOrders(status);
    }
}