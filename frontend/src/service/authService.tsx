// src/services/authService.ts
import axios from "axios";

export type UserRole = "SELLER" | "CUSTOMER";

const api = axios.create({
    baseURL: "/api",
    withCredentials: true,
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
        await api.post("/auth/logout");
    },
};