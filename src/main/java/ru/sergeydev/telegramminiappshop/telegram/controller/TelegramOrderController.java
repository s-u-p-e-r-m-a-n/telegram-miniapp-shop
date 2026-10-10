package ru.sergeydev.telegramminiappshop.telegram.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.telegram.security.TelegramInitDataValidator;
import ru.sergeydev.telegramminiappshop.telegram.service.TelegramOrderService;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class TelegramOrderController {

    private final TelegramOrderService telegramOrderService;
    private final TelegramInitDataValidator telegramInitDataValidator;

    // Создать заказ
    @PostMapping
    public OrderDetailsResponseDto createOrder(
            @RequestHeader(value = "X-Telegram-Init-Data",
                    required = false) String initData,

            @RequestHeader("Idempotency-Key")
            UUID idempotencyKey, //перед-м в заголовке ключ idempotency

            @Valid @RequestBody CreateOrderRequestDto request) {
        Long telegramUserId = telegramInitDataValidator.extractTelegramUserId(initData);

        return telegramOrderService.createOrder(
                telegramUserId,
                request,
                idempotencyKey
        );
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

        return telegramOrderService.getOrders(telegramUserId);
    }


}