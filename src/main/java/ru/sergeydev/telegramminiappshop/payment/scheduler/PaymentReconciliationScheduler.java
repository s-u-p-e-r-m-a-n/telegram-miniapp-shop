package ru.sergeydev.telegramminiappshop.payment.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sergeydev.telegramminiappshop.order.entity.OrderStatus;
import ru.sergeydev.telegramminiappshop.order.repository.OrderRepository;
import ru.sergeydev.telegramminiappshop.payment.entity.PaymentStatus;
import ru.sergeydev.telegramminiappshop.payment.repository.PaymentRepository;
import ru.sergeydev.telegramminiappshop.payment.service.PaymentPreparationService;
import ru.sergeydev.telegramminiappshop.payment.service.PaymentReconciliationService;
import ru.sergeydev.telegramminiappshop.payment.service.PaymentService;

import java.time.OffsetDateTime;
import java.util.List;


//планировщик проверяет оплату с установленным интервалом времени
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final PaymentPreparationService paymentPreparationService;

    //сервис сверки платежей
    private final PaymentReconciliationService reconciliationService;

    @Scheduled(
            fixedDelay = 60_000,//фиксированная задержка
            initialDelay = 30_000//начальная задержка в мс
    )
    //сверка платежа
    public void reconcilePayments() {

        List<Long> expiredOrderIds = orderRepository
                .findExpiredOrderIdsWithoutPayments(
                        OrderStatus.AWAITING_PAYMENT,
                        OffsetDateTime.now()
                );

        for (Long orderId : expiredOrderIds) {
            try {
                paymentPreparationService
                        .cancelExpiredOrderWithoutPayments(orderId);
            } catch (Exception ex) {
                log.error(
                        "Не удалось отменить просроченный заказ {} без попыток оплаты",
                        orderId,
                        ex
                );
            }
        }

        var paymentIds = paymentRepository
                .findPaymentIdsWithoutInvoiceByStatus(PaymentStatus.PENDING);

        for (Long paymentId : paymentIds) {
            try {
                paymentService.recoverPaymentWithoutInvoice(paymentId);
            } catch (Exception ex) {
                log.error(
                        "Не удалось восстановить попытку оплаты {}",
                        paymentId,
                        ex
                );
            }
        }

        var invoiceIds = paymentRepository.findInvoiceIdsByStatus(
                PaymentStatus.PENDING
        );

        for (String invoiceId : invoiceIds) {
            try {
                reconciliationService.reconcileInvoice(invoiceId);
            } catch (Exception exception) {
                log.error(
                        "Не удалось проверить оплату счёта {}",
                        invoiceId,
                        exception
                );
            }
        }
    }
}