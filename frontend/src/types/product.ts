import type { ProductCategoryValue } from "./category";

export type ProductStatus = "ACTIVE" | "INACTIVE" | "DRAFT" | "RECALLED";

export type Product = {
    id: string;
    sellerId: string;
    shopId: string;
    name: string;
    slug: string;
    description: string;
    price: number;
    category: ProductCategoryValue;
    imageUrl?: string | null;
    productionDate?: string | null;
    bestBeforeDate?: string | null;
    stockQuantity: number;
    status: ProductStatus;
};

export type CreateProductPayload = {
    name: string;
    description: string;
    price: number;
    category: ProductCategoryValue;
    productionDate?: string | null;
    bestBeforeDate?: string | null;
    stockQuantity: number;
    status: ProductStatus;
};

export type UpdateProductPayload = {
    name: string;
    description: string;
    price: number;
    category: ProductCategoryValue;
    productionDate?: string | null;
    bestBeforeDate?: string | null;
    stockQuantity: number;
    status: ProductStatus;
};

export type PageResponse<T> = {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
};

export type ProductSearchParams = {
    page?: number;
    size?: number;
    sort?: string;
    category?: ProductCategoryValue;
    status?: ProductStatus;
    name?: string;
};

export type StorefrontSearchParams = {
    query?: string;
    category?: ProductCategoryValue;
    page?: number;
    size?: number;
};
