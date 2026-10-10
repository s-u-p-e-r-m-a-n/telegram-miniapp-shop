package ru.sergeydev.telegramminiappshop.order.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderDetailsResponseDto;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderSummaryResponseDto;
import ru.sergeydev.telegramminiappshop.admin.dto.AdminOrderView;
import ru.sergeydev.telegramminiappshop.common.exception.BadRequestException;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderItemRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderItem;
import ru.sergeydev.telegramminiappshop.order.entity.OrderSource;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.event.OrderCreatedEvent;
import ru.sergeydev.telegramminiappshop.order.event.OrderStatusChangedEvent;
import ru.sergeydev.telegramminiappshop.order.mapper.OrderMapper;
import ru.sergeydev.telegramminiappshop.order.repository.OrderRepository;
import ru.sergeydev.telegramminiappshop.product.entity.Product;
import ru.sergeydev.telegramminiappshop.product.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderMapper orderMapper;
    private final EntityManager entityManager;

    @Transactional
    public Order createOrder(OrderSource source, CreateOrderRequestDto request, UUID idempotencyKey) {

        Order existingOrder = orderRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existingOrder != null) {
            return existingOrder;
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("Заказ не может быть пустым");
        }

        Order order = new Order();
        order.setIdempotencyKey(idempotencyKey);
        order.setSource(source);
        order.setCustomerName(request.customerName());
        order.setCustomerPhone(request.customerPhone());
        order.setCustomerComment(request.customerComment());
        order.setStatus(OrderStatus.AWAITING_PAYMENT);

        OffsetDateTime now = OffsetDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setPaymentExpiresAt(now.plusMinutes(30));

        BigDecimal totalAmount = BigDecimal.ZERO;

        //сортировка товаров для избежания взаимных блокировок при обращении к БД с учетом блокировок
        List<CreateOrderItemRequestDto> sortedItems = request.items()
                .stream()
                .sorted(Comparator.comparing(
                        CreateOrderItemRequestDto::productId
                ))
                .toList();

        for (CreateOrderItemRequestDto itemRequest : sortedItems) {

            if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
                throw new BadRequestException("Количество товара должно быть больше 0");
            }

            Product product = productRepository.findActiveByIdForUpdate(itemRequest.productId())
                    .orElseThrow(() -> new NotFoundException("Товар не найден"));

            if (product.getTrackStock() && product.getStockQuantity() < itemRequest.quantity()) {
                throw new BadRequestException("Недостаточно товара на складе: " + product.getName());
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

        }

        decreaseStock(order);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        return savedOrder;

    }

    @Transactional(readOnly = true)
    public AdminOrderDetailsResponseDto getAdminOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        return orderMapper.toAdminDetailsDto(order);
    }

    @Transactional(readOnly = true)
    public List<AdminOrderSummaryResponseDto> getAdminOrders(AdminOrderView view) {

        // Для dashboard по умолчанию показывает активные заказы
        if (view == null) {
            view = AdminOrderView.ACTIVE;
        }

        List<Order> orders = switch (view) {
            case ACTIVE -> orderRepository.findByStatusInOrderByCreatedAtDesc(
                    List.of(OrderStatus.NEW, OrderStatus.IN_WORK)
            );

            case COMPLETED -> orderRepository.findByStatusInOrderByCreatedAtDesc(
                    List.of(OrderStatus.DONE, OrderStatus.CANCELLED)
            );

            case ALL -> orderRepository.findAllByOrderByCreatedAtDesc();
        };

        return orders.stream()
                .map(orderMapper::toAdminSummaryDto)
                .toList();
    }


    @Transactional
    public AdminOrderDetailsResponseDto updateOrderStatus(
            Long orderId,
            OrderStatus newStatus
    ) {
        if (newStatus == null) {
            throw new BadRequestException("Статус заказа не указан");
        }

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new NotFoundException("Заказ не найден")
                );

        entityManager.refresh(order);

        OrderStatus currentStatus = order.getStatus();

        validateStatusTransition(currentStatus, newStatus);

        // Если статус не изменился — ничего не делаем
        if (currentStatus == newStatus) {
            return orderMapper.toAdminDetailsDto(order);
        }

        //возврат
        if ((currentStatus == OrderStatus.NEW
                || currentStatus == OrderStatus.IN_WORK)
                && newStatus == OrderStatus.CANCELLED) {

            restoreStock(order);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(OffsetDateTime.now());
        // Публикуем событие внутри транзакции.
        // Сам OrderStatusChangedEvent создаётся сразу,
        // но обработчик с AFTER_COMMIT выполнится только после успешного commit.
        // Если транзакция откатится — Telegram-уведомление не отправится.
        eventPublisher.publishEvent(
                new OrderStatusChangedEvent(
                        order.getId(),
                        newStatus
                )
        );

        return orderMapper.toAdminDetailsDto(order);
    }

    private void decreaseStock(Order order) {

        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            // Если остатки этого товара не учитываются — пропускаем
            if (!product.getTrackStock()) {
                continue;
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException(
                        "Недостаточно товара на складе: " + product.getName()
                );
            }

            product.setStockQuantity(
                    product.getStockQuantity() - item.getQuantity()
            );
        }
    }

    private void restoreStock(Order order) {

        var quantitiesByProductId = new TreeMap<Long, Integer>();//отсортированные ключи(порядка обхода) в мапе,иначе взаимная блокировка
//сохраняем в мап заказ, присваивается индент productId(Long) + ЗНАЧЕНИЕ(Integer), при каждом проходе если идент
// одинаковый срабатывает переданный метод суммирования старого значения+новое. Подсчет остатков по каждому товару.
        for (OrderItem item : order.getItems()) {
            quantitiesByProductId.merge(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }
//возвращаем товар на склад
        for (var entry : quantitiesByProductId.entrySet()) {//.entrySet() используется для того,
            // чтобы перебрать (обойти в цикле) полученный результат.

            Product product = productRepository
                    .findByIdForUpdate(entry.getKey())
                    .orElseThrow(() ->
                            new NotFoundException("Товар заказа не найден")
                    );

            // Перечитываем состояние после получения блокировки.
            entityManager.refresh(product);

            if (!product.getTrackStock()) {
                continue;
            }

            product.setStockQuantity(
                    product.getStockQuantity() + entry.getValue()
            );
        }
    }

    //проверка статуса
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {

        if (currentStatus == newStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {
            case AWAITING_PAYMENT -> false;
            case NEW -> newStatus == OrderStatus.IN_WORK || newStatus == OrderStatus.CANCELLED;
            case IN_WORK -> newStatus == OrderStatus.DONE || newStatus == OrderStatus.CANCELLED;
            case DONE, CANCELLED -> false;
        };

        if (!allowed) {
            throw new BadRequestException(
                    "Нельзя изменить статус с " + currentStatus + " на " + newStatus
            );
        }
    }

    @Transactional
    public void markOrderAsPaid(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new BadRequestException(
                    "Заказ не ожидает оплату"
            );
        }

        order.setStatus(OrderStatus.NEW);
        order.setPaymentExpiresAt(null);
        order.setUpdatedAt(OffsetDateTime.now());

        eventPublisher.publishEvent(
                new OrderCreatedEvent(order.getId())
        );
    }

    @Transactional
    public void cancelAwaitingPayment(Long orderId) {

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new NotFoundException("Заказ не найден")
                );
//запрос свежей инф. из БД по order
        entityManager.refresh(order);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new BadRequestException(
                    "Заказ не ожидает оплату"
            );
        }

        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(OffsetDateTime.now());

        eventPublisher.publishEvent(
                new OrderStatusChangedEvent(
                        order.getId(),
                        OrderStatus.CANCELLED
                )
        );
    }

}