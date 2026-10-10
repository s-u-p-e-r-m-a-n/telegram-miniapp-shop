export type OrderStatus =
    | 'AWAITING_PAYMENT'
    | 'NEW'
    | 'IN_WORK'
    | 'DONE'
    | 'CANCELLED';

export type OrderItem = {
    productId: number;
    productName: string;
    productPrice: number;
    quantity: number;
    totalPrice: number;
};

export type OrderDetails = {
    id: number;
    status: OrderStatus;
    totalAmount: number;
    createdAt: string;
    paymentExpiresAt: string | null;
    items: OrderItem[];
};

export function getOrderStatusText(
    status: OrderStatus
): string {
    switch (status) {
        case 'AWAITING_PAYMENT':
            return 'Ожидает оплаты';
        case 'NEW':
            return 'Новый';

        case 'IN_WORK':
            return 'Принят в работу';

        case 'DONE':
            return 'Выполнен';

        case 'CANCELLED':
            return 'Отменён';
    }
}