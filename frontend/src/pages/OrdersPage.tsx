import { useEffect, useState } from 'react';
import {PaymentCountdown} from '../components/PaymentCountdown';
import { getMyOrders } from '../api/ordersApi';
import {getOrderStatusText, type OrderDetails} from '../types/Order';
import { createPayment } from '../api/paymentsApi';

type OrdersPageProps = {
    initData: string;
};

export function OrdersPage({
                               initData
                           }: OrdersPageProps) {

    const [orders, setOrders] = useState<OrderDetails[]>([]);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);//запрос еще идет true
    const [refreshError, setRefreshError] = useState<string | null>(null);//хранит текст ошибки обновления либо null
    const [payingOrderId, setPayingOrderId] = useState<number | null>(null);
    const [paymentUrls, setPaymentUrls] = useState<Record<number, string>>({});
    const [paymentErrors, setPaymentErrors] = useState<Record<number, string>>({});


    const handleCreatePayment = async (orderId: number) => {
        if (payingOrderId !== null) {
            return;
        }

        setPayingOrderId(orderId);
        setPaymentErrors(current => ({
            ...current,
            [orderId]: ''
        }));

        try {
            const payment = await createPayment(
                orderId,
                initData,
                crypto.randomUUID()
            );

            const paymentUrl = new URL(payment.confirmationUrl);

            if (paymentUrl.protocol !== 'https:') {
                throw new Error('Получена некорректная ссылка на оплату');
            }

            setPaymentUrls(current => ({
                ...current,
                [orderId]: paymentUrl.toString()
            }));
        } catch (error) {
            const message = error instanceof Error
                ? error.message
                : 'Не удалось создать ссылку на оплату';

            setPaymentErrors(current => ({
                ...current,
                [orderId]: message
            }));
        } finally {
            setPayingOrderId(null);
        }
    };
// Загрузка заказов и настройка повторных запросов
    useEffect(() => {

        setRefreshError(null);
        let stopped = false;
        //Переменная для хранения идентификатора таймера
        let timeoutId: ReturnType<typeof setTimeout> | undefined;
        //была ли хотя бы одна успешная загрузка в рамках этого эффекта.
        let hasLoaded = false;

        setIsLoading(true);
        setErrorMessage(null);

        const loadOrders = async () => {
            try {
                const data = await getMyOrders(initData);

                if (stopped) {
                    return;
                }

                setOrders(data);
                setRefreshError(null);
                setErrorMessage(null);
                hasLoaded = true;
            } catch (error) {
                if (stopped) {
                    return;
                }

                console.error('Ошибка загрузки заказов', error);

                // Если данные уже показаны, сохраняем их на экране.
                if (!hasLoaded) {
                    setErrorMessage('Не удалось загрузить заказы');
                } else {
                    setRefreshError(
                        'Не удалось обновить заказы. '
                        + 'Показанные статусы могут быть устаревшими. '
                        + 'Повторяем попытку…'
                    );
                }
            } finally {
                if (!stopped) {
                    setIsLoading(false);

                    timeoutId = setTimeout(loadOrders, 10_000);
                }
            }
        };

        void loadOrders();

        return () => {
            stopped = true;
            clearTimeout(timeoutId);
        };
    }, [initData]);

    return (
        <main className="orders-page">

            <h1>Мои заказы</h1>

            {isLoading && (
                <p>Загрузка...</p>
            )}

            {errorMessage && (
                <p>{errorMessage}</p>
            )}
            {refreshError && (
                <p role="status">
                    {refreshError}
                </p>
            )}
            {!isLoading && !errorMessage && orders.length === 0 && (
                <p>Заказов пока нет</p>
            )}



            <div className="orders-list">

                {!isLoading && !errorMessage && orders.map( order=> (

                    <article
                        key={order.id}
                        className="order-card"
                    >

                        <div className="order-card-header">
                            <div>
                                <h2>Заказ №{order.id}</h2>

                                <span className="order-date">
            {new Date(order.createdAt).toLocaleString(
                'ru-RU',
                {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                }
            )}
        </span>
                            </div>

                            <span className="order-status">
        {getOrderStatusText(order.status)}
    </span>
                        </div>

                        {order.status === 'AWAITING_PAYMENT'
                            && order.paymentExpiresAt && (
                                <PaymentCountdown
                                    expiresAt={order.paymentExpiresAt}
                                />
                            )}
                        {order.status === 'AWAITING_PAYMENT' && (
                            <>
                                {!paymentUrls[order.id] && (
                                    <button
                                        className="primary-button"
                                        onClick={() => void handleCreatePayment(order.id)}
                                        disabled={payingOrderId !== null}
                                    >
                                        {payingOrderId === order.id
                                            ? 'Готовим ссылку...'
                                            : 'Оплатить'}
                                    </button>
                                )}

                                {paymentUrls[order.id] && (
                                    <a
                                        className="primary-button order-payment-link"
                                        href={paymentUrls[order.id]}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                    >
                                        Перейти к оплате
                                    </a>
                                )}

                                {paymentErrors[order.id] && (
                                    <p className="checkout-error" role="alert">
                                        {paymentErrors[order.id]}
                                    </p>
                                )}
                            </>
                        )}
                        <div className="order-items">

                            {order.items.map(item => (
                                <div
                                    key={item.productId}
                                    className="order-item-row"
                                >
                                    <div>
                                        <strong>
                                            {item.productName}
                                        </strong>

                                        <span>
                                            {item.quantity} шт.
                                        </span>
                                    </div>

                                    <strong>
                                        {item.totalPrice} ₽
                                    </strong>
                                </div>
                            ))}

                        </div>

                        <div className="order-total">
                            <span>Итого</span>

                            <strong>
                                {order.totalAmount} ₽
                            </strong>
                        </div>

                    </article>

                ))}

            </div>

        </main>
    );
}