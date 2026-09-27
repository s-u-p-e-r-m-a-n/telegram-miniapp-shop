//страница оформления заказа
import {Navigate, useNavigate} from 'react-router-dom';
import {CheckoutForm} from '../components/CheckoutForm';
import type {CartItem} from '../types/CartItem';
import type {OrderDetails} from '../types/Order';
import {useState} from "react";

type CheckoutPageProps = {
    items: CartItem[];
    initData: string;
    onOrderCreated: (order: OrderDetails) => void;
};

export function CheckoutPage({
                                 items,
                                 initData,
                                 onOrderCreated
                             }: CheckoutPageProps) {

    const navigate = useNavigate();
    const [wasEmptyOnOpen] = useState(items.length === 0);//проверка корзины

    if (wasEmptyOnOpen) {
        return <Navigate to="/cart" replace/>;//если изначально пуста
    }

    return (
        <CheckoutForm
            items={items}
            initData={initData}
            onClose={() => navigate('/cart')}
            onOrderCreated={onOrderCreated}
        />
    );
}