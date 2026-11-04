import {type FormEvent, Fragment, useEffect, useState} from 'react';
import {createAdminCategory, deleteAdminCategory} from '../api/adminCategoriesApi';
import {
    type AdminCredentials,
    type AdminOrderDetails,
    type AdminOrderStatus,
    type AdminOrderSummary,
    type AdminOrdersView,
    getAdminOrderDetails,
    getAdminOrders,
    sendAdminOrderMessage,
    updateAdminOrderStatus,
} from '../api/adminOrdersApi';
import './AdminPage.css';
import {
    createAdminProduct,
    deleteAdminProduct,
    getAdminProducts,
    updateAdminProduct,
    uploadAdminProductImage
} from '../api/adminProductsApi';
import type {AdminProduct, AdminProductRequest} from '../types/AdminProduct';

import {getCategories} from '../api/categoriesApi';
import type {Category} from '../types/Category';

type AdminSection = 'orders' | 'products' | 'product-create' | 'categories';

function createEmptyAdminProductRequest(
    categoryId: number
): AdminProductRequest {
    return {
        categoryId,
        name: '',
        description: '',
        price: 0,
        images: [],
        active: true,
        featured: false,
        sortOrder: 0,
        stockQuantity: 0,
        trackStock: true,
    };
}

function getStatusText(status: AdminOrderStatus): string {
    switch (status) {
        case 'AWAITING_PAYMENT':
            return 'Ожидает оплаты';
        case 'NEW':
            return 'Новый';
        case 'IN_WORK':
            return 'Принят в работу';
        case 'DONE':
            return 'Выполнен';
        case 'CANCELLED':
            return 'Отменён';
    }
}

function sortAdminOrders(orders: AdminOrderSummary[]): AdminOrderSummary[] {
    const priority: Record<AdminOrderStatus, number> = {
        NEW: 0,
        IN_WORK: 0,
        AWAITING_PAYMENT: 1,
        DONE: 2,
        CANCELLED: 3,
    };

    return [...orders].sort((first, second) => {
        const statusDifference = priority[first.status] - priority[second.status];

        if (statusDifference !== 0) {
            return statusDifference;
        }

        return new Date(second.createdAt).getTime()
            - new Date(first.createdAt).getTime();
    });
}

function getProductCategoryName(product: AdminProduct): string {
    return product.categoryName?.trim() || 'Без категории';
}

function groupAdminProducts(
    products: AdminProduct[]
): { categoryName: string; products: AdminProduct[] }[] {
    const groups = new Map<string, AdminProduct[]>();

    products.forEach(product => {
        const categoryName = getProductCategoryName(product);
        const categoryProducts = groups.get(categoryName);

        if (categoryProducts) {
            categoryProducts.push(product);
        } else {
            groups.set(categoryName, [product]);
        }
    });

    return Array.from(groups, ([categoryName, categoryProducts]) => ({
        categoryName,
        products: categoryProducts,
    }));
}

function formatOrderDate(value: string): string {
    return new Date(value).toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });
}

function getAvailableStatusActions(
    status: AdminOrderStatus
): { status: AdminOrderStatus; label: string }[] {
    if (status === 'NEW') {
        return [
            {status: 'IN_WORK', label: 'Взять в работу'},
            {status: 'CANCELLED', label: 'Отменить заказ'},
        ];
    }

    if (status === 'IN_WORK') {
        return [
            {status: 'DONE', label: 'Завершить заказ'},
            {status: 'CANCELLED', label: 'Отменить заказ'},
        ];
    }

    return [];
}

