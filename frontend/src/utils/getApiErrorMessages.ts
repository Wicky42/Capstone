import axios from "axios";

export function getApiErrorMessage(error: unknown): string {
    if (axios.isAxiosError(error)) {
        const data = error.response?.data;

        if (
            data &&
            typeof data === "object" &&
            "message" in data &&
            typeof data.message === "string"
        ) {
            return data.message;
        }

        if (typeof data === "string") {
            return data;
        }

        if (error.response?.status === 403) {
            return "Du hast keine Berechtigung für diese Aktion.";
        }

        if (error.response?.status === 404) {
            return "Der Eintrag wurde nicht gefunden.";
        }

        if (error.response?.status === 413) {
            return "Die Datei ist zu groß.";
        }
    }

    return "Ein unerwarteter Fehler ist aufgetreten.";
}