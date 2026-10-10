import { getApiErrorMessage } from './apiErrorHandler';

export type CreatePaymentResponse = {
    paymentId: number;
    status: 'PENDING' | 'PAID' | 'CANCELLED';
    confirmationUrl: string;
};

export async function createPayment(
    orderId: number,
    initData: string,
    idempotencyKey: string
): Promise<CreatePaymentResponse> {

    const response = await fetch(
        `/api/telegram/orders/${orderId}/payments`,
        {
            method: 'POST',
            headers: {
                'X-Telegram-Init-Data': initData,
                'Idempotency-Key': idempotencyKey
            }
        }
    );

    if (!response.ok) {
        const message = await getApiErrorMessage(response);
        throw new Error(message);
    }

    return response.json();
}