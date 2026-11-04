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

    if (
        import.meta.env.DEV
        && import.meta.env.VITE_PAYMENT_LINK_DEMO === 'true'
    ) {
        return {
            paymentId: 0,
            status: 'PENDING',
            confirmationUrl: `https://example.com/?demoPayment=${orderId}`
        };
    }

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