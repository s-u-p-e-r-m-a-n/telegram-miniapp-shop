import {useEffect, useState} from 'react';
import type {Product} from '../types/Product';
import {getProducts} from '../api/productsApi';
import {ProductCard} from '../components/ProductCard';
import {ProductSearch} from '../components/ProductSearch';
import {useSearchParams} from "react-router-dom";

type ShopPageProps = {
    onAddToCart: (product: Product) => void;
};

export function ShopPage({
                             onAddToCart
                         }: ShopPageProps) {
    const [products, setProducts] = useState<Product[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    //<number | null> null-все товары или id товара
    const [searchParams, setSearchParams] = useSearchParams();

    const categoryIdParam = searchParams.get('categoryId');

    const selectedCategoryId = categoryIdParam !== null
        ? Number(categoryIdParam)
        : null;


    useEffect(() => {
        const loadProducts = async () => {
            setErrorMessage(null);
            setIsLoading(true);
            try {
                const data = await getProducts();
                setProducts(data);
            } catch (error) {
                console.error('Ошибка загрузки товаров', error);
                setErrorMessage('Не удалось загрузить товары');
            } finally {
                setIsLoading(false);
            }
        };

        loadProducts();
    }, []);


    const categories = Array.from(
        new Map(
            products.map(product => [
                product.categoryId,
                product.categoryName
            ])
        )
    );

    const filteredProducts = products.filter(product => {
        const matchesSearch = product.name
            .toLowerCase()
            .includes(searchQuery.toLowerCase());

// категории не выбраны null показываем все или по id категории, фильтр показывает только результат true
        const matchesCategory =
            selectedCategoryId === null || product.categoryId === selectedCategoryId;

        return matchesSearch && matchesCategory;
    });

    return (
        <div>
            <ProductSearch
                value={searchQuery}
                onChange={setSearchQuery}
            />

            <div className="catalog-categories">

                <button
                    className={selectedCategoryId === null ? 'category-chip active' : 'category-chip'}
                    onClick={() => setSearchParams({})}
                >
                    Все
                </button>

                {categories.map(([categoryId, categoryName]) => (
                    <button
                        key={categoryId}
                        className={
                            selectedCategoryId === categoryId
                                ? 'category-chip active'
                                : 'category-chip'
                        }
                        onClick={() => setSearchParams({
                            categoryId: String(categoryId)
                        })
                        }
                    >
                        {categoryName}
                    </button>
                ))}

            </div>

            {isLoading && (
                <p>Загрузка...</p>
            )}
            {errorMessage && (
                <p>{errorMessage}</p>
            )}
            {!errorMessage && !isLoading && filteredProducts.length === 0 && (
                <p>Товары не найдены</p>
            )}
            <div className="product-grid">


                {filteredProducts.map(product => (//вывод карточки
                    <ProductCard
                        key={product.id}
                        product={product}
                        onAddToCart={onAddToCart}
                    />
                ))}
            </div>
        </div>
    );
}