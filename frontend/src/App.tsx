import {ShopPage} from "./pages/ShopPage.tsx";
import {Route, Routes, useNavigate} from 'react-router-dom';
import {useEffect, useState} from 'react';
import type {Product} from './types/Product';
import type {CartItem} from './types/CartItem';
import {CartPage} from "./pages/CartPage.tsx";
import {CheckoutPage} from "./pages/CheckoutPage.tsx";
import {HomePage} from './pages/HomePage';
import {BottomNavigation} from './components/BottomNavigation';
import {OrdersPage} from './pages/OrdersPage';
import type {OrderDetails} from "./types/Order.ts";
import {OrderSuccessPage} from './pages/OrderSuccessPage';
import {ProductDetailsPage} from "./pages/ProductDetailsPage.tsx";

// содержит корзину и выступает в качестве контроллера (маршрутизатора)
function App() {
    const telegramWebApp = window.Telegram?.WebApp;
    telegramWebApp?.ready();
    telegramWebApp?.expand();
    const initData = telegramWebApp?.initData ?? '';
    const [cartItems, setCartItems] = useState<CartItem[]>(() => {
        const savedCart = localStorage.getItem('cartItems');

        if (!savedCart) {
            return [];
        }

        try {
            return JSON.parse(savedCart) as CartItem[];
        } catch {
            return [];// в случае битого json
        }
    });// корзина

    useEffect(() => {
        localStorage.setItem(// сохраняем корзину в localStorage
            'cartItems',
            JSON.stringify(cartItems)
        );
    }, [cartItems]);
    const [createdOrder, setCreatedOrder] = useState<OrderDetails | null>(null);
    const navigate = useNavigate();


    const handleOrderCreated = (order: OrderDetails) => {
        setCreatedOrder(order);
        setCartItems([]);//очищает корзину
        navigate(`/order-success/${order.id}`);
    };
    const cartItemsCount = cartItems.reduce(
        (total, item) => total + item.quantity,
        0
    );


    const addToCart = (
        product: Product,
        quantity: number = 1
    ) => {

        setCartItems(currentItems => {

            const existingItem = currentItems.find(
                item => item.product.id === product.id
            );

            if (existingItem) {
                return currentItems.map(item =>
                    item.product.id === product.id
                        ? {
                            ...item,
                            quantity: item.quantity + quantity
                        }
                        : item
                );
            }

            return [
                ...currentItems,
                {
                    product,
                    quantity
                }
            ];
        });
    };

    const changeCartItemQuantity = (
        productId: number,
        change: number
    ) => {
        setCartItems(currentItems =>
            currentItems
                .map(item =>
                    item.product.id === productId
                        ? {
                            ...item,
                            quantity: item.quantity + change
                        }
                        : item
                )
                .filter(item => item.quantity > 0)
        );
    };

    return (
        <>
            <Routes>
                <Route
                    path="/"
                    element={
                        <HomePage
                            onAddToCart={addToCart}
                        />
                    }
                />
                <Route
                    path="/catalog"
                    element={
                        <ShopPage
                            onAddToCart={addToCart}
                        />
                    }
                />
                <Route
                    path="/cart"
                    element={
                        <CartPage
                            items={cartItems}
                            onChangeQuantity={changeCartItemQuantity}
                        />
                    }
                />
                <Route
                    path="/checkout"
                    element={
                        <CheckoutPage
                            items={cartItems}
                            initData={initData}
                            onOrderCreated={handleOrderCreated}
                        />
                    }
                />
                <Route
                    path="/orders"
                    element={<OrdersPage initData={initData}
                    />}
                />
                <Route
                    path="/order-success/:orderId"
                    element={
                        <OrderSuccessPage
                            order={createdOrder}
                            initData={initData}
                        />
                    }
                />
                <Route
                    path="/products/:productId"
                    element={
                        <ProductDetailsPage
                            onAddToCart={addToCart}
                        />
                    }
                />
            </Routes>
            <BottomNavigation
                cartItemsCount={cartItemsCount}
            /></>
    );
}

export default App;