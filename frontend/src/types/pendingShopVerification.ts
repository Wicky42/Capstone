import type {ShopStatus} from './shopStatus';

export type PendingShopVerification = {
    shopId: string;
    shopName: string;
    shopDescription: string;
    sellerId: string;
    sellerName: string;
    sellerEmail: string;
    shopStatus: ShopStatus;
    onboardingCompleted: boolean;
    createdAt: string;
    updatedAt: string;
};