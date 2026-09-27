import { useState } from 'react';

import type { CartItem } from '../types/CartItem';
import type { CreateOrderRequest } from '../types/CreateOrderRequest';
import type { OrderDetails } from '../types/Order';

import { createOrder } from '../api/ordersApi';

type CheckoutFormProps = {
    items: CartItem[];
    initData: string;
    onClose: () => void;
    onOrderCreated: (order: OrderDetails) => void;
};

export function CheckoutForm({
                                 items,
                                 initData,
                                 onClose,
                                 onOrderCreated
                             }: CheckoutFormProps) {

    const [customerName, setCustomerName] = useState('');
    const [customerPhone, setCustomerPhone] = useState('');
    const [customerComment, setCustomerComment] = useState('');
    const [idempotencyKey] = useState(
        () => crypto.randomUUID()
    );

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    const [isSubmitting, setIsSubmitting] = useState(false);//защита от повторного нажатия, false не нажималась

    const totalPrice = items.reduce(
        (total, item) =>
            total + item.product.price * item.quantity,
        0
    );

    const buildOrderRequest = (): CreateOrderRequest => {
        return {
            customerName: customerName,
            customerPhone: customerPhone,
            customerComment: customerComment,

            items: items.map(item => ({
                productId: item.product.id,
                quantity: item.quantity
            }))
        };
    };

    const submitOrder = async () => {

        if (isSubmitting) {
            return;
        }

        setErrorMessage(null);
        setIsSubmitting(true);

        try {

            const request = buildOrderRequest();

            const order = await createOrder(
                request,
                initData,
                idempotencyKey
            );

            onOrderCreated(order);

        } catch (error) {

            if (error instanceof Error) {
                setErrorMessage(error.message);
            } else {
                setErrorMessage(
                    'Не удалось оформить заказ'
                );
            }

        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <main className="checkout-page">

            <h1>Оформление заказа</h1>

            <div className="checkout-form">

                <label className="checkout-field">
                    <span>Имя</span>

                    <input
                        type="text"
                        placeholder="Ваше имя"
                        value={customerName}
                        onChange={event =>
                            setCustomerName(
                                event.target.value
                            )
                        }
                    />
                </label>

                <label className="checkout-field">
                    <span>Телефон</span>

                    <input
                        type="tel"
                        placeholder="+7 999 123-45-67"
                        value={customerPhone}
                        onChange={event =>
                            setCustomerPhone(
                                event.target.value
                            )
                        }
                    />
                </label>

                <label className="checkout-field">
                    <span>Комментарий</span>

                    <textarea
                        placeholder="Например: позвонить перед доставкой"
                        maxLength={1000}
                        value={customerComment}
                        onChange={event =>
                            setCustomerComment(
                                event.target.value
                            )
                        }
                    />
                </label>

            </div>

            {errorMessage && (
                <p className="checkout-error">
                    {errorMessage}
                </p>
            )}

            <div className="checkout-total">
                <span>Итого</span>

                <strong>{totalPrice} ₽</strong>
            </div>

            <button
                className="primary-button checkout-submit"
                onClick={submitOrder}
                disabled={isSubmitting}
            >
                {isSubmitting
                    ? 'Оформляем...'
                    : 'Подтвердить заказ'}
            </button>

            <button
                className="checkout-back"
                onClick={onClose}
            >
                Назад в корзину
            </button>

        </main>
    );
}