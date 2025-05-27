<template>
  <div>Завершение аутентификации...</div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/store/auth';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

onMounted(async () => {
  const code = route.query.code;
  const codeVerifier = sessionStorage.getItem('pkce_code_verifier');

  if (!code || !codeVerifier) {
    await router.push('/login');
    return;
  }

  try {
    const response = await fetch('http://localhost:8071/oauth2/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic ' + btoa('client:secret')
      },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        code,
        redirect_uri: 'http://localhost:5173/auth-callback',
        code_verifier: codeVerifier,
        client_id: 'client'
      }),
      credentials: 'include'
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.error || 'Ошибка получения токена');
    }

    const tokens = await response.json();

    authStore.setTokens(tokens.access_token , tokens.refresh_token || '');
    sessionStorage.removeItem('pkce_code_verifier');

    await router.push('/works');

  } catch (error) {
    console.error('Ошибка:', error);
    alert('Ошибка аутентификации: ' + error.message);
    await router.push('/login');
  }
});
</script>