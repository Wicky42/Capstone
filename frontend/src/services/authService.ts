// src/services/authService.ts
import api from "./api";

export type UserRole = "SELLER" | "CUSTOMER";

// Hilfsfunktion: liest einen Cookie-Wert anhand des Namens
function getCookie(name: string): string | null {
    const match = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]*)"));
    return match ? decodeURIComponent(match[2]) : null;
}


export const authService = {
    startGithubLogin() {
        window.location.href = "/oauth2/authorization/github";
    },

    /**
     * Ruft den CSRF-Endpoint auf → Backend schreibt XSRF-TOKEN-Cookie in die Response.
     * Muss vor jedem state-ändernden Request aufgerufen werden (z. B. beim App-Start).
     */
    async getCsrf() {
        await api.get("/auth/csrf");
    },

    async getMe() {
        const response = await api.get("/auth/me");
        return response.data;
    },

    async register(role: UserRole) {
        const response = await api.post("/auth/register", null, {
            params: { role },
        });
        return response.data;
    },

    /**
     * Meldet den Nutzer ab: POST /api/auth/logout (via Vite-Proxy, mit CSRF-Token).
     * Backend invalidiert die Session und antwortet mit 204 – kein Redirect.
     * Die Navigation zur /logout-Seite übernimmt der Aufrufer.
     */
    async logout() {
        // Sicherstellen, dass XSRF-TOKEN-Cookie existiert
        if (!getCookie("XSRF-TOKEN")) {
            await api.get("/auth/csrf");
        }
        // POST läuft durch den /api-Proxy → kein CORS-Problem
        // Der Axios-Interceptor setzt X-XSRF-TOKEN automatisch
        await api.post("/auth/logout");
    },
};