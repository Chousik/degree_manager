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
        },
        isAccessTokenExpired(bufferSeconds = 30) {
            if (!this.userInfo || !this.userInfo.exp) return true;
            const now = Math.floor(Date.now() / 1000);
            return now + bufferSeconds >= this.userInfo.exp;
        }
    },
    persist: true

});
