<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  API_BASE,
  EMAIL_VERIFY_ENDPOINT,
  EMAIL_VERIFY_PARAM,
} from '../api/client';
import { useSession } from '../state/session';

const { setLoggedIn, setPendingEmail, setUserIdFromToken } = useSession();
const route = useRoute();
const router = useRouter();

const activeTab = ref('login');
const toast = reactive({ message: '', type: 'success', visible: false });
const submitting = reactive({ login: false, register: false, verify: false });
const pendingEmail = ref('');

const loginForm = reactive({ username: '', password: '' });
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  name: '',
  surname: '',
  lastName: '',
  phone: '',
});

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

const handleRegister = async () => {
  if (submitting.register) return;
  submitting.register = true;
  toast.visible = false;

  try {
    const payload = {
      username: registerForm.username.trim(),
      email: registerForm.email.trim(),
      password: registerForm.password,
      name: registerForm.name.trim(),
      surname: registerForm.surname.trim(),
      lastName: registerForm.lastName.trim(),
      phone: registerForm.phone.trim(),
    };

    const res = await fetch(`${API_BASE}/api/users/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const data = await res.text();
      throw new Error(data || 'Ошибка при регистрации');
    }

    pendingEmail.value = registerForm.email.trim();
    setPendingEmail(pendingEmail.value);
    showToast('Письмо для подтверждения отправлено. Проверьте почту и перейдите по ссылке.', 'success');
    Object.keys(registerForm).forEach((key) => {
      registerForm[key] = '';
    });
    activeTab.value = 'login';
  } catch (err) {
    showToast(err.message || 'Ошибка при регистрации', 'error');
  } finally {
    submitting.register = false;
  }
};

const handleOAuth = (provider) => {
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
    activeTab.value = 'login';
  } catch (err) {
    showToast(err.message || 'Не удалось подтвердить почту', 'error');
  } finally {
    submitting.verify = false;
  }
};

onMounted(() => {
  const url = new URL(window.location.href);
  const token = url.searchParams.get(EMAIL_VERIFY_PARAM);
  if (token) {
    verifyEmail(token);
  }
  if (route.query.tab === 'register') {
    activeTab.value = 'register';
  }
});
</script>

<template>
  <div>
    <div class="tab-bar inner">
      <button class="tab" type="button" :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'">
        Вход
      </button>
      <button class="tab" type="button" :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'">
        Регистрация
      </button>
    </div>

    <form v-show="activeTab === 'login'" class="form" @submit.prevent="handleLogin">
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
      <p class="helper small">Вводите обычный логин и пароль — дальше мы сами перенаправим в личный кабинет.</p>
      <button type="submit" class="btn primary" :disabled="submitting.login">
        {{ submitting.login ? 'Входим...' : 'Войти' }}
      </button>
      <p class="helper">Нет аккаунта? <span class="link" @click="activeTab = 'register'">Зарегистрируйтесь</span></p>
    </form>

    <form v-show="activeTab === 'register'" class="form" @submit.prevent="handleRegister">
      <div class="grid">
        <div class="field">
          <label for="reg-username">Логин</label>
          <input id="reg-username" v-model="registerForm.username" name="username" required placeholder="fixly_user">
        </div>
        <div class="field">
          <label for="reg-email">Email</label>
          <input id="reg-email" v-model="registerForm.email" name="email" type="email" required placeholder="you@example.com">
        </div>
        <div class="field">
          <label for="reg-password">Пароль</label>
          <input
            id="reg-password"
            v-model="registerForm.password"
            name="password"
            type="password"
            required
            minlength="8"
            placeholder="Минимум 8 символов"
          >
        </div>
        <div class="field">
          <label for="reg-name">Имя</label>
          <input id="reg-name" v-model="registerForm.name" name="name" required maxlength="20" placeholder="Имя">
        </div>
        <div class="field">
          <label for="reg-surname">Фамилия</label>
          <input id="reg-surname" v-model="registerForm.surname" name="surname" required maxlength="60" placeholder="Фамилия">
        </div>
        <div class="field">
          <label for="reg-lastname">Отчество (опционально)</label>
          <input id="reg-lastname" v-model="registerForm.lastName" name="lastName" maxlength="20" placeholder="Отчество">
        </div>
        <div class="field">
          <label for="reg-phone">Телефон (опционально)</label>
          <input id="reg-phone" v-model="registerForm.phone" name="phone" maxlength="12" placeholder="+7XXXXXXXXXX">
        </div>
      </div>
      <p class="helper small">
        Нажимая “Зарегистрироваться”, вы соглашаетесь с правилами Fixly и даёте согласие на обработку данных.
      </p>
      <button type="submit" class="btn primary" :disabled="submitting.register">
        {{ submitting.register ? 'Создаём профиль...' : 'Зарегистрироваться' }}
      </button>
      <p class="helper">Уже с нами? <span class="link" @click="activeTab = 'login'">Войти</span></p>
    </form>

    <div v-if="pendingEmail" class="notice">
      Письмо отправлено на {{ pendingEmail }}. Подтвердите email и вернитесь, чтобы войти.
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type" role="status" aria-live="polite">
      {{ toast.message }}
    </div>
  </div>
</template>
