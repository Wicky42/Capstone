export type ProductStatus = "ACTIVE" | "INACTIVE" | "DRAFT" | "RECALLED";

export type Product = {
    id: string;
    sellerId: string;
    shopId: string;
    name: string;
    description: string;
    price: number;
    category: string;
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
    category: string;
    productionDate?: string | null;
    bestBeforeDate?: string | null;
    stockQuantity: number;
    status: ProductStatus;
};

export type UpdateProductPayload = {
    name: string;
    description: string;
    price: number;
    category: string;
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
    category?: string;
    status?: ProductStatus;
    name?: string;
};
