export type CreateOrderItemRequest = {
    productId: number;
    quantity: number;
};

export type CreateOrderRequest = {
    customerName: string;
    customerPhone: string;
    customerComment: string;
    items: CreateOrderItemRequest[];
};