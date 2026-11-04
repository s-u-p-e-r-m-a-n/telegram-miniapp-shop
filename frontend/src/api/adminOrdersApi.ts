export type AdminOrderStatus =
    | 'AWAITING_PAYMENT'
    | 'NEW'
    | 'IN_WORK'
    | 'DONE'
    | 'CANCELLED';

export type AdminOrderSummary = {
    id: number;
    createdAt: string;
    customerName: string;
    customerPhone: string;
    totalAmount: number;
    status: AdminOrderStatus;
};

export type AdminOrdersView = 'ACTIVE' | 'COMPLETED' | 'ALL';

export type AdminCredentials = {
    username: string;
    password: string;
};

export async function getAdminOrders(
    credentials: AdminCredentials,
    view: AdminOrdersView = 'ACTIVE'
): Promise<AdminOrderSummary[]> {
    const authorization = btoa(
        `${credentials.username}:${credentials.password}`
    );

    const response = await fetch(
        `/api/admin/orders?view=${view}`,
        {
            headers: {
                Authorization: `Basic ${authorization}`
            }
        }
    );

    if (response.status === 401) {
        throw new Error('Неверный логин или пароль администратора');
    }

    if (response.status === 403) {
        throw new Error('У пользователя нет прав администратора');
    }

    if (!response.ok) {
        throw new Error('Не удалось загрузить заказы');
    }

    return response.json();
}


export type AdminOrderItem = {
    productId: number;
    productName: string;
    productPrice: number;
    quantity: number;
    totalPrice: number;
}

export type AdminOrderDetails = {
    id: number;
    source: string;
    customerName: string;
    customerPhone: string;
    customerComment: string;
    status: AdminOrderStatus;
    totalAmount: number;
    createdAt: string;
    updatedAt: string;
    items: AdminOrderItem[];
}

export async function getAdminOrderDetails(id: number, credentials: AdminCredentials
): Promise<AdminOrderDetails> {
    const response = await fetch(`/api/admin/orders/${id}`, {
        headers: {
            Authorization: `Basic ${btoa(
                `${credentials.username}:${credentials.password}`
            )}`,
        },
    });

    if (!response.ok) {
        throw new Error(`Не удалось загрузить заказ: ${response.status}`);
    }

    return response.json();
}

export async function updateAdminOrderStatus(
    id: number,
    status: AdminOrderStatus,
    credentials: AdminCredentials
): Promise<void> {
    const response = await fetch(`/api/admin/orders/${id}/status`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Basic ${btoa(
                `${credentials.username}:${credentials.password}`
            )}`,
        },
        body: JSON.stringify({status}),
    });

    if (!response.ok) {
        throw new Error(`Не удалось изменить статус: ${response.status}`);
    }
}

export async function sendAdminOrderMessage(
    orderId: number,
    message: string,
    credentials: AdminCredentials
): Promise<void> {
    const response = await fetch(
        `/api/admin/orders/${orderId}/message`,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Basic ${btoa(
                    `${credentials.username}:${credentials.password}`
                )}`,
            },
            body: JSON.stringify({ message }),
        }
    );

    if (!response.ok) {
        throw new Error(
            `Не удалось отправить сообщение: ${response.status}`
        );
    }
}