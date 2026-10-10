import { useEffect, useState } from 'react';
import {PaymentCountdown} from '../components/PaymentCountdown';
import { getMyOrders } from '../api/ordersApi';
import {
    getOrderStatusText,
    type OrderDetails
} from '../types/Order';

type OrdersPageProps = {
    initData: string;
};

export function OrdersPage({
                               initData
                           }: OrdersPageProps) {

    const [orders, setOrders] = useState<OrderDetails[]>([]);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);//запрос еще идет true

    useEffect(() => {
        let stopped = false;
        let timeoutId: ReturnType<typeof setTimeout> | undefined;
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
                                <h2>
                                    Заказ №{order.id}
                                </h2>

                                <span className="order-date">
                                    {new Date(
                                        order.createdAt
                                    ).toLocaleString(
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
                                {getOrderStatusText(
                                    order.status
                                )}
                            </span>
                            {order.status === 'AWAITING_PAYMENT'//таймер оплаты
                                && order.paymentExpiresAt && (
                                    <PaymentCountdown
                                        expiresAt={order.paymentExpiresAt}
                                    />
                                )}

                        </div>

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