import { useState, useCallback } from "react";
import type { Cart, CartItem } from "../types/cart";

const CART_KEY = "cart";

const loadCart = (): Cart => {
    try {
        const raw = localStorage.getItem(CART_KEY);
        if (!raw) return { items: [] };
        return JSON.parse(raw) as Cart;
    } catch {
        return { items: [] };
    }
};

const saveCart = (cart: Cart): void => {
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
};

export const useCart = () => {
    const [cart, setCart] = useState<Cart>(loadCart);

    const persist = useCallback((updated: Cart) => {
        saveCart(updated);
        setCart(updated);
    }, []);

    /** Produkt hinzufügen oder Menge erhöhen (begrenzt auf maxQuantity) */
    const addItem = useCallback(
        (item: Omit<CartItem, "quantity">, quantity: number = 1) => {
            setCart((prev) => {
                const existing = prev.items.find((i) => i.productId === item.productId);
                let updated: Cart;

                if (existing) {
                    const newQty = Math.min(
                        existing.quantity + quantity,
                        existing.maxQuantity
                    );
                    updated = {
                        items: prev.items.map((i) =>
                            i.productId === item.productId
                                ? { ...i, quantity: newQty }
                                : i
                        ),
                    };
                } else {
                    const clampedQty = Math.min(Math.max(quantity, 1), item.maxQuantity);
                    updated = {
                        items: [...prev.items, { ...item, quantity: clampedQty }],
                    };
                }

                saveCart(updated);
                return updated;
            });
        },
        []
    );

    /** Menge eines Items setzen (min 1, max maxQuantity) */
    const updateQuantity = useCallback((productId: string, quantity: number) => {
        setCart((prev) => {
            const updated: Cart = {
                items: prev.items.map((i) => {
                    if (i.productId !== productId) return i;
                    const clamped = Math.min(Math.max(quantity, 1), i.maxQuantity);
                    return { ...i, quantity: clamped };
                }),
            };
            saveCart(updated);
            return updated;
        });
    }, []);

    /** Einzelnes Item entfernen */
    const removeItem = useCallback((productId: string) => {
        setCart((prev) => {
            const updated: Cart = {
                items: prev.items.filter((i) => i.productId !== productId),
            };
            saveCart(updated);
            return updated;
        });
    }, []);

    /** Gesamten Warenkorb leeren */
    const clearCart = useCallback(() => {
        const empty: Cart = { items: [] };
        saveCart(empty);
        setCart(empty);
    }, []);

    const totalItems = cart.items.reduce((sum, i) => sum + i.quantity, 0);

    return {
        items: cart.items,
        totalItems,
        addItem,
        updateQuantity,
        removeItem,
        clearCart,
    };
};

