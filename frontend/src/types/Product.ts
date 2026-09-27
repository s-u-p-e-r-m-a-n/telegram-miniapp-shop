// export разрешает другим файлам доступ к Product
export type Product = {
    id: number;
    categoryId: number;
    categoryName: string;
    name: string;
    description: string;
    price: number;
    images: string[];
    featured: boolean;
    stockQuantity: number;
    trackStock: boolean;
};