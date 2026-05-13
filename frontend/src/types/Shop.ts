import type {ShopStatus} from "./shopStatus.ts";

export type Shop = {
    id: string;
    sellerId: string;
    name: string;
    description: string;
    logoUrl: string | null;
    headerImageUrl: string | null;
    slug: string;
    status: ShopStatus;
    createdAt: string;
    updatedAt: string;
};
