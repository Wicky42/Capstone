// src/services/authService.ts
import axios from "axios";

export type UserRole = "SELLER" | "CUSTOMER";

// 5. Hilfsfunktion: liest einen Cookie-Wert anhand des Namens
function getCookie(name: string): string | null {
    const match = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]*)"));
    return match ? decodeURIComponent(match[2]) : null;
}

const api = axios.create({
    baseURL: "/api",
    withCredentials: true, // Session-Cookie wird bei jedem Request mitgeschickt
});

// 5. CSRF-Interceptor: liest XSRF-TOKEN-Cookie und setzt X-XSRF-TOKEN-Header
//    bei allen state-ändernden Methoden (POST, PUT, PATCH, DELETE)
api.interceptors.request.use((config) => {
    const method = config.method?.toUpperCase();
    if (method && ["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
        const token = getCookie("XSRF-TOKEN");
        if (token) {
            config.headers["X-XSRF-TOKEN"] = token;
        }
    }
    return config;
});

export const authService = {
    startGithubLogin() {
        window.location.href = "/oauth2/authorization/github";
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

    async logout() {
        await api.post("/logout");
    },
};