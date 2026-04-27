export type Shop = {
    id: string;
    sellerId: string;
    name: string;
    description: string;
    logoUrl: string | null;
    headerImageUrl: string | null;
    slug: string;
    status: "DRAFT" | "ACTIVE" | "INACTIVE";
    createdAt: string;
    updatedAt: string;
};