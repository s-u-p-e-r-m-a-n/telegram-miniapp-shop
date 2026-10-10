package ru.sergeydev.telegramminiappshop.payment.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sergeydev.telegramminiappshop.common.exception.NotFoundException;
import ru.sergeydev.telegramminiappshop.order.entity.Order;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.repository.OrderRepository;
import ru.sergeydev.telegramminiappshop.order.service.OrderService;
import ru.sergeydev.telegramminiappshop.payment.entity.Payment;
import ru.sergeydev.telegramminiappshop.payment.entity.PaymentStatus;
import ru.sergeydev.telegramminiappshop.payment.provider.InvoiceProvider;
import ru.sergeydev.telegramminiappshop.payment.provider.PaymentProvider;
import ru.sergeydev.telegramminiappshop.payment.repository.PaymentRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderService orderService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentProvider paymentProvider;
    private final InvoiceProvider invoiceProvider;
    private final EntityManager entityManager;
    private final PaymentPreparationService paymentPreparationService;

    public Payment createPayment(
            Long orderId,
            UUID idempotencyKey
    ) {
        // Первая транзакция: сохраняем попытку и её ключ.
        Long paymentId = paymentPreparationService.preparePayment(
                orderId,
                idempotencyKey
        );

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Платёж не найден"));

        // Для завершённой попытки новый счёт не создаём.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return payment;
        }

        // Счёт уже создан и сохранён — возвращаем имеющуюся ссылку.
        if (payment.getProviderInvoiceId() != null) {
            if (payment.getConfirmationUrl() == null
                    || payment.getConfirmationUrl().isBlank()) {
                throw new IllegalStateException(
                        "У сохранённого счёта отсутствует ссылка на оплату"
                );
            }

            return payment;
        }

        // Получаем данные заказа и сохранённый ключ этой попытки.
        var data = paymentPreparationService.getInvoiceCreationData(
                paymentId
        );

        // Сетевой запрос выполняется вне транзакций БД.
        InvoiceProvider.CreatedInvoice createdInvoice =
                invoiceProvider.createInvoice(
                        data.orderId(),
                        data.amount(),
                        data.items(),
                        data.expiresAt(),
                        data.idempotencyKey()
                );

        // Вторая транзакция: сохраняем результат в тот же Payment.
        return paymentPreparationService.saveCreatedInvoice(
                data.orderId(),
                data.paymentId(),
                createdInvoice
        );
    }
    //отметить платеж, как оплаченный
    @Transactional
    public void markPaymentAsPaid(String providerPaymentId) {

        PaymentProvider.PaymentInfo paymentInfo =
                paymentProvider.getPayment(providerPaymentId);

        if (paymentInfo.status() != PaymentStatus.PAID) {
            throw new IllegalStateException(
                    "Провайдер не подтвердил успешную оплату"
            );
        }

        String invoiceId = paymentInfo.providerInvoiceId();
        boolean hasInvoice = invoiceId != null && !invoiceId.isBlank();

        // Сначала узнаём, к какому заказу относится оплата.
        Long orderId = (hasInvoice
                ? paymentRepository.findOrderIdByProviderInvoiceId(invoiceId)
                : paymentRepository.findOrderIdByProviderPaymentId(providerPaymentId))
                .orElseThrow(() ->
                        new NotFoundException("Платёж не найден")
                );

// Сначала блокируем заказ.
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new NotFoundException("Заказ не найден")
                );

        entityManager.refresh(order);

// Затем блокируем оплату.
        Payment payment = (hasInvoice
                ? paymentRepository.findByProviderInvoiceIdForUpdate(invoiceId)
                : paymentRepository.findByProviderPaymentIdForUpdate(providerPaymentId))
                .orElseThrow(() ->
                        new NotFoundException("Платёж не найден")
                );

        entityManager.refresh(payment);

        if (!hasInvoice && payment.getProviderInvoiceId() != null) {
            throw new IllegalStateException(
                    "В ответе провайдера отсутствует связь платежа со счётом"
            );
        }

        if (payment.getProviderPaymentId() != null
                && !payment.getProviderPaymentId().equals(providerPaymentId)) {
            throw new IllegalStateException(
                    "С записью оплаты уже связан другой платёж"
            );
        }

        if (paymentInfo.amount().compareTo(payment.getAmount()) != 0
                || paymentInfo.amount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalStateException(
                    "Сумма платежа не совпадает с суммой заказа"
            );
        }

        if (!"RUB".equals(paymentInfo.currency())) {
            throw new IllegalStateException(
                    "Некорректная валюта платежа"
            );
        }

        // Повторное подтверждение — ничего не меняем.
        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING
                || order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException(
                    "Оплата подтверждена, но состояние заказа требует проверки"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        payment.setProviderPaymentId(providerPaymentId);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(now);
        payment.setUpdatedAt(now);

        orderService.markOrderAsPaid(order.getId());
    }

    @Transactional
    public void cancelPaymentByInvoice(String providerInvoiceId) {

        InvoiceProvider.InvoiceInfo invoiceInfo =
                invoiceProvider.getInvoice(providerInvoiceId);

        if (invoiceInfo.status() != InvoiceProvider.InvoiceStatus.CANCELLED) {
            return;
        }

        Long orderId = paymentRepository
                .findOrderIdByProviderInvoiceId(providerInvoiceId)
                .orElseThrow(() ->
                        new NotFoundException("Платёж не найден")
                );

        Order order = orderRepository
                .findByIdForUpdate(orderId)
                .orElseThrow(() ->
                        new NotFoundException("Заказ не найден")
                );

        entityManager.refresh(order);

        Payment payment = paymentRepository
                .findByProviderInvoiceIdForUpdate(providerInvoiceId)
                .orElseThrow(() ->
                        new NotFoundException("Платёж не найден")
                );

        entityManager.refresh(payment);

        // Эта отмена уже полностью выполнена.
        if (payment.getStatus() == PaymentStatus.CANCELLED
                && order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        if (payment.getStatus() != PaymentStatus.PENDING
                || order.getStatus() != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException(
                    "Счёт отменён, но состояние оплаты или заказа требует проверки"
            );
        }

        orderService.cancelAwaitingPayment(order.getId());

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(OffsetDateTime.now());
    }
//восстановить платеж без счета
    public void recoverPaymentWithoutInvoice(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Платёж не найден"));

        // Пока очередь дошла до этой попытки, её могли завершить.
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        // Другой запрос уже мог сохранить счёт.
        if (payment.getProviderInvoiceId() != null) {
            return;
        }

        var data = paymentPreparationService.getInvoiceCreationData(
                paymentId
        );

        if (!OffsetDateTime.now().isBefore(data.expiresAt())) {
            paymentPreparationService.cancelExpiredPaymentWithoutInvoice(
                    data.orderId(),
                    data.paymentId()
            );
            return;
        }

        // Используем ключ сохранённой попытки.
        createPayment(
                data.orderId(),
                data.idempotencyKey()
        );
    }
}