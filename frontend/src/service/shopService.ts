import api from "./api";
import type { Shop } from "../types/Shop";

export type CreateShopRequest = {
    name: string;
    description: string;
};

export async function createShop(request: CreateShopRequest): Promise<Shop> {
    const response = await api.post("/seller/shops", request);
    return response.data;
}

export async function getMyShop(): Promise<Shop> {
    const response = await api.get("/seller/shops/my");
    return response.data;
}