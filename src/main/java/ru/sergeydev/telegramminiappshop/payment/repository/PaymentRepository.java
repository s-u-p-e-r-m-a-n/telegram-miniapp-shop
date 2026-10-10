package ru.sergeydev.telegramminiappshop.payment.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sergeydev.telegramminiappshop.payment.entity.Payment;
import ru.sergeydev.telegramminiappshop.payment.entity.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(UUID idempotencyKey);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    //Найти по идентификатору заказа сортировать по созданию в упорядоченном виде
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)//блокировка строки Payment в БД
    @Query("""
        select p
        from Payment p
        where p.providerPaymentId = :providerPaymentId
        """)
    Optional<Payment> findByProviderPaymentIdForUpdate(
            @Param("providerPaymentId") String providerPaymentId
    );
//защита от повторных оплат одного и того же заказ,получаем только крайнюю запись об ожидании оплаты
    Optional<Payment> findFirstByOrderIdAndStatusOrderByCreatedAtDesc(
            Long orderId,
            PaymentStatus status
    );

//Найти по идентификатору счета поставщика для обновления
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select p
    from Payment p
    where p.providerInvoiceId = :providerInvoiceId
    """)
    Optional<Payment> findByProviderInvoiceIdForUpdate(
            @Param("providerInvoiceId") String providerInvoiceId
    );
//узнать ID заказа до установки блокировок.
    Optional<Payment> findByProviderInvoiceId(String providerInvoiceId);

//получаем счета ожидающие оплаты
    @Query("""
select p.providerInvoiceId
        from Payment p
        where p.status = :status
          and p.providerInvoiceId is not null
        order by p.id
        """)
    List<String> findInvoiceIdsByStatus(
            @Param("status") PaymentStatus status
    );


    @Query("""
        select p.order.id
        from Payment p
        where p.providerInvoiceId = :providerInvoiceId
        """)
    Optional<Long> findOrderIdByProviderInvoiceId(
            @Param("providerInvoiceId") String providerInvoiceId
    );

    @Query("""
        select p.order.id
        from Payment p
        where p.providerPaymentId = :providerPaymentId
        """)
    Optional<Long> findOrderIdByProviderPaymentId(
            @Param("providerPaymentId") String providerPaymentId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p
        from Payment p
        where p.id = :paymentId
        """)
    Optional<Payment> findByIdForUpdate(
            @Param("paymentId") Long paymentId
    );

    @Query("""
        select p.id
        from Payment p
        where p.status = :status
          and p.providerInvoiceId is null
        order by p.id
        """)
    List<Long> findPaymentIdsWithoutInvoiceByStatus(
            @Param("status") PaymentStatus status
    );

}