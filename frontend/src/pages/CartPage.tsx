import { useNavigate } from 'react-router-dom';
import { Cart } from '../components/Cart';
import type { CartItem } from '../types/CartItem';

type CartPageProps = {
    items: CartItem[];
    onChangeQuantity: (
        productId: number,
        change: number
    ) => void;
};

export function CartPage({
                             items,
                             onChangeQuantity
                         }: CartPageProps) {

    const navigate = useNavigate();

    return (
        <Cart
            items={items}
            onChangeQuantity={onChangeQuantity}
            onClose={() => navigate('/')}
            onCheckout={() => navigate('/checkout')}
        />
    );
}