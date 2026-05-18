import { type FC, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCartContext } from "../../context/CartContext";
import { orderService } from "../../services/orderService";
import AddressForm from "../../components/checkout/AddressForm";
import OrderSummary from "../../components/checkout/OrderSummary";
import type { Address } from "../../types/address";
import type { OrderItemRequest } from "../../types/order";
import "./CheckoutPage.css";

type AddressErrors = Partial<Record<keyof Address, string>>;

const EMPTY_ADDRESS: Address = {
    street: "",
    houseNumber: "",
    postalCode: "",
    city: "",
    country: "",
};

function validateAddress(addr: Address): AddressErrors {
    const errors: AddressErrors = {};
    if (!addr.street.trim()) errors.street = "Pflichtfeld";
    if (!addr.houseNumber.trim()) errors.houseNumber = "Pflichtfeld";
    if (!/^\d{5}$/.test(addr.postalCode.trim())) errors.postalCode = "5-stellige PLZ erforderlich";
    if (!addr.city.trim()) errors.city = "Pflichtfeld";
    if (!addr.country.trim()) errors.country = "Pflichtfeld";
    return errors;
}

const CheckoutPage: FC = () => {
    const navigate = useNavigate();
    const { items, clearCart } = useCartContext();

    const [step, setStep] = useState<1 | 2>(1);
    const [shippingAddress, setShippingAddress] = useState<Address>(EMPTY_ADDRESS);
    const [billingAddress, setBillingAddress] = useState<Address>(EMPTY_ADDRESS);
    const [sameAddress, setSameAddress] = useState(true);
    const [shippingErrors, setShippingErrors] = useState<AddressErrors>({});
    const [billingErrors, setBillingErrors] = useState<AddressErrors>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);
    const [orderCompleted, setOrderCompleted] = useState(false);

    // Warenkorb leer → zurück zur CartPage
    if (items.length === 0 && !orderCompleted) {
        navigate("/cart", { replace: true });
        return null;
    }

    const handleNextStep = () => {
        const sErr = validateAddress(shippingAddress);
        const bErr = sameAddress ? {} : validateAddress(billingAddress);

        setShippingErrors(sErr);
        setBillingErrors(bErr);

        if (Object.keys(sErr).length > 0 || Object.keys(bErr).length > 0) return;

        if (sameAddress) setBillingAddress(shippingAddress);
        setStep(2);
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setSubmitError(null);

        const orderItems: OrderItemRequest[] = items.map((i) => ({
            productId: i.productId,
            productName: i.productName,
            unitPrice: i.unitPrice,
            quantity: i.quantity,
            titleImage: i.titleImage,
            shopId: i.shopId,
            sellerId: i.sellerId,
        }));

        try {
            const response = await orderService.checkout({
                items: orderItems,
                shippingAddress,
                billingAddress: sameAddress ? shippingAddress : billingAddress,
            });
            setOrderCompleted(true);
            clearCart();
            navigate(`/orders/${response.orderNumber}/confirmation`);
        } catch {
            setSubmitError(
                "Die Bestellung konnte nicht aufgegeben werden. Bitte versuche es erneut."
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="page checkout-page">
            <div className="container checkout-page__container">

                {/* Stepper-Header */}
                <div className="checkout-page__stepper" aria-label="Checkout-Fortschritt">
                    <div className={`checkout-page__step${step === 1 ? " checkout-page__step--active" : " checkout-page__step--done"}`}>
                        <span className="checkout-page__step-number">1</span>
                        <span className="checkout-page__step-label">Adresse</span>
                    </div>
                    <div className="checkout-page__step-divider" aria-hidden="true" />
                    <div className={`checkout-page__step${step === 2 ? " checkout-page__step--active" : ""}`}>
                        <span className="checkout-page__step-number">2</span>
                        <span className="checkout-page__step-label">Übersicht</span>
                    </div>
                </div>

                {/* ── SCHRITT 1: Adressformular ── */}
                {step === 1 && (
                    <div className="checkout-page__content">
                        <h1 className="checkout-page__title">Lieferadresse</h1>

                        <AddressForm
                            legend="Lieferadresse"
                            value={shippingAddress}
                            onChange={setShippingAddress}
                            errors={shippingErrors}
                        />

                        <label className="checkout-page__same-address-label">
                            <input
                                type="checkbox"
                                checked={sameAddress}
                                onChange={(e) => setSameAddress(e.target.checked)}
                            />
                            Rechnungsadresse ist gleich wie Lieferadresse
                        </label>

                        {!sameAddress && (
                            <>
                                <h2 className="checkout-page__subtitle">Rechnungsadresse</h2>
                                <AddressForm
                                    legend="Rechnungsadresse"
                                    value={billingAddress}
                                    onChange={setBillingAddress}
                                    errors={billingErrors}
                                />
                            </>
                        )}

                        <div className="checkout-page__actions">
                            <button
                                type="button"
                                className="button-secondary"
                                onClick={() => navigate("/cart")}
                            >
                                ← Zurück zum Warenkorb
                            </button>
                            <button
                                type="button"
                                className="button-primary"
                                onClick={handleNextStep}
                            >
                                Weiter zur Übersicht →
                            </button>
                        </div>
                    </div>
                )}

                {/* ── SCHRITT 2: Bestellübersicht ── */}
                {step === 2 && (
                    <div className="checkout-page__content">
                        <h1 className="checkout-page__title">Bestellübersicht</h1>

                        <OrderSummary
                            activeItems={items}
                            shippingAddress={shippingAddress}
                            billingAddress={sameAddress ? shippingAddress : billingAddress}
                        />

                        {submitError && (
                            <div className="alert alert--error" role="alert">
                                {submitError}
                            </div>
                        )}

                        <div className="checkout-page__actions">
                            <button
                                type="button"
                                className="button-secondary"
                                onClick={() => setStep(1)}
                                disabled={isSubmitting}
                            >
                                ← Adresse ändern
                            </button>
                            <button
                                type="button"
                                className="button-primary"
                                onClick={handleSubmit}
                                disabled={isSubmitting}
                            >
                                {isSubmitting ? "Bestellung wird aufgegeben…" : "Jetzt kaufen →"}
                            </button>
                        </div>
                    </div>
                )}

            </div>
        </div>
    );
};

export default CheckoutPage;
