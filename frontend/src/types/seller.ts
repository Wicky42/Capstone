import type { Address } from "./address";

export type SellerData = {
    businessName: string;
    address: Address;
    billingAddress: Address;
    taxId: string;
};