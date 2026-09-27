import { useNavigate } from 'react-router-dom';
import type { Product } from '../types/Product';
import {useLocation} from "react-router-dom";

type ProductCardProps = {
    product: Product;
    onAddToCart: (product: Product) => void;
};

export function ProductCard({
                                product,
                                onAddToCart
                            }: ProductCardProps) {

    const navigate = useNavigate();
    const location = useLocation();

    const mainImage = product.images[0];

    const openProduct = () => {
        navigate(
            `/products/${product.id}`,
            {
                state: {//Добавляет сохраняемое состояние маршрутизации
                    // на стороне клиента к следующему местоположению.
                    from:
                        location.pathname +
                        location.search
                }
            }
        );
    };;

    return (
        <article className="product-card">

            <div
                className="product-card-clickable"
                onClick={openProduct}
            >

                <div className="product-card-image-wrapper">
                    {mainImage && (
                        <img
                            className="product-card-image"
                            src={mainImage}
                            alt={product.name}
                        />
                    )}
                </div>

                <div className="product-card-content">

                    <h3 className="product-card-title">
                        {product.name}
                    </h3>

                    <p className="product-card-description">
                        {product.description}
                    </p>

                    <strong className="product-card-price">
                        {product.price} ₽
                    </strong>

                </div>

            </div>

            <div className="product-card-content">
                <button
                    className="primary-button"
                    onClick={() => onAddToCart(product)}
                >
                    В корзину
                </button>
            </div>

        </article>
    );
}