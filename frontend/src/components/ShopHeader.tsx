type ShopHeaderProps = {
    cartItemsCount: number;
    onCartClick: () => void;
};

export function ShopHeader({cartItemsCount,onCartClick}: ShopHeaderProps) {
    return (
        <header>
            <h1>Магазин</h1>

            <nav>
                <button>Мои заказы</button>
                <button onClick={onCartClick}>
                    Корзина ({cartItemsCount})
                </button>
            </nav>
        </header>
    );
}