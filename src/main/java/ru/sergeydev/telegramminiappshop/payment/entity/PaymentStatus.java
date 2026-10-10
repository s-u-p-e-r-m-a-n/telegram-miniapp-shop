package ru.sergeydev.telegramminiappshop.payment.entity;

public enum PaymentStatus {

    PENDING,                              //платёж создан, ждём оплаты
    PAID,                                //ЮKassa подтвердила успешную оплату
    CANCELLED                            //платёж отменён / не состоялся
}
