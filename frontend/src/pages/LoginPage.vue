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
      <div v-if="errorMessage" class="text-red-600 text-sm mt-2 text-center">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const login = ref('');
const password = ref('');
const errorMessage = ref('');

async function handleSubmit() {
  errorMessage.value = ''; // сброс при новой попытке
  try {
    const loginResponse = await fetch('http://localhost:8071/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        username: login.value,
        password: password.value,
      }),
      credentials: 'include'
    });

    if (!loginResponse.ok) {
      const errorText = await loginResponse.text();
      throw new Error('Неверный логин или пароль');
    }

    const codeVerifier = generateCodeVerifier();
    const codeChallenge = await generateCodeChallenge(codeVerifier);
    sessionStorage.setItem('pkce_code_verifier', codeVerifier);

    const authUrl = new URL('http://localhost:8071/oauth2/authorize');
    authUrl.searchParams.append('response_type', 'code');
    authUrl.searchParams.append('client_id', 'client');
    authUrl.searchParams.append('redirect_uri', 'http://localhost:5173/auth-callback');
    authUrl.searchParams.append('code_challenge', codeChallenge);
    authUrl.searchParams.append('code_challenge_method', 'S256');
    authUrl.searchParams.append('scope', 'openid');

    window.location.href = authUrl.toString();

  } catch (error) {
    console.error('Ошибка:', error);
    errorMessage.value = error.message || 'Ошибка входа';
  }
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

