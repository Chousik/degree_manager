<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { API_BASE } from '../api/client';
import { useSession } from '../state/session';

const status = ref('Получаю код...');
const route = useRoute();
const router = useRouter();
const { setTokens, setLoggedIn, setUserIdFromToken } = useSession();

const exchangeCode = async (code) => {
  status.value = 'Обмениваю код на токен...';
  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    code,
    redirect_uri: 'http://localhost:5173/auth-callback',
    client_id: 'client',
  });
  const res = await fetch(`${API_BASE}/oauth2/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      Authorization: 'Basic ' + btoa('client:secret'),
    },
    body,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Не удалось получить токен');
  }
  const data = await res.json();
  setTokens(data.access_token, data.refresh_token);
  setLoggedIn(true);
  setUserIdFromToken(data.access_token);
  status.value = 'Успех! Перенаправляю...';
  router.replace('/listings');
};

onMounted(async () => {
  const code = route.query.code;
  if (!code) {
    status.value = 'Нет кода авторизации.';
    return;
  }
  try {
    await exchangeCode(code);
  } catch (err) {
    status.value = err.message || 'Ошибка при обмене кода';
  }
});
</script>

<template>
  <div class="api-playground">
    <div class="section">
      <h3>OAuth вход</h3>
      <p>{{ status }}</p>
    </div>
  </div>
</template>
