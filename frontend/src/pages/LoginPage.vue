<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-tr from-[#C9D7FF] from-50% ...">
    <div class="bg-white p-8 rounded-lg shadow-md w-auto h-auto border border-gray-300">
      <div class="grid grid-cols-[50px_1fr] mb-6">
        <img src="/favicon.png" class="h-11 place-self-center">
        <h2 class="text-center place-self-start ml-4 mt-2
                  text-xl font-medium text-gray-800">Stankin Works</h2></div>
      <form @submit.prevent="handleSubmit">
        <div class="mb-4">
          <label class="block text-gray-700 mb-1">Login</label>
          <input
              v-model="login"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg
              focus:outline-none focus:border-indigo-400"
          />
        </div>
        <div class="mb-6">
          <label class="block text-gray-700 mb-1">Password</label>
          <input
              v-model="password"
              type="password"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg
              focus:outline-none focus:border-indigo-400"
          />
        </div>
        <button
            type="submit"
            class="w-full bg-black text-white py-2 rounded-lg hover:bg-gray-800 transition"
        >
          Sign In
        </button>
      </form>
      <div class="mt-4 text-center">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const login = ref('');
const password = ref('');

async function handleSubmit() {
  try {
    // 1. Отправляем логин/пароль (получаем куки)
    const loginResponse = await fetch('http://localhost:8071/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        username: login.value,
        password: password.value,
      }),
      credentials: 'include' // Важно для сохранения кук
    });

    if (!loginResponse.redirected) {
      throw new Error('Ошибка входа: неверные учетные данные');
    }

    // 2. Генерируем PKCE параметры
    const codeVerifier = generateCodeVerifier();
    const codeChallenge = await generateCodeChallenge(codeVerifier);
    sessionStorage.setItem('pkce_code_verifier', codeVerifier);

    // 3. Формируем URL для OAuth авторизации
    const authUrl = new URL('http://localhost:8071/oauth2/authorize');
    authUrl.searchParams.append('response_type', 'code');
    authUrl.searchParams.append('client_id', 'client');
    authUrl.searchParams.append('redirect_uri', 'https://www.manning.com/authorized');
    authUrl.searchParams.append('code_challenge', codeChallenge);
    authUrl.searchParams.append('code_challenge_method', 'S256');
    authUrl.searchParams.append('scope', 'openid');

    // 4. Перенаправляем пользователя на OAuth endpoint
    window.location.href = authUrl.toString();

  } catch (error) {
    console.error('Ошибка:', error);
    alert(error.message || 'Ошибка входа');
  }
}


async function initOAuthFlow() {
  // Генерация PKCE параметров
  const codeVerifier = generateCodeVerifier();
  const codeChallenge = await generateCodeChallenge(codeVerifier);
  sessionStorage.setItem('pkce_code_verifier', codeVerifier);

  // Формирование OAuth URL
  const authUrl = new URL('http://localhost:8071/oauth2/authorize');
  const params = {
    response_type: 'code',
    client_id: 'client',
    redirect_uri: 'https://www.manning.com/authorized',
    code_challenge: codeChallenge,
    code_challenge_method: 'S256',
    scope: 'openid'
  };

  Object.entries(params).forEach(([key, val]) => {
    authUrl.searchParams.append(key, val);
  });

  // Перенаправление
  window.location.href = authUrl.toString();
}
function generateCodeVerifier() {
  const array = new Uint8Array(32);
  window.crypto.getRandomValues(array);
  return base64UrlEncode(array);
}

async function generateCodeChallenge(verifier) {
  const encoder = new TextEncoder();
  const data = encoder.encode(verifier);
  const digest = await window.crypto.subtle.digest('SHA-256', data);
  return base64UrlEncode(digest);
}

function base64UrlEncode(buffer) {
  return btoa(String.fromCharCode(...new Uint8Array(buffer)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
}
</script>

