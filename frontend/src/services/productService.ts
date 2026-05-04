import type {
    CreateProductPayload,
    PageResponse,
    Product,
    ProductSearchParams,
    UpdateProductPayload,
} from "../types/product";
import api from "./api";

export const productService = {
    async getSellerProducts(
        params?: ProductSearchParams
    ): Promise<PageResponse<Product>> {
        const response = await api.get<PageResponse<Product>>("/seller/products", {
            params,
        });

        return response.data;
    },

    async searchProducts(
        params?: ProductSearchParams
    ): Promise<PageResponse<Product>> {
        const response = await api.get<PageResponse<Product>>("/products", {
            params,
        });

        return response.data;
    },

    async getProductById(productId: string): Promise<Product> {
        const response = await api.get<Product>(`/products/${productId}`);

        return response.data;
    },

    async createProduct(payload: CreateProductPayload): Promise<Product> {
        const response = await api.post<Product>("/seller/products", payload);

        return response.data;
    },

    async updateProduct(
        productId: string,
        payload: UpdateProductPayload
    ): Promise<Product> {
        const response = await api.put<Product>(
            `/seller/products/${productId}`,
            payload
        );

        return response.data;
    },

    async deleteProduct(productId: string): Promise<void> {
        await api.delete(`/seller/products/${productId}`);
    },

    async activateProduct(productId: string): Promise<Product> {
        const response = await api.put<Product>(
            `/seller/products/${productId}/activate`);
        return response.data;
    },

    async uploadProductImage(productId: string, file: File): Promise<Product> {
        const formData = new FormData();
        formData.append("file", file);

        const response = await api.post<Product>(
            `/seller/products/${productId}/image`,
            formData
        );

        return response.data;
    },

    async createProductWithOptionalImage(
        payload: CreateProductPayload,
        imageFile?: File | null
    ): Promise<Product> {
        const createdProduct = await productService.createProduct(payload);

        if (!imageFile) {
            return createdProduct;
        }

        return productService.uploadProductImage(createdProduct.id, imageFile);
    },
};