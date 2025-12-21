<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { RouterLink, useRoute } from 'vue-router';
import {
  API_BASE,
  EMAIL_VERIFY_ENDPOINT,
  EMAIL_VERIFY_PARAM,
} from '../api/client';
import { useSession } from '../state/session';
import AuthShell from '../components/AuthShell.vue';

const { state } = useSession();
const route = useRoute();

const toast = reactive({ message: '', type: 'success', visible: false });
const submitting = reactive({ login: false, verify: false });
const loginForm = reactive({ username: '', password: '' });
const pendingEmail = computed(() => state.pendingEmail);

const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};

const authUrl = computed(() => {
  const redirect = encodeURIComponent('http://localhost:5173/auth-callback');
  return `${API_BASE}/oauth2/authorize?response_type=code&client_id=client&redirect_uri=${redirect}&scope=openid%20offline_access`;
});

const handleLogin = async () => {
  if (submitting.login) return;
  submitting.login = true;
  toast.visible = false;

  try {
    const formData = new FormData();
    formData.append('username', loginForm.username.trim());
    formData.append('password', loginForm.password);

    const res = await fetch(`${API_BASE}/login`, {
      method: 'POST',
      credentials: 'include',
      headers: { Accept: 'application/json' },
      body: formData,
    });

    if (!res.ok) {
      throw new Error('Неверный логин или пароль или email ещё не подтверждён');
    }

    showToast('Пароль верен. Перенаправляем для получения токена...', 'success');
    window.location.href = authUrl.value;
  } catch (err) {
    showToast(err.message || 'Ошибка при входе', 'error');
  } finally {
    submitting.login = false;
  }
};

const verifyEmail = async (token) => {
  if (!token || submitting.verify) return;
  submitting.verify = true;

  try {
    const endpoint = EMAIL_VERIFY_ENDPOINT.startsWith('/')
      ? `${API_BASE}${EMAIL_VERIFY_ENDPOINT}`
      : `${API_BASE}/${EMAIL_VERIFY_ENDPOINT}`;

    const res = await fetch(`${endpoint}?${EMAIL_VERIFY_PARAM}=${encodeURIComponent(token)}`, { method: 'POST' });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || 'Не удалось подтвердить почту');
    }
    showToast('Email подтверждён. Теперь можно войти.', 'success');
  } catch (err) {
    showToast(err.message || 'Не удалось подтвердить почту', 'error');
  } finally {
    submitting.verify = false;
  }
};

onMounted(() => {
  const token = route.query[EMAIL_VERIFY_PARAM];
  if (token) {
    verifyEmail(token);
  }
});
</script>

<template>
  <AuthShell>
    <form class="form" @submit.prevent="handleLogin">
      <div class="field">
        <label for="login-username">Логин</label>
        <input
          id="login-username"
          v-model="loginForm.username"
          name="username"
          type="text"
          autocomplete="username"
          required
          placeholder="master89"
        >
      </div>
      <div class="field">
        <label for="login-password">Пароль</label>
        <input
          id="login-password"
          v-model="loginForm.password"
          name="password"
          type="password"
          autocomplete="current-password"
          required
          placeholder="••••••••"
        >
      </div>
      <button type="submit" class="btn primary" :disabled="submitting.login">
        {{ submitting.login ? 'Входим...' : 'Войти' }}
      </button>
      <p class="helper">
        Нет аккаунта?
        <RouterLink class="link" to="/register">Зарегистрируйтесь</RouterLink>
      </p>
    </form>

    <div v-if="pendingEmail" class="notice">
      Письмо отправлено на {{ pendingEmail }}. Подтвердите email и вернитесь, чтобы войти.
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type" role="status" aria-live="polite">
      {{ toast.message }}
    </div>
  </AuthShell>
</template>
