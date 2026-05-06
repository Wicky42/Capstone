import api from "./api.ts";
import type {PendingShopVerification} from "../types/pendingShopVerification.ts";
import type {Shop} from "../types/shop.ts";

export const adminShopService = {
    async getPendingShopVerifications(): Promise<PendingShopVerification[]>{
        const response = await api.get<PendingShopVerification[]>("/admin/shops/pending-verifications");
        return response.data;
    },

    async activateShop(shopId: string): Promise<Shop>{
        const response = await api.put<Shop>(`api/admin/${shopId}/activate`);
        return response.data;
    },

    async deactivate(shopId: string) : Promise<Shop>{
        const response = await api.put<Shop>(`api/admin/${shopId}/deactivate`);
        return response.data;
    },

    async rejectShop(shopId: string) : Promise<Shop>{
        const response = await api.put<Shop>(`api/admin/${shopId}/reject`);
        return response.data;
    }
}
