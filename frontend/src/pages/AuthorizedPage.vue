<template>
  <div>Завершение аутентификации...</div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

onMounted(async () => {
  const code = route.query.code;
  const codeVerifier = sessionStorage.getItem('pkce_code_verifier');

  if (!code || !codeVerifier) {
    router.push('/login');
    return;
  }

  try {
    // Обмен кода на токен
    const response = await fetch('http://localhost:8071/oauth2/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': 'Basic ' + btoa('client:secret')
      },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        code,
        redirect_uri: 'http://localhost:8080/auth-callback',
        code_verifier: codeVerifier
      })
    });

    if (!response.ok) throw new Error('Ошибка получения токена');

    const tokens = await response.json();
    localStorage.setItem('access_token', tokens.access_token);
    sessionStorage.removeItem('pkce_code_verifier');
    router.push('/');
  } catch (error) {
    console.error('Ошибка:', error);
    router.push('/login');
  }
});
</script>