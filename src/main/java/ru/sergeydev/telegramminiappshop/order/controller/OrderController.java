package ru.sergeydev.telegramminiappshop.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderResponseDto;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Создать заказ
    @PostMapping
    public OrderResponseDto createOrder(@RequestBody CreateOrderRequestDto request) {
        return orderService.createOrder(request);
    }
}