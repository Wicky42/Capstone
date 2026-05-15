import api from "./api";
import type { PageResponse, Product, StorefrontSearchParams } from "../types/product";
import type { Shop } from "../types/shop";
import type { CategoryOption } from "../types/category";

export const storefrontService = {
    async getStorefrontProducts(params?: StorefrontSearchParams): Promise<PageResponse<Product>> {
        const response = await api.get<PageResponse<Product>>("/public/storefront/products", { params });
        return response.data;
    },

    async getStorefrontShops(page = 0, size = 20): Promise<PageResponse<Shop>> {
        const response = await api.get<PageResponse<Shop>>("/public/storefront/shops", {
            params: { page, size },
        });
        return response.data;
    },

    async getProductById(productId: string): Promise<Product> {
        const response = await api.get<Product>(`/public/products/${productId}`);
        return response.data;
    },

    getProductImageUrl(productId: string): string {
        return `/api/public/products/${productId}/image`;
    },

    async getShopBySlug(slug: string): Promise<Shop> {
        const response = await api.get<Shop>(`/public/shops/by-slug/${slug}`);
        return response.data;
    },

    async getShopById(shopId: string): Promise<Shop> {
        const response = await api.get<Shop>(`/public/shops/${shopId}`);
        return response.data;
    },

    async getShopProducts(shopId: string, page = 0, size = 20): Promise<PageResponse<Product>> {
        const response = await api.get<PageResponse<Product>>(`/public/shops/${shopId}/products`, {
            params: { page, size },
        });
        return response.data;
    },

    async getCategories(): Promise<CategoryOption[]> {
        const response = await api.get<CategoryOption[]>("/public/categories");
        return response.data;
    },
};
