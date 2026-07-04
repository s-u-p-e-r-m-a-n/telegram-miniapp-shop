package ru.sergeydev.telegramminiappshop.order.entity;

public enum OrderStatus {
    NEW,        // новый заказ
    IN_WORK,   // заказ в работе
    DONE,      // заказ выполнен
    CANCELLED  // заказ отменён
}