export function AdminPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [credentials, setCredentials] = useState<AdminCredentials | null>(null);
    const [orders, setOrders] = useState<AdminOrderSummary[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [ordersView, setOrdersView] = useState<AdminOrdersView>('ACTIVE');
    const [ordersError, setOrdersError] = useState('');
    const [loginError, setLoginError] = useState('');
    const [activeSection, setActiveSection] = useState<AdminSection>('orders');
    const [isProductsMenuOpen, setIsProductsMenuOpen] = useState(false);
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    const [selectedOrder, setSelectedOrder] = useState<AdminOrderDetails | null>(null);
    const [selectedOrderSummary, setSelectedOrderSummary] =
        useState<AdminOrderSummary | null>(null);
    const [detailsLoadingId, setDetailsLoadingId] = useState<number | null>(null);
    const [detailsError, setDetailsError] = useState('');
    const [statusUpdatingId, setStatusUpdatingId] = useState<number | null>(null);
    const [statusError, setStatusError] = useState('');

    const [messageText, setMessageText] = useState('');
    const [messageSending, setMessageSending] = useState(false);
    const [messageError, setMessageError] = useState('');
    const [messageSuccess, setMessageSuccess] = useState('');
    const [categoriesLoading, setCategoriesLoading] = useState(false);
    const [categories, setCategories] = useState<Category[]>([]);
    const [categoriesError, setCategoriesError] = useState('');
    const [categoryModalOpen, setCategoryModalOpen] = useState(false);
    const [newCategoryName, setNewCategoryName] = useState('');
    const [newCategoryImageUrl, setNewCategoryImageUrl] = useState('');
    const [categoryCreating, setCategoryCreating] = useState(false);
    const [categoryCreateError, setCategoryCreateError] = useState('');
    const [categoryDeletingId, setCategoryDeletingId] = useState<number | null>(null);
    const [categoryDeleteError, setCategoryDeleteError] = useState('');
    const [products, setProducts] = useState<AdminProduct[]>([]);
    const [productsLoading, setProductsLoading] = useState(false);
    const [productsError, setProductsError] = useState('');
    const [productImageFile, setProductImageFile] = useState<File | null>(null);

    const [editingProduct, setEditingProduct] =
        useState<AdminProduct | null>(null);
    const [productForm, setProductForm] =
        useState<AdminProductRequest | null>(null);
    const [productSaving, setProductSaving] = useState(false);
    const [productSaveError, setProductSaveError] = useState('');
    const [productDeletingId, setProductDeletingId] = useState<number | null>(null);
    const [productDeleteError, setProductDeleteError] = useState('');

    const ALL_PRODUCT_CATEGORIES = '__ALL__';
    const [collapsedProductCategories, setCollapsedProductCategories] =
        useState<Set<string>>(() => new Set());

    const toggleProductCategory = (categoryName: string) => {
        setCollapsedProductCategories(current => {
            const updated = new Set(current);

            if (updated.has(categoryName)) {
                updated.delete(categoryName);
            } else {
                updated.add(categoryName);
            }

            return updated;
        });
    };


    const [productCategoryFilter, setProductCategoryFilter] =
        useState<string>(ALL_PRODUCT_CATEGORIES);

    const productCategories = Array.from(
        new Set(
            products.map(product =>
                product.categoryName?.trim() || 'Без категории'
            )
        )
    ).sort((first, second) => first.localeCompare(second, 'ru'));

    const filteredProducts = products
        .filter(product => {
            const categoryName =
                product.categoryName?.trim() || 'Без категории';

            return productCategoryFilter === ALL_PRODUCT_CATEGORIES
                || categoryName === productCategoryFilter;
        })
        .sort((first, second) => {
            const firstCategory =
                first.categoryName?.trim() || 'Без категории';
            const secondCategory =
                second.categoryName?.trim() || 'Без категории';

            const categoryDifference =
                firstCategory.localeCompare(secondCategory, 'ru');

            if (categoryDifference !== 0) {
                return categoryDifference;
            }

            return first.name.localeCompare(second.name, 'ru');
        });
    const groupedProducts = groupAdminProducts(filteredProducts);


    const handleLogin = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setIsLoading(true);
        setLoginError('');

        const enteredCredentials = {username, password};

        try {
            const loadedOrders = await getAdminOrders(enteredCredentials);

            setCredentials(enteredCredentials);
            setOrders(sortAdminOrders(loadedOrders));
            setOrdersView('ACTIVE');
            setActiveSection('orders');
            setPassword('');
        } catch (error) {
            setLoginError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось войти в админку'
            );
        } finally {
            setIsLoading(false);
        }
    };

    const handleLogout = () => {
        setCredentials(null);
        setOrders([]);
        setUsername('');
        setPassword('');
        setOrdersView('ACTIVE');
        setActiveSection('orders');
        setIsSidebarOpen(false);
        setSelectedOrder(null);
        setSelectedOrderSummary(null);
        setLoginError('');
        setOrdersError('');
        setDetailsError('');
        setStatusError('');
        setMessageText('');
        setMessageError('');
        setMessageSuccess('');
        setProducts([]);
        setProductsError('');
        setIsProductsMenuOpen(false);
    };

    const handleShowDetails = async (orderId: number) => {
        if (!credentials) return;

        const orderSummary = orders.find(order => order.id === orderId) ?? null;

        setSelectedOrder(null);
        setSelectedOrderSummary(orderSummary);
        setDetailsLoadingId(orderId);
        setDetailsError('');
        setStatusError('');
        setMessageText('');
        setMessageError('');
        setMessageSuccess('');

        try {
            const details = await getAdminOrderDetails(orderId, credentials);
            setSelectedOrder(details);
        } catch (error) {
            setDetailsError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось загрузить заказ'
            );
        } finally {
            setDetailsLoadingId(null);
        }
    };

    const handleCloseDetails = () => {
        setSelectedOrder(null);
        setSelectedOrderSummary(null);
        setDetailsError('');
        setStatusError('');
        setMessageText('');
        setMessageError('');
        setMessageSuccess('');
    };

    const handleChangeOrderStatus = async (
        orderId: number,
        newStatus: AdminOrderStatus
    ) => {
        if (!credentials) return;

        setStatusUpdatingId(orderId);
        setStatusError('');

        try {
            await updateAdminOrderStatus(orderId, newStatus, credentials);

            const updatedOrders = await getAdminOrders(credentials, ordersView);
            setOrders(sortAdminOrders(updatedOrders));

            setSelectedOrder(current =>
                current?.id === orderId
                    ? {...current, status: newStatus}
                    : current
            );
            setSelectedOrderSummary(current =>
                current?.id === orderId
                    ? {...current, status: newStatus}
                    : current
            );
        } catch (error) {
            setStatusError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось изменить статус заказа'
            );
        } finally {
            setStatusUpdatingId(null);
        }
    };

    const handleSendMessage = async () => {
        if (!credentials || !selectedOrder) return;

        const trimmedMessage = messageText.trim();

        if (!trimmedMessage) {
            setMessageError('Введите текст сообщения');
            return;
        }

        setMessageSending(true);
        setMessageError('');
        setMessageSuccess('');

        try {
            await sendAdminOrderMessage(
                selectedOrder.id,
                trimmedMessage,
                credentials
            );

            setMessageText('');
            setMessageSuccess('Сообщение отправлено клиенту');
        } catch (error) {
            setMessageError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось отправить сообщение'
            );
        } finally {
            setMessageSending(false);
        }
    };

    const handleChangeView = async (view: AdminOrdersView) => {
        if (!credentials || view === ordersView || isLoading) return;

        setIsLoading(true);
        setOrdersError('');

        try {
            const loadedOrders = await getAdminOrders(credentials, view);
            setOrders(sortAdminOrders(loadedOrders));
            setOrdersView(view);
            handleCloseDetails();
        } catch (error) {
            setOrdersError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось загрузить заказы'
            );
        } finally {
            setIsLoading(false);
        }
    };

    const isDetailsOpen =
        selectedOrder !== null
        || detailsLoadingId !== null
        || detailsError.length > 0;

    const drawerOrderId = selectedOrder?.id ?? detailsLoadingId;
    const drawerStatus =
        selectedOrderSummary?.status ?? selectedOrder?.status;

    const handleShowProducts = async () => {
        if (!credentials) return;

        setActiveSection('products');
        setIsSidebarOpen(false);
        setProductsLoading(true);
        setProductsError('');

        try {
            const loadedProducts = await getAdminProducts(credentials);
            setProducts(loadedProducts);

            try {
                const loadedCategories = await getCategories();
                setCategories(loadedCategories);
                setCategoriesError('');
            } catch (error) {
                setCategoriesError(
                    error instanceof Error
                        ? error.message
                        : 'Не удалось загрузить категории'
                );
            }
        } catch (error) {
            setProductsError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось загрузить товары'
            );
        } finally {
            setProductsLoading(false);
        }
    };

    const handleStartEditProduct = (product: AdminProduct) => {
        setEditingProduct(product);
        setProductForm({
            categoryId: product.categoryId,
            name: product.name,
            description: product.description,
            price: product.price,
            images: product.images,
            active: product.active,
            featured: product.featured,
            sortOrder: product.sortOrder,
            stockQuantity: product.stockQuantity,
            trackStock: product.trackStock,
        });
        setProductSaveError('');
        setProductImageFile(null);
    };

    const handleCloseProductForm = () => {
        if (productSaving) return;

        setEditingProduct(null);
        setProductForm(null);
        setProductSaveError('');
        setProductImageFile(null);
    };

    const handleUpdateProduct = async (
        event: FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();

        if (!credentials || !editingProduct || !productForm) return;

        setProductSaving(true);
        setProductSaveError('');

        try {
            let request: AdminProductRequest = productForm;

            if (productImageFile) {
                const {imageUrl} = await uploadAdminProductImage(
                    credentials,
                    productImageFile
                );

                const images: string[] = [imageUrl, ...productForm.images.slice(1)];

                request = { ...productForm, images };

                setProductForm(current => current ? { ...current, images } : current);
                setProductImageFile(null);
            }

            const updatedProduct = await updateAdminProduct(
                credentials,
                editingProduct.id,
                request
            );

            setProducts(currentProducts =>
                currentProducts.map(product =>
                    product.id === updatedProduct.id
                        ? updatedProduct
                        : product
                )
            );

            setEditingProduct(null);
            setProductForm(null);
        } catch (error) {
            setProductSaveError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось сохранить товар'
            );
        } finally {
            setProductSaving(false);
        }
    };

    const handleDeleteProduct = async (product: AdminProduct) => {
        if (!credentials || productDeletingId !== null) return;

        const confirmed = window.confirm(
            `Удалить товар «${product.name}»?`
        );

        if (!confirmed) return;

        setProductDeletingId(product.id);
        setProductDeleteError('');

        try {
            await deleteAdminProduct(credentials, product.id);

            setProducts(currentProducts =>
                currentProducts.filter(
                    currentProduct => currentProduct.id !== product.id
                )
            );
        } catch (error) {
            setProductDeleteError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось удалить товар'
            );
        } finally {
            setProductDeletingId(null);
        }
    };

    const productCategoriesForForm =
        editingProduct !== null
        && !categories.some(
            category => category.id === editingProduct.categoryId
        )
            ? [
                ...categories,
                {
                    id: editingProduct.categoryId,
                    name: editingProduct.categoryName,
                    imageUrl: '',
                },
            ]
            : categories;

    const handleStartCreateProduct = async () => {
        if (!credentials) return;

        setActiveSection('product-create');
        setIsProductsMenuOpen(true);
        setIsSidebarOpen(false);
        setProductSaveError('');
        setCategoriesError('');

        const initialCategoryId = categories[0]?.id ?? 0;
        setProductForm(createEmptyAdminProductRequest(initialCategoryId));

        if (categories.length > 0) return;

        setCategoriesLoading(true);

        try {
            const loadedCategories = await getCategories();

            setCategories(loadedCategories);

            setProductForm(currentForm =>
                currentForm
                    ? {
                        ...currentForm,
                        categoryId:
                            currentForm.categoryId || loadedCategories[0]?.id || 0,
                    }
                    : currentForm
            );
        } catch (error) {
            setCategoriesError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось загрузить категории'
            );
        } finally {
            setCategoriesLoading(false);
        }
    };

    const handleCreateProduct = async (
        event: FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();

        if (!credentials || !productForm) return;

        if (productForm.categoryId <= 0) {
            setProductSaveError('Выберите категорию товара');
            return;
        }

        const imageUrls = productForm.images
            .map(image => image.trim())
            .filter(Boolean);

        if (imageUrls.length === 0 && !productImageFile) {
            setProductSaveError('Выберите фото или укажите ссылку на изображение');
            return;
        }

        setProductSaving(true);
        setProductSaveError('');

        try {
            if (productImageFile) {
                const {imageUrl} = await uploadAdminProductImage(
                    credentials,
                    productImageFile
                );

                imageUrls.push(imageUrl);
                setProductImageFile(null);
                setProductForm(current =>
                    current
                        ? {...current, images: [...current.images, imageUrl]}
                        : current
                );
            }

            const request: AdminProductRequest = {
                ...productForm,
                images: imageUrls,
            };

            await createAdminProduct(credentials, request);
            setProductForm(null);
            setActiveSection('products');

            await handleShowProducts();
        } catch (error) {
            setProductSaveError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось создать товар'
            );
        } finally {
            setProductSaving(false);
        }
    };

    const handleOpenCategoryModal = () => {
        setNewCategoryName('');
        setNewCategoryImageUrl('');
        setCategoryCreateError('');
        setCategoryModalOpen(true);
    };

    const handleCloseCategoryModal = () => {
        if (categoryCreating) return;

        setCategoryModalOpen(false);
        setCategoryCreateError('');
    };

    const handleCreateCategory = async (
        event: FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();

        if (!credentials || !productForm) return;

        setCategoryCreating(true);
        setCategoryCreateError('');

        try {
            const createdCategory = await createAdminCategory(credentials, {
                name: newCategoryName.trim(),
                imageUrl: newCategoryImageUrl.trim() || null,
            });

            setCategories(currentCategories => [
                ...currentCategories,
                createdCategory,
            ]);

            setProductForm(currentForm =>
                currentForm
                    ? {
                        ...currentForm,
                        categoryId: createdCategory.id,
                    }
                    : currentForm
            );

            setCategoryModalOpen(false);
            setNewCategoryName('');
            setNewCategoryImageUrl('');
        } catch (error) {
            setCategoryCreateError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось создать категорию'
            );
        } finally {
            setCategoryCreating(false);
        }
    };


    const handleShowCategories = async () => {
        if (!credentials) return;

        setActiveSection('categories');
        setIsSidebarOpen(false);
        setCategoriesLoading(true);
        setCategoriesError('');
        setCategoryDeleteError('');

        try {
            const loadedCategories = await getCategories();
            setCategories(loadedCategories);
        } catch (error) {
            setCategoriesError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось загрузить категории'
            );
        } finally {
            setCategoriesLoading(false);
        }
    };

    const handleDeleteCategory = async (category: Category) => {
        if (!credentials || categoryDeletingId !== null) return;

        const confirmed = window.confirm(
            `Удалить категорию «${category.name}»?`
        );

        if (!confirmed) return;

        setCategoryDeletingId(category.id);
        setCategoryDeleteError('');

        try {
            await deleteAdminCategory(credentials, category.id);

            const remainingCategories = categories.filter(
                currentCategory => currentCategory.id !== category.id
            );

            setCategories(remainingCategories);

            setProductForm(currentForm =>
                currentForm?.categoryId === category.id
                    ? {
                        ...currentForm,
                        categoryId: remainingCategories[0]?.id ?? 0,
                    }
                    : currentForm
            );
        } catch (error) {
            setCategoryDeleteError(
                error instanceof Error
                    ? error.message
                    : 'Не удалось удалить категорию'
            );
        } finally {
            setCategoryDeletingId(null);
        }
    };

    const [productImagePreview, setProductImagePreview] = useState('');

    useEffect(() => {
        if (!productImageFile) {
            setProductImagePreview('');
            return;
        }

        const previewUrl = URL.createObjectURL(productImageFile);
        setProductImagePreview(previewUrl);

        return () => URL.revokeObjectURL(previewUrl);
    }, [productImageFile]);

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    return (
        <main
            className={
                credentials
                    ? 'admin-page admin-page--application'
                    : 'admin-page admin-page--login'
            }
        >
            {!credentials ? (
                <section className="admin-login-card">
                    <div className="admin-login-brand">
                        <span className="admin-brand-mark" aria-hidden="true">М</span>
                        <span>Панель магазина</span>
                    </div>

                    <h1>Вход в админку</h1>
                    <p className="admin-login-description">
                        Введите данные администратора для продолжения
                    </p>

                    <form className="admin-login-form" onSubmit={handleLogin}>
                        <label className="admin-login-field">
                            <span>Логин</span>
                            <input
                                value={username}
                                onChange={event => setUsername(event.target.value)}
                                autoComplete="username"
                                required
                            />
                        </label>

                        <label className="admin-login-field">
                            <span>Пароль</span>
                            <input
                                type="password"
                                value={password}
                                onChange={event => setPassword(event.target.value)}
                                autoComplete="current-password"
                                required
                            />
                        </label>

                        {loginError && (
                            <p className="admin-alert" role="alert">
                                {loginError}
                            </p>
                        )}

                        <button
                            className="admin-primary-button"
                            type="submit"
                            disabled={isLoading}
                        >
                            {isLoading ? 'Проверяем...' : 'Войти'}
                        </button>
                    </form>
                </section>
            ) : (
                <div className="admin-shell">
                    {isSidebarOpen && (
                        <button
                            className="admin-sidebar-backdrop"
                            type="button"
                            aria-label="Закрыть меню"
                            onClick={() => setIsSidebarOpen(false)}
                        />
                    )}

                    <aside
                        className={
                            isSidebarOpen
                                ? 'admin-sidebar admin-sidebar--open'
                                : 'admin-sidebar'
                        }
                    >
                        <div className="admin-sidebar-brand">
                            <span className="admin-brand-mark" aria-hidden="true">М</span>
                            <span>ПАНЕЛЬ МАГАЗИНА</span>
                        </div>

                        <p className="admin-sidebar-caption">УПРАВЛЕНИЕ</p>

                        <nav className="admin-navigation" aria-label="Разделы админки">
                            <button
                                className={
                                    activeSection === 'orders'
                                        ? 'admin-navigation-link admin-navigation-link--active'
                                        : 'admin-navigation-link'
                                }
                                type="button"
                                aria-current={activeSection === 'orders' ? 'page' : undefined}
                                onClick={() => {
                                    setActiveSection('orders');
                                    setIsSidebarOpen(false);
                                }}
                            >
                                <svg viewBox="0 0 24 24" aria-hidden="true">
                                    <path d="M7 3.75h8l4 4v12.5H5V3.75h2Z"/>
                                    <path d="M14.5 3.75v4.5H19M8.5 12h7M8.5 16h7"/>
                                </svg>
                                <span>Заказы</span>
                            </button>

                            <div className="admin-navigation-group">
                                <button
                                    className={
                                        activeSection === 'products'
                                        || activeSection === 'product-create'
                                        || activeSection === 'categories'
                                            ? 'admin-navigation-link admin-navigation-link--active'
                                            : 'admin-navigation-link'
                                    }
                                    type="button"
                                    aria-expanded={isProductsMenuOpen}
                                    onClick={() => setIsProductsMenuOpen(open => !open)}
                                >
                                    <svg viewBox="0 0 24 24" aria-hidden="true">
                                        <path d="m12 3 8 4.5v9L12 21l-8-4.5v-9L12 3Z"/>
                                        <path d="m4.5 7.75 7.5 4.5 7.5-4.5M12 12.25V21"/>
                                    </svg>

                                    <span>Товары</span>

                                    <span
                                        className={
                                            isProductsMenuOpen
                                                ? 'admin-navigation-chevron admin-navigation-chevron--open'
                                                : 'admin-navigation-chevron'
                                        }
                                        aria-hidden="true"
                                    >
            ⌄
        </span>
                                </button>

                                {isProductsMenuOpen && (
                                    <div className="admin-subnavigation">
                                        <button
                                            className={
                                                activeSection === 'products'
                                                    ? 'admin-subnavigation-link admin-subnavigation-link--active'
                                                    : 'admin-subnavigation-link'
                                            }
                                            type="button"
                                            aria-current={activeSection === 'products' ? 'page' : undefined}
                                            onClick={() => void handleShowProducts()}
                                        >
                                            Список товаров
                                        </button>

                                        <button
                                            className={
                                                activeSection === 'categories'
                                                    ? 'admin-subnavigation-link admin-subnavigation-link--active'
                                                    : 'admin-subnavigation-link'
                                            }
                                            type="button"
                                            aria-current={activeSection === 'categories' ? 'page' : undefined}
                                            onClick={() => void handleShowCategories()}
                                        >
                                            Категории
                                        </button>

                                        <button
                                            className={
                                                activeSection === 'product-create'
                                                    ? 'admin-subnavigation-link admin-subnavigation-link--active'
                                                    : 'admin-subnavigation-link'
                                            }
                                            type="button"
                                            aria-current={activeSection === 'product-create' ? 'page' : undefined}
                                            onClick={() => void handleStartCreateProduct()}
                                        >
                                            Создать товар
                                        </button>
                                    </div>
                                )}
                            </div>
                        </nav>

                        <div className="admin-sidebar-footer">
                            <button
                                className="admin-logout-button"
                                type="button"
                                onClick={handleLogout}
                            >
                                <svg viewBox="0 0 24 24" aria-hidden="true">
                                    <path d="M10 4H5v16h5M14 8l4 4-4 4M8 12h10"/>
                                </svg>
                                <span>Выйти</span>
                            </button>
                        </div>
                    </aside>

                    <div className="admin-main">
                        <header className="admin-topbar">
                            <button
                                className="admin-menu-toggle"
                                type="button"
                                aria-label="Открыть меню"
                                aria-expanded={isSidebarOpen}
                                onClick={() => setIsSidebarOpen(true)}
                            >
                                <span/>
                                <span/>
                                <span/>
                            </button>
                            <div>
                                <span className="admin-topbar-title">Панель управления</span>
                                <span className="admin-topbar-subtitle">Магазин</span>
                            </div>
                        </header>

                        <div className="admin-content">
                            {activeSection === 'product-create' ? (
                                <>
                                    <div className="admin-page-heading">
                                        <div>
                                            <p className="admin-eyebrow">УПРАВЛЕНИЕ КАТАЛОГОМ</p>
                                            <h1>Создать товар</h1>
                                        </div>
                                    </div>

                                    <section className="admin-create-product-area">
                                        <section className="admin-table-card admin-create-product-card">
                                            <div className="admin-table-heading">
                                                <h2>Данные товара</h2>
                                                <span>Заполните поля и сохраните товар</span>
                                            </div>

                                            {productForm ? (
                                                <form
                                                    className="admin-create-product-form"
                                                    onSubmit={handleCreateProduct}
                                                >
                                                    <div className="admin-product-form-grid">
                                                        <label className="admin-product-field">
                                                            Название
                                                            <input
                                                                required
                                                                value={productForm.name}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        name: event.target.value,
                                                                    })
                                                                }
                                                            />
                                                        </label>

                                                        <div className="admin-product-field admin-category-control">
                                                            <label htmlFor="new-product-category">
                                                                Категория
                                                            </label>

                                                            <div className="admin-category-control__row">
                                                                <select
                                                                    id="new-product-category"
                                                                    required
                                                                    value={productForm.categoryId || ''}
                                                                    disabled={categoriesLoading || categories.length === 0}
                                                                    onChange={event =>
                                                                        setProductForm({
                                                                            ...productForm,
                                                                            categoryId: Number(event.target.value),
                                                                        })
                                                                    }
                                                                >
                                                                    <option value="" disabled>
                                                                        {categoriesLoading
                                                                            ? 'Загружаем категории...'
                                                                            : categories.length === 0
                                                                                ? 'Категории не найдены'
                                                                                : 'Выберите категорию'}
                                                                    </option>

                                                                    {categories.map(category => (
                                                                        <option key={category.id} value={category.id}>
                                                                            {category.name}
                                                                        </option>
                                                                    ))}
                                                                </select>

                                                                <button
                                                                    className="admin-category-create-button"
                                                                    type="button"
                                                                    onClick={handleOpenCategoryModal}
                                                                    disabled={categoriesLoading}
                                                                >
                                                                    + Создать категорию
                                                                </button>
                                                            </div>
                                                        </div>

                                                        <label
                                                            className="admin-product-field admin-product-field--wide">
                                                            Описание
                                                            <textarea
                                                                required
                                                                rows={4}
                                                                value={productForm.description}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        description: event.target.value,
                                                                    })
                                                                }
                                                            />
                                                        </label>

                                                        <label className="admin-product-field">
                                                            Цена
                                                            <input
                                                                required
                                                                type="number"
                                                                min="0"
                                                                step="0.01"
                                                                value={productForm.price}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        price: Number(event.target.value),
                                                                    })
                                                                }
                                                            />
                                                        </label>

                                                        <label className="admin-product-field">
                                                            Порядок сортировки
                                                            <input
                                                                required
                                                                type="number"
                                                                step="1"
                                                                value={productForm.sortOrder}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        sortOrder: Number(event.target.value),
                                                                    })
                                                                }
                                                            />
                                                        </label>
                                                        <label className="admin-product-field">
                                                            Загрузить фото
                                                            <input
                                                                type="file"
                                                                accept="image/jpeg,image/png,image/webp"
                                                                disabled={productSaving}
                                                                onChange={event =>
                                                                    setProductImageFile(event.currentTarget.files?.[0] ?? null)
                                                                }
                                                            />
                                                        </label>
                                                        <label
                                                            className="admin-product-field admin-product-field--wide">
                                                            Ссылки на изображения — по одной на строку
                                                            <textarea
                                                                rows={3}
                                                                placeholder="https://example.com/product.jpg"
                                                                value={productForm.images.join('\n')}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        images: event.target.value
                                                                            .split('\n')
                                                                            .map(image => image.trim())
                                                                            .filter(Boolean),
                                                                    })
                                                                }
                                                            />
                                                        </label>

                                                        <label className="admin-product-field">
                                                            Остаток
                                                            <input
                                                                required
                                                                type="number"
                                                                min="0"
                                                                step="1"
                                                                value={productForm.stockQuantity}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        stockQuantity: Number(event.target.value),
                                                                    })
                                                                }
                                                            />
                                                        </label>
                                                    </div>

                                                    {categoriesError && (
                                                        <p className="admin-alert" role="alert">
                                                            {categoriesError}
                                                        </p>
                                                    )}

                                                    <div className="admin-product-checkboxes">
                                                        <label>
                                                            <input
                                                                type="checkbox"
                                                                checked={productForm.trackStock}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        trackStock: event.target.checked,
                                                                    })
                                                                }
                                                            />
                                                            Учитывать остаток
                                                        </label>

                                                        <label>
                                                            <input
                                                                type="checkbox"
                                                                checked={productForm.active}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        active: event.target.checked,
                                                                    })
                                                                }
                                                            />
                                                            Товар активен
                                                        </label>

                                                        <label>
                                                            <input
                                                                type="checkbox"
                                                                checked={productForm.featured}
                                                                onChange={event =>
                                                                    setProductForm({
                                                                        ...productForm,
                                                                        featured: event.target.checked,
                                                                    })
                                                                }
                                                            />
                                                            Популярный товар
                                                        </label>
                                                    </div>

                                                    {productSaveError && (
                                                        <p className="admin-alert" role="alert">
                                                            {productSaveError}
                                                        </p>
                                                    )}

                                                    <div className="admin-create-product-actions">
                                                        <button
                                                            className="admin-secondary-button"
                                                            type="button"
                                                            disabled={productSaving}
                                                            onClick={() => {
                                                                handleCloseProductForm();
                                                                void handleShowProducts();
                                                            }}
                                                        >
                                                            Отмена
                                                        </button>

                                                        <button
                                                            className="admin-primary-button"
                                                            type="submit"
                                                            disabled={
                                                                productSaving
                                                                || categoriesLoading
                                                                || categories.length === 0
                                                            }
                                                        >
                                                            {productSaving ? 'Создание...' : 'Создать товар'}
                                                        </button>
                                                    </div>
                                                </form>
                                            ) : (
                                                <div className="admin-empty-state">
                                                    <span className="admin-loader"/>
                                                    <p>Подготавливаем форму...</p>
                                                </div>
                                            )}
                                        </section>
                                    </section>

                                    {categoryModalOpen && (
                                        <div
                                            className="admin-category-modal-backdrop"
                                            onClick={event => {
                                                if (event.target === event.currentTarget) {
                                                    handleCloseCategoryModal();
                                                }
                                            }}
                                        >
                                            <section
                                                className="admin-category-modal"
                                                role="dialog"
                                                aria-modal="true"
                                                aria-labelledby="admin-category-modal-title"
                                            >
                                                <header className="admin-category-modal__header">
                                                    <h2 id="admin-category-modal-title">
                                                        Новая категория
                                                    </h2>

                                                    <button
                                                        className="admin-icon-button"
                                                        type="button"
                                                        aria-label="Закрыть окно"
                                                        onClick={handleCloseCategoryModal}
                                                        disabled={categoryCreating}
                                                    >
                                                        ×
                                                    </button>
                                                </header>

                                                <form
                                                    className="admin-category-modal__form"
                                                    onSubmit={handleCreateCategory}
                                                >
                                                    <label className="admin-category-modal__field">
                                                        <span>Название категории</span>
                                                        <input
                                                            required
                                                            autoFocus
                                                            value={newCategoryName}
                                                            onChange={event =>
                                                                setNewCategoryName(event.target.value)
                                                            }
                                                            placeholder="Например, Напитки"
                                                        />
                                                    </label>

                                                    <label className="admin-category-modal__field">
                                                        <span>Ссылка на изображение — необязательно</span>
                                                        <input
                                                            type="url"
                                                            value={newCategoryImageUrl}
                                                            onChange={event =>
                                                                setNewCategoryImageUrl(event.target.value)
                                                            }
                                                            placeholder="https://example.com/category.jpg"
                                                        />
                                                    </label>

                                                    {categoryCreateError && (
                                                        <p className="admin-alert" role="alert">
                                                            {categoryCreateError}
                                                        </p>
                                                    )}

                                                    <footer className="admin-category-modal__actions">
                                                        <button
                                                            className="admin-secondary-button"
                                                            type="button"
                                                            onClick={handleCloseCategoryModal}
                                                            disabled={categoryCreating}
                                                        >
                                                            Отмена
                                                        </button>

                                                        <button
                                                            className="admin-primary-button"
                                                            type="submit"
                                                            disabled={
                                                                categoryCreating
                                                                || !newCategoryName.trim()
                                                            }
                                                        >
                                                            {categoryCreating
                                                                ? 'Создание...'
                                                                : 'Создать категорию'}
                                                        </button>
                                                    </footer>
                                                </form>
                                            </section>
                                        </div>
                                    )}

                                </>
                            ) : activeSection === 'categories' ? (
                                <>
                                    <div className="admin-page-heading">
                                        <div>
                                            <p className="admin-eyebrow">УПРАВЛЕНИЕ КАТАЛОГОМ</p>
                                            <h1>Категории</h1>
                                            <p className="admin-page-description">
                                                Здесь можно удалить категорию, если в ней нет товаров
                                            </p>
                                        </div>
                                    </div>

                                    <section className="admin-products-placeholder">
                                        {categoriesError && (
                                            <p className="admin-alert" role="alert">
                                                {categoriesError}
                                            </p>
                                        )}

                                        {categoryDeleteError && (
                                            <p className="admin-alert" role="alert">
                                                {categoryDeleteError}
                                            </p>
                                        )}

                                        {categoriesLoading ? (
                                            <div className="admin-empty-state">
                                                <span className="admin-loader"/>
                                                <p>Загружаем категории...</p>
                                            </div>
                                        ) : categories.length === 0 ? (
                                            <div className="admin-empty-state">
                                                <h2>Категорий пока нет</h2>
                                                <p>Создать категорию можно в форме создания товара.</p>
                                            </div>
                                        ) : (
                                            <div className="admin-table-card">
                                                <div className="admin-table-heading">
                                                    <h2>Список категорий</h2>
                                                    <span>{categories.length} категорий</span>
                                                </div>

                                                <div className="admin-table-scroll">
                                                    <table className="admin-orders-table">
                                                        <thead>
                                                        <tr>
                                                            <th scope="col">Категория</th>
                                                            <th scope="col">Действие</th>
                                                        </tr>
                                                        </thead>

                                                        <tbody>
                                                        {categories.map(category => (
                                                            <tr key={category.id}>
                                                                <td data-label="Категория">
                                                                    <strong>{category.name}</strong>
                                                                </td>

                                                                <td data-label="Действие">
                                                                    <button
                                                                        className="admin-danger-button"
                                                                        type="button"
                                                                        disabled={categoryDeletingId !== null}
                                                                        onClick={() =>
                                                                            void handleDeleteCategory(category)
                                                                        }
                                                                    >
                                                                        {categoryDeletingId === category.id
                                                                            ? 'Удаляем...'
                                                                            : 'Удалить'}
                                                                    </button>
                                                                </td>
                                                            </tr>
                                                        ))}
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        )}
                                    </section>
                                </>
                            ) : activeSection === 'orders' ? (
                                <>
                                    <div className="admin-page-heading">
                                        <div>
                                            <p className="admin-eyebrow">УПРАВЛЕНИЕ ЗАКАЗАМИ</p>
                                            <h1>Заказы</h1>
                                            <p className="admin-page-description">
                                                Просматривайте и обрабатывайте заказы клиентов
                                            </p>
                                        </div>
                                        <div className="admin-order-count">
                                            <strong>{orders.length}</strong>
                                            <span>заказов</span>
                                        </div>
                                    </div>

                                    <div className="admin-orders-toolbar">
                                        <div
                                            className="admin-order-filters"
                                            role="tablist"
                                            aria-label="Фильтр заказов"
                                        >
                                            <button
                                                className={
                                                    ordersView === 'ACTIVE'
                                                        ? 'admin-filter-button admin-filter-button--active'
                                                        : 'admin-filter-button'
                                                }
                                                type="button"
                                                role="tab"
                                                aria-selected={ordersView === 'ACTIVE'}
                                                onClick={() => void handleChangeView('ACTIVE')}
                                                disabled={isLoading}
                                            >
                                                Активные
                                            </button>
                                            <button
                                                className={
                                                    ordersView === 'COMPLETED'
                                                        ? 'admin-filter-button admin-filter-button--active'
                                                        : 'admin-filter-button'
                                                }
                                                type="button"
                                                role="tab"
                                                aria-selected={ordersView === 'COMPLETED'}
                                                onClick={() => void handleChangeView('COMPLETED')}
                                                disabled={isLoading}
                                            >
                                                История
                                            </button>
                                            <button
                                                className={
                                                    ordersView === 'ALL'
                                                        ? 'admin-filter-button admin-filter-button--active'
                                                        : 'admin-filter-button'
                                                }
                                                type="button"
                                                role="tab"
                                                aria-selected={ordersView === 'ALL'}
                                                onClick={() => void handleChangeView('ALL')}
                                                disabled={isLoading}
                                            >
                                                Все заказы
                                            </button>
                                        </div>
                                    </div>

                                    {ordersError && (
                                        <p className="admin-alert" role="alert">
                                            {ordersError}
                                        </p>
                                    )}

                                    {isLoading ? (
                                        <div className="admin-empty-state">
                                            <span className="admin-loader"/>
                                            <p>Загружаем заказы...</p>
                                        </div>
                                    ) : orders.length === 0 ? (
                                        <div className="admin-empty-state">
                                            <h2>
                                                {ordersView === 'ACTIVE'
                                                    ? 'Активных заказов пока нет'
                                                    : ordersView === 'COMPLETED'
                                                        ? 'В истории пока нет заказов'
                                                        : 'Заказов пока нет'}
                                            </h2>
                                            <p>Когда появятся заказы, они отобразятся здесь.</p>
                                        </div>
                                    ) : (
                                        <div className="admin-table-card">
                                            <div className="admin-table-heading">
                                                <h2>Список заказов</h2>
                                                <span>{orders.length} записей</span>
                                            </div>

                                            <div className="admin-table-scroll">
                                                <table className="admin-orders-table">
                                                    <thead>
                                                    <tr>
                                                        <th scope="col">Заказ</th>
                                                        <th scope="col">Покупатель</th>
                                                        <th scope="col">Статус</th>
                                                        <th scope="col">Сумма</th>
                                                        <th scope="col">Действие</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    {orders.map(order => (
                                                        <tr
                                                            className={
                                                                selectedOrderSummary?.id === order.id
                                                                    ? 'admin-order-row admin-order-row--selected'
                                                                    : 'admin-order-row'
                                                            }
                                                            key={order.id}
                                                        >
                                                            <td data-label="Заказ">
                                                                <strong>№{order.id}</strong>
                                                                <span className="admin-order-date">
                                                                        {formatOrderDate(order.createdAt)}
                                                                    </span>
                                                            </td>
                                                            <td data-label="Покупатель">
                                                                <strong>{order.customerName}</strong>
                                                                <span className="admin-order-phone">
                                                                        {order.customerPhone}
                                                                    </span>
                                                            </td>
                                                            <td data-label="Статус">
                                                                    <span
                                                                        className={
                                                                            'admin-status admin-status--'
                                                                            + order.status.toLowerCase()
                                                                        }
                                                                    >
                                                                        {getStatusText(order.status)}
                                                                    </span>
                                                            </td>
                                                            <td
                                                                className="admin-order-amount"
                                                                data-label="Сумма"
                                                            >
                                                                {order.totalAmount} ₽
                                                            </td>
                                                            <td data-label="Действие">
                                                                <button
                                                                    className="admin-row-action"
                                                                    type="button"
                                                                    onClick={() => void handleShowDetails(order.id)}
                                                                    disabled={detailsLoadingId === order.id}
                                                                    aria-label={'Открыть заказ номер ' + order.id}
                                                                >
                                                                    {detailsLoadingId === order.id
                                                                        ? 'Загрузка...'
                                                                        : 'Подробнее'}
                                                                </button>
                                                            </td>
                                                        </tr>
                                                    ))}
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    )}
                                </>
                            ) : (
                                <>
                                    <div className="admin-page-heading">
                                        <div>
                                            <p className="admin-eyebrow">УПРАВЛЕНИЕ КАТАЛОГОМ</p>
                                            <h1>Товары</h1>
                                            <p className="admin-page-description">
                                                Раздел для управления товарами магазина
                                            </p>
                                        </div>
                                    </div>
                                    <section className="admin-products-placeholder">
                                        {productsError && (
                                            <p className="admin-alert" role="alert">
                                                {productsError}
                                            </p>
                                        )}
                                        {productDeleteError && (
                                            <p className="admin-alert" role="alert">
                                                {productDeleteError}
                                            </p>
                                        )}

                                        {productsLoading ? (
                                            <div className="admin-empty-state">
                                                <span className="admin-loader"/>
                                                <p>Загружаем товары...</p>
                                            </div>
                                        ) : products.length === 0 ? (
                                            <div className="admin-empty-state">
                                                <h2>Товаров пока нет</h2>
                                                <p>Добавленные товары появятся здесь.</p>
                                            </div>
                                        ) : (
                                            <div className="admin-table-card">
                                                <div className="admin-table-heading admin-products-toolbar">
                                                    <h2>Список товаров</h2>

                                                    <label className="admin-product-category-filter">
                                                        <span>Категория</span>

                                                        <select
                                                            value={productCategoryFilter}
                                                            onChange={event =>
                                                                setProductCategoryFilter(event.target.value)
                                                            }
                                                        >
                                                            <option value={ALL_PRODUCT_CATEGORIES}>
                                                                Все категории
                                                            </option>

                                                            {productCategories.map(categoryName => (
                                                                <option key={categoryName} value={categoryName}>
                                                                    {categoryName}
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </label>

                                                    <span>
        {filteredProducts.length} из {products.length} товаров
    </span>
                                                </div>

                                                <div className="admin-table-scroll">
                                                    <table className="admin-orders-table">
                                                        <thead>
                                                        <tr>
                                                            <th scope="col">Товар</th>
                                                            <th scope="col">Категория</th>
                                                            <th scope="col">Цена</th>
                                                            <th scope="col">Остаток</th>
                                                            <th scope="col">Статус</th>
                                                            <th scope="col">Популярное</th>
                                                            <th scope="col">Действия</th>
                                                        </tr>
                                                        </thead>

                                                        <tbody>
                                                        {groupedProducts.map(group => {
                                                            const isCollapsed =
                                                                collapsedProductCategories.has(group.categoryName);

                                                            return (
                                                                <Fragment key={group.categoryName}>
                                                                    <tr className="admin-product-category-row">
                                                                        <td colSpan={7}>
                                                                            <button
                                                                                className="admin-product-category-toggle"
                                                                                type="button"
                                                                                aria-expanded={!isCollapsed}
                                                                                onClick={() =>
                                                                                    toggleProductCategory(group.categoryName)
                                                                                }
                                                                            >
                                                                                <strong>{group.categoryName}</strong>

                                                                                <span
                                                                                    className="admin-product-category-count">
                            {group.products.length} шт.
                        </span>

                                                                                <span aria-hidden="true">
                            {isCollapsed ? '+' : '−'}
                        </span>
                                                                            </button>
                                                                        </td>
                                                                    </tr>

                                                                    {!isCollapsed && group.products.map(product => (
                                                                        <tr key={product.id}>
                                                                            <td data-label="Товар">
                                                                                <strong>{product.name}</strong>
                                                                                <span className="admin-order-date">
                                    ID: {product.id}
                                </span>
                                                                            </td>

                                                                            <td data-label="Категория">
                                                                                {product.categoryName}
                                                                            </td>

                                                                            <td data-label="Цена">
                                                                                {product.price.toLocaleString('ru-RU')} ₽
                                                                            </td>

                                                                            <td data-label="Остаток">
                                                                                {product.trackStock
                                                                                    ? `${product.stockQuantity} шт.`
                                                                                    : 'Не учитывается'}
                                                                            </td>

                                                                            <td data-label="Статус">
                                                                                {product.active ? 'Активен' : 'Скрыт'}
                                                                            </td>

                                                                            <td data-label="Популярное">
                                                                                {product.featured ? 'Да' : 'Нет'}
                                                                            </td>
                                                                            <td data-label="Действия">
                                                                                <button
                                                                                    className="admin-row-action"
                                                                                    type="button"
                                                                                    onClick={() => handleStartEditProduct(product)}
                                                                                >
                                                                                    Редактировать
                                                                                </button>
                                                                                <button
                                                                                    className="admin-row-action"
                                                                                    type="button"
                                                                                    disabled={productDeletingId !== null}
                                                                                    onClick={() => void handleDeleteProduct(product)}
                                                                                >
                                                                                    {productDeletingId === product.id ? 'Удаляем...' : 'Удалить'}
                                                                                </button>
                                                                            </td>
                                                                        </tr>

                                                                    ))}
                                                                </Fragment>
                                                            );
                                                        })}
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        )}
                                    </section>
                                </>
                            )}
                        </div>
                    </div>
                    {editingProduct && productForm && (
                        <div className="admin-drawer-layer">
                            <button
                                className="admin-drawer-scrim"
                                type="button"
                                aria-label="Закрыть форму редактирования"
                                onClick={handleCloseProductForm}
                            />

                            <section
                                className="admin-product-dialog"
                                role="dialog"
                                aria-modal="true"
                                aria-labelledby="admin-product-dialog-title"
                            >
                                <header className="admin-drawer-header">
                                    <div>
                                        <p className="admin-eyebrow">ТОВАР #{editingProduct.id}</p>
                                        <h2 id="admin-product-dialog-title">Редактирование товара</h2>
                                    </div>

                                    <button
                                        className="admin-icon-button"
                                        type="button"
                                        aria-label="Закрыть"
                                        onClick={handleCloseProductForm}
                                        disabled={productSaving}
                                    >
                                        ×
                                    </button>
                                </header>

                                <form
                                    className="admin-product-form"
                                    onSubmit={handleUpdateProduct}
                                >
                                    <div className="admin-drawer-content">
                                        <div className="admin-product-form-grid">
                                            <label className="admin-product-field">
                                                Название
                                                <input
                                                    required
                                                    value={productForm.name}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            name: event.target.value,
                                                        })
                                                    }
                                                />
                                            </label>

                                            <label className="admin-product-field">
                                                Категория
                                                <select
                                                    required
                                                    value={productForm.categoryId}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            categoryId: Number(event.target.value),
                                                        })
                                                    }
                                                >
                                                    {productCategoriesForForm.map(category => (
                                                        <option key={category.id} value={category.id}>
                                                            {category.name}
                                                        </option>
                                                    ))}
                                                </select>
                                            </label>

                                            {categoriesError && (
                                                <p className="admin-alert" role="alert">
                                                    {categoriesError}
                                                </p>
                                            )}

                                            <label className="admin-product-field admin-product-field--wide">
                                                Описание
                                                <textarea
                                                    required
                                                    rows={4}
                                                    value={productForm.description}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            description: event.target.value,
                                                        })
                                                    }
                                                />
                                            </label>

                                            <label className="admin-product-field">
                                                Цена
                                                <input
                                                    required
                                                    type="number"
                                                    min="0"
                                                    step="0.01"
                                                    value={productForm.price}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            price: Number(event.target.value),
                                                        })
                                                    }
                                                />
                                            </label>

                                            <label className="admin-product-field">
                                                Порядок сортировки
                                                <input
                                                    required
                                                    type="number"
                                                    value={productForm.sortOrder}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            sortOrder: Number(event.target.value),
                                                        })
                                                    }
                                                />
                                            </label>
                                            <label className="admin-product-field">
                                                Добавить фото
                                                <input
                                                    type="file"
                                                    accept="image/jpeg,image/png,image/webp"
                                                    disabled={productSaving}
                                                    onChange={event =>
                                                        setProductImageFile(event.currentTarget.files?.[0] ?? null)
                                                    }
                                                />
                                            </label>
                                            {(productImagePreview || productForm.images[0]) && (
                                                <img
                                                    src={productImagePreview || productForm.images[0]}
                                                    alt="Главное фото товара"
                                                    style={{width: 180, height: 180, objectFit: 'contain'}}
                                                />
                                            )}

                                            <label className="admin-product-field">
                                                Остаток
                                                <input
                                                    required
                                                    type="number"
                                                    min="0"
                                                    step="1"
                                                    value={productForm.stockQuantity}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            stockQuantity: Number(event.target.value),
                                                        })
                                                    }
                                                />
                                            </label>
                                        </div>

                                        <div className="admin-product-checkboxes">
                                            <label>
                                                <input
                                                    type="checkbox"
                                                    checked={productForm.trackStock}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            trackStock: event.target.checked,
                                                        })
                                                    }
                                                />
                                                Учитывать остаток
                                            </label>

                                            <label>
                                                <input
                                                    type="checkbox"
                                                    checked={productForm.active}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            active: event.target.checked,
                                                        })
                                                    }
                                                />
                                                Товар активен
                                            </label>

                                            <label>
                                                <input
                                                    type="checkbox"
                                                    checked={productForm.featured}
                                                    onChange={event =>
                                                        setProductForm({
                                                            ...productForm,
                                                            featured: event.target.checked,
                                                        })
                                                    }
                                                />
                                                Популярный товар
                                            </label>
                                        </div>

                                        {productSaveError && (
                                            <p className="admin-alert" role="alert">
                                                {productSaveError}
                                            </p>
                                        )}
                                    </div>

                                    <footer className="admin-product-form-actions">
                                        <button
                                            className="admin-secondary-button"
                                            type="button"
                                            onClick={handleCloseProductForm}
                                            disabled={productSaving}
                                        >
                                            Отмена
                                        </button>

                                        <button
                                            className="admin-primary-button"
                                            type="submit"
                                            disabled={productSaving}
                                        >
                                            {productSaving ? 'Сохранение...' : 'Сохранить'}
                                        </button>
                                    </footer>
                                </form>
                            </section>
                        </div>
                    )}

                    {isDetailsOpen && (
                        <div className="admin-drawer-layer">
                            <button
                                className="admin-drawer-scrim"
                                type="button"
                                aria-label="Закрыть заказ"
                                onClick={handleCloseDetails}
                            />
                            <aside
                                className="admin-order-drawer"
                                role="dialog"
                                aria-modal="true"
                                aria-labelledby="admin-drawer-title"
                            >
                                <header className="admin-drawer-header">
                                    <div>
                                        <p className="admin-eyebrow">ПОДРОБНОСТИ</p>
                                        <h2 id="admin-drawer-title">
                                            {drawerOrderId === null
                                                ? 'Заказ'
                                                : 'Заказ №' + drawerOrderId}
                                        </h2>
                                    </div>
                                    <button
                                        className="admin-icon-button"
                                        type="button"
                                        aria-label="Закрыть подробности"
                                        onClick={handleCloseDetails}
                                    >
                                        ×
                                    </button>
                                </header>

                                {detailsLoadingId !== null ? (
                                    <div className="admin-empty-state">
                                        <span className="admin-loader"/>
                                        <p>Загружаем заказ...</p>
                                    </div>
                                ) : detailsError ? (
                                    <p className="admin-alert" role="alert">
                                        {detailsError}
                                    </p>
                                ) : selectedOrder ? (
                                    <div className="admin-drawer-content">
                                        <section className="admin-detail-section">
                                            <div className="admin-detail-status-line">
                                                <h3>Информация о заказе</h3>
                                                {drawerStatus && (
                                                    <span
                                                        className={
                                                            'admin-status admin-status--'
                                                            + drawerStatus.toLowerCase()
                                                        }
                                                    >
                                                        {getStatusText(drawerStatus)}
                                                    </span>
                                                )}
                                            </div>

                                            <dl className="admin-detail-list">
                                                <div>
                                                    <dt>Покупатель</dt>
                                                    <dd>{selectedOrderSummary?.customerName ?? '—'}</dd>
                                                </div>
                                                <div>
                                                    <dt>Телефон</dt>
                                                    <dd>{selectedOrderSummary?.customerPhone ?? '—'}</dd>
                                                </div>
                                                <div>
                                                    <dt>Создан</dt>
                                                    <dd>
                                                        {selectedOrderSummary
                                                            ? formatOrderDate(selectedOrderSummary.createdAt)
                                                            : '—'}
                                                    </dd>
                                                </div>
                                                <div>
                                                    <dt>Сумма</dt>
                                                    <dd>
                                                        {selectedOrderSummary
                                                            ? selectedOrderSummary.totalAmount + ' ₽'
                                                            : '—'}
                                                    </dd>
                                                </div>
                                            </dl>
                                        </section>

                                        <section className="admin-detail-section">
                                            <h3>Состав заказа</h3>
                                            <ul className="admin-order-items">
                                                {selectedOrder.items.map(item => (
                                                    <li key={item.productId}>
                                                        <span>
                                                            <strong>{item.productName}</strong>
                                                            <small>{item.quantity} шт.</small>
                                                        </span>
                                                        <strong>{item.totalPrice} ₽</strong>
                                                    </li>
                                                ))}
                                            </ul>
                                        </section>

                                        <section className="admin-detail-section">
                                            <h3>Комментарий клиента</h3>
                                            <p className="admin-customer-comment">
                                                {selectedOrder.customerComment || 'Комментария нет'}
                                            </p>
                                        </section>

                                        <section className="admin-detail-section">
                                            {selectedOrder.source === 'TELEGRAM' ? (
                                                <>
                                                    <h3>Сообщение клиенту в Telegram</h3>
                                                    <textarea
                                                        className="admin-message-input"
                                                        value={messageText}
                                                        onChange={event => setMessageText(event.target.value)}
                                                        maxLength={2000}
                                                        rows={4}
                                                        placeholder="Введите сообщение клиенту"
                                                    />
                                                    <div className="admin-message-footer">
                                                        <span>{messageText.length}/2000</span>
                                                        <button
                                                            className="admin-secondary-button"
                                                            type="button"
                                                            onClick={() => void handleSendMessage()}
                                                            disabled={
                                                                messageSending
                                                                || !messageText.trim()
                                                            }
                                                        >
                                                            {messageSending
                                                                ? 'Отправляем...'
                                                                : 'Отправить сообщение'}
                                                        </button>
                                                    </div>
                                                    {messageError && (
                                                        <p className="admin-alert" role="alert">
                                                            {messageError}
                                                        </p>
                                                    )}
                                                    {messageSuccess && (
                                                        <p className="admin-success-message" role="status">
                                                            {messageSuccess}
                                                        </p>
                                                    )}
                                                </>
                                            ) : (
                                                <>
                                                    <h3>Сообщение клиенту</h3>
                                                    <p className="admin-customer-comment">
                                                        Отправка сообщения доступна только для Telegram-заказов.
                                                    </p>
                                                </>
                                            )}
                                        </section>

                                        {statusError && (
                                            <p className="admin-alert" role="alert">
                                                {statusError}
                                            </p>
                                        )}

                                        {getAvailableStatusActions(selectedOrder.status).length > 0 && (
                                            <div className="admin-status-actions">
                                                {getAvailableStatusActions(selectedOrder.status).map(action => (
                                                    <button
                                                        className={
                                                            action.status === 'CANCELLED'
                                                                ? 'admin-danger-button'
                                                                : 'admin-primary-button'
                                                        }
                                                        key={action.status}
                                                        type="button"
                                                        disabled={statusUpdatingId === selectedOrder.id}
                                                        onClick={() =>
                                                            void handleChangeOrderStatus(
                                                                selectedOrder.id,
                                                                action.status
                                                            )
                                                        }
                                                    >
                                                        {statusUpdatingId === selectedOrder.id
                                                            ? 'Сохраняем...'
                                                            : action.label}
                                                    </button>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                ) : null}
                            </aside>
                        </div>
                    )}
                </div>
            )}
        </main>
    );
}
