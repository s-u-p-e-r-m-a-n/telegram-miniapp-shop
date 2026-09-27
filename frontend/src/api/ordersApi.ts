import type {CreateOrderRequest} from '../types/CreateOrderRequest';
import type { OrderDetails } from '../types/Order';
import { getApiErrorMessage } from './apiErrorHandler';

export async function createOrder(
    request: CreateOrderRequest,
    initData: string,
    idempotencyKey: string
):Promise<OrderDetails> {
    const response = await fetch('/api/orders', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Telegram-Init-Data': initData,
            'Idempotency-Key' : idempotencyKey
        },
        body: JSON.stringify(request)
    });

    if (!response.ok) {
        const message = await getApiErrorMessage(response);
        throw new Error(message);
    }

    return response.json();

}

export async function getMyOrders(
    initData: string
): Promise<OrderDetails[]> {

    const response = await fetch('/api/orders/my', {
        headers: {
            'X-Telegram-Init-Data': initData
        }
    });

    if (!response.ok) {
        throw new Error('Не удалось загрузить заказы');
    }

    return response.json();
}