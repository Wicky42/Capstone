import {
    Button,
    Calendar,
    CalendarCell,
    CalendarGrid,
    CalendarGridBody,
    CalendarGridHeader,
    CalendarHeaderCell,
    DateInput,
    DatePicker,
    DateSegment,
    Dialog,
    Group,
    Heading,
    Popover,
} from "react-aria-components";
import type { DateValue } from "react-aria-components";
import "./DatePickerField.css";

type DatePickerFieldProps = {
    id?: string;
    label: string;
    value: DateValue | null;
    onChange: (value: DateValue | null) => void;
    isDisabled?: boolean;
    errorMessage?: string;
};

export default function DatePickerField({
    id,
    label,
    value,
    onChange,
    isDisabled = false,
    errorMessage,
}: DatePickerFieldProps) {
    return (
        <DatePicker
            id={id}
            value={value}
            onChange={onChange}
            isDisabled={isDisabled}
            className="dpf"
        >
            <span className="dpf__label">{label}</span>

            <Group className="dpf__group">
                <DateInput className="dpf__input">
                    {(segment) => (
                        <DateSegment
                            segment={segment}
                            className={`dpf__segment${segment.isPlaceholder ? " dpf__segment--placeholder" : ""}${segment.type === "literal" ? " dpf__segment--literal" : ""}`}
                        />
                    )}
                </DateInput>
                <Button className="dpf__trigger" aria-label="Kalender öffnen">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        aria-hidden
                    >
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                        <line x1="16" y1="2" x2="16" y2="6" />
                        <line x1="8" y1="2" x2="8" y2="6" />
                        <line x1="3" y1="10" x2="21" y2="10" />
                    </svg>
                </Button>
            </Group>

            {errorMessage && (
                <p className="dpf__error">{errorMessage}</p>
            )}

            <Popover className="dpf__popover">
                <Dialog className="dpf__dialog">
                    <Calendar className="dpf__calendar">
                        <header className="dpf__cal-header">
                            <Button slot="previous" className="dpf__cal-nav" aria-label="Vorheriger Monat">
                                ‹
                            </Button>
                            <Heading className="dpf__cal-heading" />
                            <Button slot="next" className="dpf__cal-nav" aria-label="Nächster Monat">
                                ›
                            </Button>
                        </header>

                        <CalendarGrid className="dpf__grid">
                            <CalendarGridHeader>
                                {(day) => (
                                    <CalendarHeaderCell className="dpf__weekday">
                                        {day}
                                    </CalendarHeaderCell>
                                )}
                            </CalendarGridHeader>
                            <CalendarGridBody>
                                {(date) => (
                                    <CalendarCell
                                        date={date}
                                        className="dpf__cell"
                                    />
                                )}
                            </CalendarGridBody>
                        </CalendarGrid>
                    </Calendar>
                </Dialog>
            </Popover>
        </DatePicker>
    );
}

