package ru.sergeydev.telegramminiappshop.payment.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderItem;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.repository.OrderRepository;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;
import ru.sergeydev.telegramminiappshop.payment.entity.Payment;
import ru.sergeydev.telegramminiappshop.payment.entity.PaymentStatus;
import ru.sergeydev.telegramminiappshop.payment.provider.InvoiceProvider;
import ru.sergeydev.telegramminiappshop.payment.provider.PaymentProvider;
import ru.sergeydev.telegramminiappshop.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private OrderService orderService;
    @Mock
    private PaymentProvider paymentProvider;
    @Mock
    private InvoiceProvider invoiceProvider;

    @Test
    void createPaymentCreatesInvoiceFromOrderAndStoresProviderResult() {
        Long orderId = 10L;
        Long paymentId = 20L;
        UUID idempotencyKey = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1000.00");
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(30);
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.AWAITING_PAYMENT);
        order.setTotalAmount(amount);
        order.setPaymentExpiresAt(expiresAt);

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setOrder(order);
        item.setProductName("Капкейк");
        item.setProductPrice(new BigDecimal("500.00"));
        item.setQuantity(2);
        order.getItems().add(item);

        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(paymentRepository.findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
                orderId, PaymentStatus.PENDING
        )).thenReturn(Optional.empty());

        // The repository assigns the ID and returns the same payment on subsequent reads.
        AtomicReference<Payment> savedPayment = new AtomicReference<>();
        when(paymentRepository.save(any(Payment.class))).thenAnswer(call -> {
            Payment payment = call.getArgument(0);
            payment.setId(paymentId);
            savedPayment.set(payment);
            return payment;
        });
        when(paymentRepository.findById(paymentId))
                .thenAnswer(call -> Optional.ofNullable(savedPayment.get()));
        when(paymentRepository.findByIdForUpdate(paymentId))
                .thenAnswer(call -> Optional.ofNullable(savedPayment.get()));

        List<InvoiceProvider.InvoiceItem> invoiceItems = List.of(
                new InvoiceProvider.InvoiceItem("Капкейк", new BigDecimal("500.00"), 2)
        );
        InvoiceProvider.CreatedInvoice invoice = new InvoiceProvider.CreatedInvoice(
                "invoice-123", "https://payment.example/invoice-123"
        );
        when(invoiceProvider.createInvoice(orderId, amount, invoiceItems, expiresAt, idempotencyKey))
                .thenReturn(invoice);
        PaymentPreparationService preparationService = new PaymentPreparationService(
                orderRepository, paymentRepository, entityManager, orderService
        );
        PaymentService paymentService = new PaymentService(
                orderService, paymentRepository, orderRepository, paymentProvider,
                invoiceProvider, entityManager, preparationService
        );

        Payment payment = paymentService.createPayment(orderId, idempotencyKey);

        verify(invoiceProvider).createInvoice(orderId, amount, invoiceItems, expiresAt, idempotencyKey);
        assertThat(payment).isSameAs(savedPayment.get());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getProviderInvoiceId()).isEqualTo("invoice-123");
        assertThat(payment.getConfirmationUrl()).isEqualTo("https://payment.example/invoice-123");
    }
}
