import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        jsessionId: null,
        codeVerifier: null,
        accessToken: null
    }),
    actions: {
        setJSessionId(id) {
            this.jsessionId = id
        },
        setCodeVerifier(verifier) {
            this.codeVerifier = verifier
        },
        setAccessToken(token) {
            this.accessToken = token
        }
    }
})
