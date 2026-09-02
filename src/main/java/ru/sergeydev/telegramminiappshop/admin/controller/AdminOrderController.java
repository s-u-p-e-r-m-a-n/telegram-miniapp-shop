package ru.sergeydev.telegramminiappshop.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.admin.dto.*;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;
import ru.sergeydev.telegramminiappshop.telegram.service.TelegramOrderService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final TelegramOrderService telegramOrderService;

    @GetMapping
    public List<AdminOrderSummaryResponseDto> getAdminOrders(
            @RequestParam(required = false) AdminOrderView view
    ) {
        return orderService.getAdminOrders(view);
    }

    @PatchMapping("/{orderId}/status")
    public AdminOrderDetailsResponseDto updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDto request
    ) {
        return orderService.updateOrderStatus(orderId, request.status());
    }

    @PostMapping("/{orderId}/message")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendMessageToCustomer(
            @PathVariable Long orderId,
            @Valid @RequestBody AdminSendMessageRequestDto request
    ) {
        telegramOrderService.sendManagerMessageToCustomer(
                orderId,
                request.message()
        );
    }

    @GetMapping("/{orderId}")
    public AdminOrderDetailsResponseDto getOrderDetails(@PathVariable Long orderId) {
        return orderService.getAdminOrderById(orderId);
    }


}