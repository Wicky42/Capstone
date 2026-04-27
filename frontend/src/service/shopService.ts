import axios from "axios";
import type { Shop } from "../types/Shop";

export type CreateShopRequest = {
    name: string;
    description: string;
};

export async function createShop(request: CreateShopRequest): Promise<Shop> {
    const response = await axios.post("/api/seller/shops", request);
    return response.data;
}

export async function getMyShop(): Promise<Shop> {
    const response = await axios.get("/api/seller/shops/my");
    return response.data;
}