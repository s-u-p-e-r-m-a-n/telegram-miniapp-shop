import {useEffect, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import {useLocation} from 'react-router-dom';
import {getProductById} from '../api/productsApi';
import type {Product} from '../types/Product';

type ProductDetailsPageProps = {
    onAddToCart: (
        product: Product,
        quantity: number
    ) => void;
};

export function ProductDetailsPage({
                                       onAddToCart
                                   }: ProductDetailsPageProps) {

    const {productId} = useParams();
    const navigate = useNavigate();
    const [quantity, setQuantity] = useState(1);
    const location = useLocation();

    const decreaseQuantity = () => {
        setQuantity(current =>
            Math.max(1, current - 1)
        );
    };

    const increaseQuantity = () => {
        setQuantity(current =>
            current + 1
        );
    };

    const [product, setProduct] =
        useState<Product | null>(null);

    const [errorMessage, setErrorMessage] =
        useState<string | null>(null);

    useEffect(() => {

        const loadProduct = async () => {

            if (!productId) {
                setErrorMessage('Товар не найден');
                return;
            }

            try {
                const data = await getProductById(
                    Number(productId)
                );

                setProduct(data);

            } catch (error) {

                if (error instanceof Error) {
                    setErrorMessage(error.message);
                } else {
                    setErrorMessage(
                        'Не удалось загрузить товар'
                    );
                }
            }
        };

        loadProduct();

    }, [productId]);

    if (errorMessage) {
        return (
            <main>
                <p>{errorMessage}</p>
            </main>
        );
    }

    if (!product) {
        return (
            <main>
                <p>Загрузка...</p>
            </main>
        );
    }

    return (
        <main className="product-details-page">

            <button
                className="product-back-button"
                onClick={() =>
                    navigate(
                        location.state?.from ?? '/catalog'
                    )
                }
            >
                ← Назад
            </button>

            {product.images[0] && (
                <img
                    className="product-details-image"
                    src={product.images[0]}
                    alt={product.name}
                />
            )}

            <div className="product-details-content">

                <p className="product-details-category">
                    {product.categoryName}
                </p>

                <h1>{product.name}</h1>

                <p className="product-details-description">
                    {product.description}
                </p>

                <strong className="product-details-price">
                    {product.price} ₽
                </strong>

                <div className="product-quantity">

                    <button onClick={decreaseQuantity}>
                        −
                    </button>

                    <span>{quantity}</span>

                    <button onClick={increaseQuantity}>
                        +
                    </button>

                </div>
                <button
                    className="primary-button"
                    onClick={() =>
                        onAddToCart(product, quantity)
                    }
                >
                    В корзину
                </button>

            </div>

        </main>
    );
}