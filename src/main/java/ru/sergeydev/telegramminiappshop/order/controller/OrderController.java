package ru.sergeydev.telegramminiappshop.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;
import ru.sergeydev.telegramminiappshop.telegram.security.TelegramInitDataValidator;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final TelegramInitDataValidator telegramInitDataValidator;

    // Создать заказ
    @PostMapping
    public OrderDetailsResponseDto createOrder(
            @RequestHeader(value = "X-Telegram-Init-Data",
                    required = false) String initData,
            @Valid @RequestBody CreateOrderRequestDto request) {
        Long telegramUserId = telegramInitDataValidator.extractTelegramUserId(initData);
        return orderService.createOrder(telegramUserId, request);
    }


    // Заказы конкретного пользователя Telegram
    @GetMapping("/my")
    public List<OrderDetailsResponseDto> getMyOrders(
            @RequestHeader(
                    value = "X-Telegram-Init-Data",
                    required = false
            ) String initData
    ) {
        Long telegramUserId =
                telegramInitDataValidator.extractTelegramUserId(initData);

        return orderService.getOrdersByTelegramUserId(telegramUserId);
    }


}