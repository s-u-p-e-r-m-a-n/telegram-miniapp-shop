package ru.sergeydev.telegramminiappshop.telegram.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.common.exception.BadRequestException;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.OrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderSource;
import ru.sergeydev.telegramminiappshop.order.mapper.OrderMapper;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;
import ru.sergeydev.telegramminiappshop.telegram.entity.TelegramOrderDataEntity;
import ru.sergeydev.telegramminiappshop.telegram.entity.TelegramUserEntity;
import ru.sergeydev.telegramminiappshop.telegram.repository.TelegramOrderDataRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramOrderService {

    private final TelegramUserService telegramUserService;
    private final OrderService orderService;
    private final TelegramOrderDataRepository telegramOrderDataRepository;
    private final OrderMapper orderMapper;
    private final TelegramNotificationService telegramNotificationService;

    @Transactional
    public OrderDetailsResponseDto  createOrder(
            Long telegramUserId,
            CreateOrderRequestDto request
    ) {

        TelegramUserEntity telegramUser =
                telegramUserService.getByTelegramUserId(telegramUserId);

        Order order = orderService.createOrder(
                OrderSource.TELEGRAM,
                request
        );

        TelegramOrderDataEntity telegramOrderData = new TelegramOrderDataEntity();
       //связываем заказ с клиентом(telegramUser)
        telegramOrderData.setOrder(order);
        telegramOrderData.setTelegramUser(telegramUser);

        telegramOrderDataRepository.save(telegramOrderData);

        return orderMapper.toDetailsDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDetailsResponseDto> getOrders(Long telegramUserId) {

        return telegramOrderDataRepository
                .findByTelegramUserTelegramUserIdOrderByOrderCreatedAtDesc(telegramUserId)
                .stream()
                .map(TelegramOrderDataEntity::getOrder)
                .map(orderMapper::toDetailsDto)
                .toList();
    }

    //по id заказа ищем пользовательский chatId
    @Transactional(readOnly = true)
    public Optional<Long> findChatIdByOrderId(Long orderId) {

        return telegramOrderDataRepository.findById(orderId)
                .map(data -> data.getTelegramUser().getTelegramChatId());
    }

    @Transactional(readOnly = true)
    public void sendManagerMessageToCustomer(
            Long orderId,
            String message
    ) {
        TelegramOrderDataEntity telegramOrderData =
                telegramOrderDataRepository.findById(orderId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Заказ не связан с Telegram"
                                )
                        );

        Long chatId = telegramOrderData
                .getTelegramUser()
                .getTelegramChatId();

        telegramNotificationService.sendManagerMessageToCustomer(
                chatId,
                orderId,
                message
        );
    }

    @Transactional(readOnly = true)
    public void sendOrderCreatedNotifications(Long orderId) {

        telegramOrderDataRepository.findById(orderId)
                .ifPresent(data -> {

                    Order order = data.getOrder();

                    Long chatId = data.getTelegramUser()
                            .getTelegramChatId();

                    telegramNotificationService
                            .sendOrderCreatedToCustomer(order, chatId);

                    telegramNotificationService
                            .sendOrderCreatedToAdmin(order);
                });
    }
}