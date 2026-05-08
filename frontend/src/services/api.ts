/**
 * Zentrale Axios-Instanz für alle API-Calls.
 * - withCredentials: Session-Cookie wird bei jedem Request mitgesendet
 * - CSRF-Interceptor: liest XSRF-TOKEN-Cookie und setzt X-XSRF-TOKEN-Header
 *   bei allen state-ändernden Methoden (POST, PUT, PATCH, DELETE)
 */
import axios from "axios";

function getCookie(name: string): string | null {
    const match = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]*)"));
    return match ? decodeURIComponent(match[2]) : null;
}

const api = axios.create({
    baseURL: "/api",
    withCredentials: true,
});

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

export default api;

