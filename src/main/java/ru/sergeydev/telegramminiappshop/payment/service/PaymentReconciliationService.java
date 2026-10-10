package ru.sergeydev.telegramminiappshop.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sergeydev.telegramminiappshop.payment.provider.InvoiceProvider;


//Сервис сверки платежей
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private final InvoiceProvider invoiceProvider;
    private final PaymentService paymentService;

    public void reconcileInvoice(String providerInvoiceId) {

        InvoiceProvider.InvoiceInfo invoiceInfo =
                invoiceProvider.getInvoice(providerInvoiceId);

        if (invoiceInfo.status() == InvoiceProvider.InvoiceStatus.PENDING) {
            return;
        }

        if (invoiceInfo.status() == InvoiceProvider.InvoiceStatus.CANCELLED) {
            paymentService.cancelPaymentByInvoice(providerInvoiceId);
            return;
        }

        String providerPaymentId = invoiceInfo.providerPaymentId();

        if (providerPaymentId == null || providerPaymentId.isBlank()) {
            throw new IllegalStateException(
                    "ЮKassa вернула оплаченный счёт без ID платежа"
            );
        }
//если не ожидает оплаты и не отменен вызываем метод о проверки успешной оплаты
        paymentService.markPaymentAsPaid(providerPaymentId);
    }
}