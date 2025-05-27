import { defineStore } from 'pinia';
import {jwtDecode} from "jwt-decode";

export const useAuthStore = defineStore('auth', {
    state: () => ({
        accessToken: null,
        refreshToken: null,
        userInfo: null
    }),
    actions: {
        setTokens(accessToken, refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userInfo = jwtDecode(accessToken);
        },
        clearAuth() {
            this.accessToken = null;
            this.refreshToken = null;
            this.userInfo = null;
        }
    },
    persist: true
});
