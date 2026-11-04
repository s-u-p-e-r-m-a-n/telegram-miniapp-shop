import type { AdminCredentials } from './adminOrdersApi';
import type { Category } from '../types/Category';
import { getApiErrorMessage } from './apiErrorHandler';

export type CreateAdminCategoryRequest = {
    name: string;
    imageUrl?: string | null;
};

function encodeCredentials(credentials: AdminCredentials): string {
    const bytes = new TextEncoder().encode(
        `${credentials.username}:${credentials.password}`
    );

    let binaryValue = '';

    bytes.forEach(byte => {
        binaryValue += String.fromCharCode(byte);
    });

    return btoa(binaryValue);
}

export async function createAdminCategory(
    credentials: AdminCredentials,
    request: CreateAdminCategoryRequest
): Promise<Category> {
    const response = await fetch('/api/admin/categories', {
        method: 'POST',
        headers: {
            Authorization: `Basic ${encodeCredentials(credentials)}`,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(request),
    });

    if (!response.ok) {
        if (response.status === 401) {
            throw new Error('Неверный логин или пароль администратора');
        }

        if (response.status === 403) {
            throw new Error('У администратора нет доступа к этой операции');
        }

        throw new Error(await getApiErrorMessage(response));
    }

    return response.json() as Promise<Category>;
}


export async function deleteAdminCategory(
    credentials: AdminCredentials,
    categoryId: number
): Promise<void> {
    const response = await fetch(
        `/api/admin/categories/${categoryId}`,
        {
            method: 'DELETE',
            headers: {
                Authorization: `Basic ${btoa(
                    `${credentials.username}:${credentials.password}`
                )}`,
            },
        }
    );

    if (!response.ok) {
        let message = 'Не удалось удалить категорию';

        try {
            const errorBody = await response.json() as {
                message?: string;
            };

            if (errorBody.message) {
                message = errorBody.message;
            }
        } catch {
            // Сервер не прислал JSON с описанием ошибки.
        }

        throw new Error(message);
    }
}