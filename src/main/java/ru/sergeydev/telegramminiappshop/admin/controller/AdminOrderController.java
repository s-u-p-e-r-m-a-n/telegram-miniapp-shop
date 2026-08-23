package ru.sergeydev.telegramminiappshop.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminSendMessageRequestDto;
import ru.sergeydev.telegramminiappshop.admin.dto.UpdateOrderStatusRequestDto;
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

    @PatchMapping("/{orderId}/status")
    public AdminOrderDetailsResponseDto updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDto request
    ) {
        return orderService.updateOrderStatus(orderId, request.status());
    }
    @PostMapping("/orders/{orderId}/message")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendMessageToCustomer(
            @PathVariable Long orderId,
            @Valid @RequestBody AdminSendMessageRequestDto request
    ) {
        orderService.sendManagerMessageToCustomer(
                orderId,
                request.message()
        );
    }
}