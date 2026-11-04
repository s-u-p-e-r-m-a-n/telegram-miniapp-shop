package ru.sergeydev.telegramminiappshop.payment.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.common.exception.BadRequestException;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.repository.OrderRepository;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;
import ru.sergeydev.telegramminiappshop.payment.entity.Payment;
import ru.sergeydev.telegramminiappshop.payment.entity.PaymentStatus;
import ru.sergeydev.telegramminiappshop.payment.repository.PaymentRepository;
import ru.sergeydev.telegramminiappshop.order.entity.OrderItem;
import ru.sergeydev.telegramminiappshop.payment.dto.InvoiceCreationData;
import ru.sergeydev.telegramminiappshop.payment.provider.InvoiceProvider;
import java.util.Comparator;

import java.time.OffsetDateTime;
import java.util.UUID;



//Сервис подготовки платежей
@Service
@RequiredArgsConstructor
public class PaymentPreparationService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final EntityManager entityManager;
    private final OrderService orderService;


    //Подготовка платежа
    @Transactional
    public Long preparePayment(Long orderId, UUID idempotencyKey) {

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new NotFoundException("Заказ не найден")
                );

        entityManager.refresh(order);

        Payment existingPayment = paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElse(null);

        if (existingPayment != null) {
            if (!existingPayment.getOrder().getId().equals(orderId)) {
                throw new BadRequestException(
                        "Idempotency-Key уже используется для другого заказа"
                );
            }

            return existingPayment.getId();
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new BadRequestException("Заказ не ожидает оплату");
        }

        Payment pendingPayment = paymentRepository
                .findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
                        orderId,
                        PaymentStatus.PENDING
                )
                .orElse(null);

        if (pendingPayment != null) {
            return pendingPayment.getId();
        }

        OffsetDateTime now = OffsetDateTime.now();

        if (order.getPaymentExpiresAt() == null
                || !order.getPaymentExpiresAt().isAfter(now)) {
            throw new BadRequestException("Срок оплаты заказа истёк");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        return paymentRepository.save(payment).getId();
    }
//получаем данные для создания счета
    @Transactional(readOnly = true)
    public InvoiceCreationData getInvoiceCreationData(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new NotFoundException("Платёж не найден")
                );

        Order order = payment.getOrder();

        if (payment.getStatus() != PaymentStatus.PENDING
                || order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new BadRequestException(
                    "Оплата или заказ уже завершены"
            );
        }

        if (order.getPaymentExpiresAt() == null) {
            throw new IllegalStateException(
                    "У заказа отсутствует срок оплаты"
            );
        }

        if (payment.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalStateException(
                    "Сумма попытки оплаты не совпадает с суммой заказа"
            );
        }

        var items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getId))
                .map(item -> new InvoiceProvider.InvoiceItem(
                        item.getProductName(),
                        item.getProductPrice(),
                        item.getQuantity()
                ))
                .toList();

        return new InvoiceCreationData(
                payment.getId(),
                order.getId(),
                payment.getAmount(),
                items,
                order.getPaymentExpiresAt(),
                payment.getIdempotencyKey()
        );
    }

    @Transactional
    public Payment saveCreatedInvoice(
            Long orderId,
            Long paymentId,
            InvoiceProvider.CreatedInvoice createdInvoice
    ) {
        if (createdInvoice == null
                || createdInvoice.providerInvoiceId() == null
                || createdInvoice.providerInvoiceId().isBlank()
                || createdInvoice.paymentUrl() == null
                || createdInvoice.paymentUrl().isBlank()) {
            throw new IllegalStateException(
                    "Не получены ID счёта или ссылка на оплату"
            );
        }

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new NotFoundException("Платёж не найден"));

        entityManager.refresh(payment);

        if (!payment.getOrder().getId().equals(order.getId())) {
            throw new IllegalStateException(
                    "Платёж принадлежит другому заказу"
            );
        }

        String existingInvoiceId = payment.getProviderInvoiceId();

        if (existingInvoiceId != null
                && !existingInvoiceId.equals(createdInvoice.providerInvoiceId())) {
            throw new IllegalStateException(
                    "К платежу уже привязан другой счёт ЮKassa"
            );
        }

        if (existingInvoiceId == null) {
            entityManager.refresh(order);

            if (order.getStatus() != OrderStatus.AWAITING_PAYMENT
                    || payment.getStatus() != PaymentStatus.PENDING) {
                throw new BadRequestException(
                        "Заказ уже не ожидает оплаты. "
                                + "Ссылка на оплату не будет выдана"
                );
            }

            payment.setProviderInvoiceId(
                    createdInvoice.providerInvoiceId()
            );
        }

        if (payment.getConfirmationUrl() == null
                || payment.getConfirmationUrl().isBlank()) {
            payment.setConfirmationUrl(createdInvoice.paymentUrl());
        }

        payment.setUpdatedAt(OffsetDateTime.now());

        return payment;
    }
//отмена просроченного платежа без счета
    @Transactional
    public void cancelExpiredPaymentWithoutInvoice(
            Long orderId,
            Long paymentId
    ) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NotFoundException("Заказ не найден"));

        entityManager.refresh(order);

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new NotFoundException("Платёж не найден"));

        entityManager.refresh(payment);

        if (!payment.getOrder().getId().equals(order.getId())) {
            throw new IllegalStateException(
                    "Платёж принадлежит другому заказу"
            );
        }

        // Завершённую попытку повторно не обрабатываем.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        // Счёт уже сохранён: его результат проверяется через ЮKassa.
        if (payment.getProviderInvoiceId() != null) {
            return;
        }

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException(
                    "Незавершённая попытка оплаты связана "
                            + "с заказом, который не ожидает оплаты"
            );
        }

        OffsetDateTime expiresAt = order.getPaymentExpiresAt();

        if (expiresAt == null) {
            throw new IllegalStateException(
                    "У заказа отсутствует срок оплаты"
            );
        }

        if (OffsetDateTime.now().isBefore(expiresAt)) {
            return;
        }

        // Этот метод уже возвращает товары в остатки и отменяет заказ.
        orderService.cancelAwaitingPayment(order.getId());

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(OffsetDateTime.now());
    }

//отменить просроченный заказ без платежей
    @Transactional
    public void cancelExpiredOrderWithoutPayments(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new NotFoundException("Заказ не найден")
                );

        entityManager.refresh(order);

        if (order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            return;
        }

        OffsetDateTime expiresAt = order.getPaymentExpiresAt();

        if (expiresAt == null) {
            throw new IllegalStateException(
                    "У заказа отсутствует срок оплаты"
            );
        }

        if (OffsetDateTime.now().isBefore(expiresAt)) {
            return;
        }

        if (paymentRepository.existsByOrder_Id(orderId)) {
            return;
        }

        orderService.cancelAwaitingPayment(orderId);
    }
}