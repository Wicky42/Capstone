export type CartItem = {
    productId: string
    productName: string
    unitPrice: number
    quantity: number
    maxQuantity: number
    titleImage: string | null
    shopId: string
    sellerId: string
};

export type Cart = {
    items : CartItem[]
};