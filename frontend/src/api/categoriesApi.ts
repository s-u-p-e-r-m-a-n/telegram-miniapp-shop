import type { Category } from '../types/Category';

export async function getCategories(): Promise<Category[]> {

    const response = await fetch('/api/categories');

    if (!response.ok) {
        throw new Error('Не удалось загрузить категории');
    }

    return response.json();
}