package ru.sergeydev.telegramminiappshop.payment.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sergeydev.telegramminiappshop.order.entity.Order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // К какому заказу относится попытка оплаты
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // ID платежа, который вернёт YooKassa
    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    // ID счёта ЮKassa, по которому клиент переходит к оплате
    @Column(name = "provider_invoice_id")
    private String providerInvoiceId;

    // Защита от повторного создания одной попытки платежа
    @Column(name = "idempotency_key", nullable = false)
    private UUID idempotencyKey;

    // Сумма берётся только из Order на backend
    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // Ссылка YooKassa, куда отправляется клиент платить
    @Column(name = "confirmation_url")
    private String confirmationUrl;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Время фактического подтверждения оплаты
    @Column(name = "paid_at")
    private OffsetDateTime paidAt;
}