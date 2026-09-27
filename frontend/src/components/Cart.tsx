import type { CartItem } from '../types/CartItem';

type CartProps = {
    items: CartItem[];
    onClose: () => void;
    onChangeQuantity: (
        productId: number,
        change: number
    ) => void;
    onCheckout: () => void;
};

export function Cart({
                         items,
                         onClose,
                         onChangeQuantity,
                         onCheckout
                     }: CartProps) {

    const totalPrice = items.reduce(
        (total, item) =>
            total + item.product.price * item.quantity,
        0
    );

    return (
        <main className="cart-page">

            <h1>Корзина</h1>

            <div className="cart-items">
                {items.map(item => (
                    <article
                        key={item.product.id}
                        className="cart-item"
                    >
                        <img
                            className="cart-item-image"
                            src={item.product.images[0]}
                            alt={item.product.name}
                        />

                        <div className="cart-item-info">

                            <h2>{item.product.name}</h2>

                            <strong>
                                {item.product.price} ₽
                            </strong>

                            <div className="cart-quantity">

                                <button
                                    onClick={() =>
                                        onChangeQuantity(
                                            item.product.id,
                                            -1
                                        )
                                    }
                                >
                                    −
                                </button>

                                <span>{item.quantity}</span>

                                <button
                                    onClick={() =>
                                        onChangeQuantity(
                                            item.product.id,
                                            1
                                        )
                                    }
                                >
                                    +
                                </button>

                            </div>

                        </div>
                    </article>
                ))}
            </div>

            <div className="cart-summary">
                <span>Итого</span>

                <strong>
                    {totalPrice} ₽
                </strong>
            </div>

            <button
                className="primary-button cart-checkout-button"
                disabled={items.length === 0}
                onClick={onCheckout}
            >
                Оформить заказ
            </button>

            <button
                className="cart-back-button"
                onClick={onClose}
            >
                Продолжить покупки
            </button>

        </main>
    );
}