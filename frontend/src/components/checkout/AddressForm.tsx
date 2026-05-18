import type { FC } from "react";
import type { Address } from "../../types/address";
import "./AddressForm.css";

type AddressErrors = Partial<Record<keyof Address, string>>;

type Props = {
    legend: string;
    value: Address;
    onChange: (val: Address) => void;
    errors?: AddressErrors;
};

const AddressForm: FC<Props> = ({ legend, value, onChange, errors = {} }) => {
    const set = (field: keyof Address) => (e: React.ChangeEvent<HTMLInputElement>) =>
        onChange({ ...value, [field]: e.target.value });

    return (
        <fieldset className="address-form">
            <legend>{legend}</legend>

            <div className="address-form__row address-form__row--split">
                <div className="form-field">
                    <label className="form-label" htmlFor={`${legend}-street`}>Straße *</label>
                    <input
                        id={`${legend}-street`}
                        className={`input${errors.street ? " input--error" : ""}`}
                        value={value.street}
                        onChange={set("street")}
                        placeholder="Musterstraße"
                        autoComplete="street-address"
                    />
                    {errors.street && <span className="address-form__error">{errors.street}</span>}
                </div>

                <div className="form-field address-form__house-number">
                    <label className="form-label" htmlFor={`${legend}-houseNumber`}>Nr. *</label>
                    <input
                        id={`${legend}-houseNumber`}
                        className={`input${errors.houseNumber ? " input--error" : ""}`}
                        value={value.houseNumber}
                        onChange={set("houseNumber")}
                        placeholder="12a"
                    />
                    {errors.houseNumber && <span className="address-form__error">{errors.houseNumber}</span>}
                </div>
            </div>

            <div className="address-form__row address-form__row--split">
                <div className="form-field address-form__postal-code">
                    <label className="form-label" htmlFor={`${legend}-postalCode`}>PLZ *</label>
                    <input
                        id={`${legend}-postalCode`}
                        className={`input${errors.postalCode ? " input--error" : ""}`}
                        value={value.postalCode}
                        onChange={set("postalCode")}
                        placeholder="10115"
                        maxLength={5}
                        inputMode="numeric"
                    />
                    {errors.postalCode && <span className="address-form__error">{errors.postalCode}</span>}
                </div>

                <div className="form-field">
                    <label className="form-label" htmlFor={`${legend}-city`}>Stadt *</label>
                    <input
                        id={`${legend}-city`}
                        className={`input${errors.city ? " input--error" : ""}`}
                        value={value.city}
                        onChange={set("city")}
                        placeholder="Berlin"
                        autoComplete="address-level2"
                    />
                    {errors.city && <span className="address-form__error">{errors.city}</span>}
                </div>
            </div>

            <div className="form-field">
                <label className="form-label" htmlFor={`${legend}-country`}>Land *</label>
                <input
                    id={`${legend}-country`}
                    className={`input${errors.country ? " input--error" : ""}`}
                    value={value.country}
                    onChange={set("country")}
                    placeholder="Deutschland"
                    autoComplete="country-name"
                />
                {errors.country && <span className="address-form__error">{errors.country}</span>}
            </div>
        </fieldset>
    );
};

export default AddressForm;

