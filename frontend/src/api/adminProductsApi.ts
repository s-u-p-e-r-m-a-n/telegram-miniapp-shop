import type { AdminCredentials } from './adminOrdersApi';
import type {
    AdminProduct,
    AdminProductRequest
} from '../types/AdminProduct';
import { getApiErrorMessage } from './apiErrorHandler';

const ADMIN_PRODUCTS_URL = '/api/admin/products';

function encodeCredentials(credentials: AdminCredentials): string {
    const bytes = new TextEncoder().encode(
        `${credentials.username}:${credentials.password}`
    );

    let binaryValue = '';

    bytes.forEach((byte) => {
        binaryValue += String.fromCharCode(byte);
    });

    return btoa(binaryValue);
}

async function adminRequest<T>(
    path: string,
    credentials: AdminCredentials,
    init: RequestInit = {}
): Promise<T> {
    const headers = new Headers(init.headers);

    headers.set(
        'Authorization',
        `Basic ${encodeCredentials(credentials)}`
    );

    if (init.body !== undefined) {
        headers.set('Content-Type', 'application/json');
    }

    const response = await fetch(path, {
        ...init,
        headers
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

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json() as Promise<T>;
}

export function getAdminProducts(
    credentials: AdminCredentials
): Promise<AdminProduct[]> {
    return adminRequest<AdminProduct[]>(
        ADMIN_PRODUCTS_URL,
        credentials
    );
}

export function getAdminProductById(
    credentials: AdminCredentials,
    productId: number
): Promise<AdminProduct> {
    return adminRequest<AdminProduct>(
        `${ADMIN_PRODUCTS_URL}/${productId}`,
        credentials
    );
}

export function createAdminProduct(
    credentials: AdminCredentials,
    request: AdminProductRequest
): Promise<AdminProduct> {
    return adminRequest<AdminProduct>(
        ADMIN_PRODUCTS_URL,
        credentials,
        {
            method: 'POST',
            body: JSON.stringify(request)
        }
    );
}

export function updateAdminProduct(
    credentials: AdminCredentials,
    productId: number,
    request: AdminProductRequest
): Promise<AdminProduct> {
    return adminRequest<AdminProduct>(
        `${ADMIN_PRODUCTS_URL}/${productId}`,
        credentials,
        {
            method: 'PUT',
            body: JSON.stringify(request)
        }
    );
}

export function deleteAdminProduct(
    credentials: AdminCredentials,
    productId: number
): Promise<void> {
    return adminRequest<void>(
        `${ADMIN_PRODUCTS_URL}/${productId}`,
        credentials,
        { method: 'DELETE' }
    );
}