import { useState } from 'react';

type Product = {
    id: number;
    name: string;
};

function App() {
    const telegramWebApp = window.Telegram?.WebApp;

    telegramWebApp?.ready();
    telegramWebApp?.expand();

    const initData = telegramWebApp?.initData ?? '';

    const [result, setResult] = useState('');
    const [products, setProducts] = useState<Product[]>([]);

    const loadProducts = async () => {
        try {
            const response = await fetch('/api/products');

            const data = await response.json();

            setProducts(data);
            setResult(JSON.stringify(data, null, 2));
        } catch (error) {
            setResult('Ошибка запроса к backend');
        }
    };

    const createTestOrder = async () => {
        if (products.length === 0) {
            setResult('Сначала загрузите товары');
            return;
        }

        try {
            const response = await fetch('/api/orders', {
                method: 'POST',

                headers: {
                    'Content-Type': 'application/json',
                    'X-Telegram-Init-Data': initData,
                },

                body: JSON.stringify({
                    customerName: 'Тестовый клиент',
                    customerPhone: '+79990000000',
                    customerComment: 'Тест заказа из Mini App',

                    items: [
                        {
                            productId: products[0].id,
                            quantity: 1,
                        },
                    ],
                }),
            });

            const data = await response.json();

            setResult(JSON.stringify(data, null, 2));
        } catch (error) {
            setResult('Ошибка создания заказа');
        }
    };

    return (
        <div>
            <h1>Telegram Mini App Shop</h1>

            <p>initData:</p>
            <p>{initData || 'initData отсутствует'}</p>

            <button onClick={loadProducts}>
                Проверить товары
            </button>

            <button onClick={createTestOrder}>
                Создать тестовый заказ
            </button>

            <pre>{result}</pre>
        </div>
    );
}

export default App;