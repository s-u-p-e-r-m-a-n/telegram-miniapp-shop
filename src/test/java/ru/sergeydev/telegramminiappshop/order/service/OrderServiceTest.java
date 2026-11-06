package ru.sergeydev.telegramminiappshop.order.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderItemRequestDto;
import ru.sergeydev.telegramminiappshop.order.dto.CreateOrderRequestDto;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderSource;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.mapper.OrderMapper;
import ru.sergeydev.telegramminiappshop.order.repository.OrderRepository;
import ru.sergeydev.telegramminiappshop.product.entity.Product;
import ru.sergeydev.telegramminiappshop.product.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderCalculatesTotalAndReservesStock() {
        UUID idempotencyKey = UUID.randomUUID();
        Product product = new Product();
        product.setId(1L);
        product.setName("Капкейк");
        product.setPrice(new BigDecimal("500.00"));
        product.setTrackStock(true);
        product.setStockQuantity(5);

        CreateOrderRequestDto request = new CreateOrderRequestDto(
                "Клиент", "+79000000000", null,
                List.of(new CreateOrderItemRequestDto(product.getId(), 2))
        );
        when(orderRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(productRepository.findActiveByIdForUpdate(product.getId())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(call -> call.getArgument(0));

        Order order = orderService.createOrder(OrderSource.TELEGRAM, request, idempotencyKey);

        verify(orderRepository).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("1000.00");
        assertThat(product.getStockQuantity()).isEqualTo(3);
    }
}
