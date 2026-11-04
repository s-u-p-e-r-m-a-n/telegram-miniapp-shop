export type AdminProduct = {
    id: number;
    categoryId: number;
    categoryName: string;
    name: string;
    description: string;
    price: number;
    images: string[];
    active: boolean;
    featured: boolean;
    sortOrder: number;
    stockQuantity: number;
    trackStock: boolean;
};

export type AdminProductRequest = {
    categoryId: number;
    name: string;
    description: string;
    price: number;
    images: string[];
    active: boolean;
    featured: boolean;
    sortOrder: number;
    stockQuantity: number;
    trackStock: boolean;
};