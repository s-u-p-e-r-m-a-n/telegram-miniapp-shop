import { useEffect, useState } from 'react';

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

        const loadOrders = async () => {
            setErrorMessage(null);
            setIsLoading(true);

            try {
                const data = await getMyOrders(initData);
                setOrders(data);
            } catch (error) {
                console.error('Ошибка загрузки заказов', error);
                setErrorMessage('Не удалось загрузить заказы');
            }finally {
                setIsLoading(false);
            }
        };

        loadOrders();

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