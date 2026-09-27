import {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';

import {getProducts} from '../api/productsApi';
import type {Product} from '../types/Product';
import type {Category} from "../types/Category.ts";
import {getCategories} from "../api/categoriesApi.ts";

type HomePageProps = {
    onAddToCart: (product: Product) => void;
};

export function HomePage({
                             onAddToCart
                         }: HomePageProps) {

    const navigate = useNavigate();

    const [products, setProducts] = useState<Product[]>([]);
    const [categories, setCategories] = useState<Category[]>([]);


    useEffect(() => {
        const loadProducts = async () => {
            try {
                const data = await getProducts();
                setProducts(data);
            } catch (error) {
                console.error('Ошибка загрузки товаров', error);
            }
        };
        const loadCategories = async () => {
            try {
                const data = await getCategories();
                setCategories(data);
            } catch (error) {
                console.error('Ошибка загрузки категорий', error);
            }
        };

        loadProducts();
        loadCategories();


    }, []);

    const popularProducts = products
        .filter(product => product.featured)
        .slice(0, 4);

    const cakesCategory = categories.find(
        category => category.name === 'Торты'
    );

    return (
        <main className="home-page">

            <section className="home-hero">
                <div>
                    <h1>Торты на любой праздник</h1>

                    <p>
                        Натуральные ингредиенты
                        и авторские рецепты
                    </p>

                    <button
                        onClick={() => {
                            if (cakesCategory) {
                                navigate(
                                    `/catalog?categoryId=${cakesCategory.id}`
                                );
                            }
                        }}
                    >
                        Выбрать торт →
                    </button>
                </div>
            </section>

            <section className="home-section">

                <div className="section-header">
                    <h2>Категории</h2>

                    <button
                        className="text-button"
                        onClick={() => navigate('/catalog')}
                    >
                        Все →
                    </button>
                </div>

                <div className="home-categories">
                    {categories.slice(0, 4).map(category => (
                        <button
                            key={category.id}
                            className="home-category-card"
                            onClick={() =>
                                navigate(
                                    `/catalog?categoryId=${category.id}`
                                )
                            }
                        >
                            <span>{category.name}</span>

                            {category.imageUrl && (
                                <img
                                    src={category.imageUrl}
                                    alt={category.name}
                                />
                            )}
                        </button>
                    ))}
                </div>

            </section>

            <section className="home-section">

                <div className="section-header">
                    <h2>Популярное</h2>

                    <button
                        className="text-button"
                        onClick={() => navigate('/catalog')}
                    >
                        Смотреть все →
                    </button>
                </div>

                <div className="home-products">

                    {popularProducts.map(product => (
                        <article
                            key={product.id}
                            className="home-product-card"
                        >
                            <div
                                className="home-product-clickable"
                                onClick={() =>
                                    navigate(`/products/${product.id}`,
                                        {state: {
                                        from: '/'}
                                    })
                                }
                            >
                                {product.images.length > 0 && (
                                    <img
                                        src={product.images[0]}
                                        alt={product.name}
                                    />
                                )}

                                <h3>{product.name}</h3>

                                <strong>
                                    {product.price} ₽
                                </strong>
                            </div>

                            <button
                                className="primary-button"
                                onClick={() =>
                                    onAddToCart(product)
                                }
                            >
                                В корзину
                            </button>
                        </article>
                    ))}

                </div>

            </section>

        </main>
    );
}