import {useAuthStore} from "@/store/authStore.js";
import router from "@/route.js";


export async function authFetch(url, options = {}) {
    const authStore = useAuthStore();

    if (authStore.isAccessTokenExpired()) {
        const success = await refreshToken();
        if (!success) {
            authStore.clearAuth()
            await router.push('/login')
        }
    }

    const finalHeaders = {
        ...options.headers,
        'Authorization': 'Bearer ' + authStore.accessToken,
    };

    return fetch(url, {
        ...options,
        headers: finalHeaders
    });
}


async function refreshToken() {
    const authStore = useAuthStore();

    try {
        const response = await fetch('http://localhost:8071/oauth2/token', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                 Authorization: 'Basic ' + btoa('client:secret')
            },
            body: new URLSearchParams({
                grant_type: 'refresh_token',
                refresh_token: authStore.refreshToken,
            }),
        });
        if (!response.ok) return false

        const data = await response.json()
        authStore.setTokens(data.access_token, data.refresh_token)
        return true
    } catch (e) {
        console.error('Ошибка при обновлении токена:', e)
    }
}
