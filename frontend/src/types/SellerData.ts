import type { Address } from "./Adress.ts";

export type SellerData = {
    businessName: string;
    address: Address;
    billingAddress: Address;
    taxId: string;
};