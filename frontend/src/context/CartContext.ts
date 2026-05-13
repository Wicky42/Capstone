import { createContext, useContext } from "react";
import type { CartItem } from "../types/cart";

export type CartContextValue = {
    items: CartItem[];
    totalItems: number;
    addItem: (item: Omit<CartItem, "quantity">, quantity?: number) => void;
    updateQuantity: (productId: string, quantity: number) => void;
    removeItem: (productId: string) => void;
    clearCart: () => void;
};

export const CartContext = createContext<CartContextValue | null>(null);

export const useCartContext = (): CartContextValue => {
    const ctx = useContext(CartContext);
    if (!ctx) {
        throw new Error("useCartContext muss innerhalb von <CartProvider> verwendet werden.");
    }
    return ctx;
};

