package ru.sergeydev.telegramminiappshop.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderResponseDto;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;

import java.util.List;

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

    // Заказы конкретного пользователя Telegram
    @GetMapping("/user/{telegramUserId}")
    public List<OrderDetailsResponseDto> getOrdersByTelegramUserId(@PathVariable Long telegramUserId) {
        return orderService.getOrdersByTelegramUserId(telegramUserId);
    }


}