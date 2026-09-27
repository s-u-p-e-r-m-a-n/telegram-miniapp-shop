import { NavLink } from 'react-router-dom';
import {
    House,
    Search,
    ShoppingBag,
    ReceiptText
} from 'lucide-react';

type BottomNavigationProps = {
    cartItemsCount: number;
};

export function BottomNavigation({
                                     cartItemsCount
                                 }: BottomNavigationProps) {

    const getNavClass = ({ isActive }: { isActive: boolean }) =>
        isActive ? 'nav-item active' : 'nav-item';

    return (
        <nav className="bottom-navigation">

            <NavLink to="/" className={getNavClass}>
                <House size={22} />
                <span>Главная</span>
            </NavLink>

            <NavLink to="/catalog" className={getNavClass}>
                <Search size={22} />
                <span>Каталог</span>
            </NavLink>

            <NavLink to="/cart" className={getNavClass}>
                <div className="nav-icon-wrapper">
                    <ShoppingBag size={22} />

                    {cartItemsCount > 0 && (
                        <span className="cart-badge">
                            {cartItemsCount}
                        </span>
                    )}
                </div>

                <span>Корзина</span>
            </NavLink>

            <NavLink to="/orders" className={getNavClass}>
                <ReceiptText size={22} />
                <span>Заказы</span>
            </NavLink>

        </nav>
    );
}