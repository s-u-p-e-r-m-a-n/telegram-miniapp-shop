import {useNavigate, useParams} from 'react-router-dom';
import type { OrderDetails } from '../types/Order';
import { getOrderStatusText } from '../types/Order';
import {useEffect, useState} from "react";
import {getMyOrders} from "../api/ordersApi.ts";

type OrderSuccessPageProps = {
    order: OrderDetails | null;
    initData:string;
};

export function OrderSuccessPage({
                                     order,
                                     initData
                                 }: OrderSuccessPageProps) {

    const navigate = useNavigate();

    const { orderId } = useParams();

    const [loadedOrder, setLoadedOrder] = useState<OrderDetails | null>(null);

    const [isLoading, setIsLoading] = useState(order === null);


    useEffect(() => {

        if (order !== null) {
            return;
        }

        const loadOrder = async () => {
            try {
                const orders = await getMyOrders(initData);

                const foundOrder = orders.find(
                    currentOrder =>
                        currentOrder.id === Number(orderId)
                );

                if (foundOrder) {
                    setLoadedOrder(foundOrder);
                }

            } catch (error) {
                console.error(
                    'Не удалось загрузить заказ',
                    error
                );
            } finally {
                setIsLoading(false);
            }
        };

        loadOrder();

    }, [order, orderId, initData]);

    const displayedOrder = order ?? loadedOrder;

    //проверка загрузки
    if (isLoading) {
        return (
            <main className="order-success-page">
                <p>Загрузка заказа...</p>
            </main>
        );
    }

    if (displayedOrder === null) {
        return (
            <main className="order-success-page">
                <div className="order-success-card">

                    <h1>Информация о заказе недоступна</h1>

                    <button
                        className="primary-button"
                        onClick={() => navigate('/orders')}
                    >
                        Мои заказы
                    </button>

                </div>
            </main>
        );
    }



    return (
        <main className="order-success-page">

            <div className="order-success-card">

                <div className="order-success-icon">
                    ✓
                </div>

                <h1>Заказ оформлен</h1>

                <p className="order-success-message">
                    Спасибо за заказ!
                </p>

                <div className="order-success-info">

                    <div>
                        <span>Номер заказа</span>
                        <strong>№{displayedOrder.id}</strong>
                    </div>

                    <div>
                        <span>Сумма</span>
                        <strong>{displayedOrder.totalAmount} ₽</strong>
                    </div>

                    <div>
                        <span>Статус</span>
                        <strong>
                            {getOrderStatusText(displayedOrder.status)}
                        </strong>
                    </div>

                </div>

                <button
                    className="primary-button order-success-primary"
                    onClick={() => navigate('/orders')}
                >
                    Мои заказы
                </button>

                <button
                    className="order-success-secondary"
                    onClick={() => navigate('/catalog')}
                >
                    Продолжить покупки
                </button>

            </div>

        </main>
    );
}