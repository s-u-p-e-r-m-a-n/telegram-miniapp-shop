import type { Product } from '../types/Product';

export async function getProducts(): Promise<Product[]> {

    const response = await fetch('/api/products');

    if (!response.ok) {
        throw new Error('Не удалось загрузить товары');
    }

    return response.json();
}

export async function getProductById(
    productId: number
): Promise<Product> {

    const response = await fetch(
        `/api/products/${productId}`
    );

    if (!response.ok) {
        throw new Error('Не удалось загрузить товар');
    }

    return response.json();
}