<script setup>
import { reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { API_BASE } from '../api/client';
import {
  hasEmoji,
  isValidEmail,
  isValidName,
  isValidPhone,
  isValidUsername,
  normalizePhone,
} from '../utils/validation';
import { useSession } from '../state/session';
import AuthShell from '../components/AuthShell.vue';

const router = useRouter();
const { setPendingEmail } = useSession();

const toast = reactive({ message: '', type: 'success', visible: false });
const submitting = ref(false);
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
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

const resetForm = () => {
  Object.keys(registerForm).forEach((key) => {
    registerForm[key] = '';
  });
};

const handleRegister = async () => {
  if (submitting.value) return;
  submitting.value = true;
  toast.visible = false;

  try {
    const errors = [];
    const username = registerForm.username.trim();
    const email = registerForm.email.trim();
    const name = registerForm.name.trim();
    const surname = registerForm.surname.trim();
    const lastName = registerForm.lastName.trim();
    const normalizedPhone = normalizePhone(registerForm.phone);

    if (hasEmoji(username)) errors.push('Логин не должен содержать эмоджи.');
    if (!isValidUsername(username)) errors.push('Логин должен быть 3-30 символов (латиница, цифры, ._-).');
    if (hasEmoji(email)) errors.push('Email не должен содержать эмоджи.');
    if (!isValidEmail(email)) errors.push('Введите корректный email.');
    if (hasEmoji(registerForm.password)) errors.push('Пароль не должен содержать эмоджи.');
    if (registerForm.password.length < 8) errors.push('Пароль должен быть минимум 8 символов.');
    if (registerForm.password !== registerForm.confirmPassword) errors.push('Пароли не совпадают.');
    if (hasEmoji(name)) errors.push('Имя не должно содержать эмоджи.');
    if (!isValidName(name)) errors.push('Имя может содержать только буквы, пробелы и дефис.');
    if (hasEmoji(surname)) errors.push('Фамилия не должна содержать эмоджи.');
    if (!isValidName(surname)) errors.push('Фамилия может содержать только буквы, пробелы и дефис.');
    if (lastName) {
      if (hasEmoji(lastName)) errors.push('Отчество не должно содержать эмоджи.');
      if (!isValidName(lastName)) errors.push('Отчество может содержать только буквы, пробелы и дефис.');
    }
    if (hasEmoji(registerForm.phone)) errors.push('Телефон не должен содержать эмоджи.');
    if (!isValidPhone(normalizedPhone)) errors.push('Телефон укажите в формате +7XXXXXXXXXX.');

    if (errors.length) {
      throw new Error(errors[0]);
    }

    const payload = {
      username,
      email,
      password: registerForm.password,
      name,
      surname,
      lastName,
      phone: normalizedPhone,
    };

    const res = await fetch(`${API_BASE}/users/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      const data = await res.text();
      throw new Error(data || 'Ошибка при регистрации');
    }

    setPendingEmail(payload.email);
    resetForm();
    showToast('Письмо для подтверждения отправлено. Сейчас перенаправим на страницу входа.', 'success');
    setTimeout(() => router.push('/login'), 1200);
  } catch (err) {
    showToast(err.message || 'Ошибка при регистрации', 'error');
  } finally {
    submitting.value = false;
  }
};
</script>

<template>
  <AuthShell>
    <form class="form" @submit.prevent="handleRegister">
      <div class="grid">
        <div class="field">
          <label for="reg-username">Логин</label>
          <input
            id="reg-username"
            v-model="registerForm.username"
            name="username"
            required
            minlength="3"
            maxlength="30"
            pattern="[A-Za-z0-9._-]+"
            autocomplete="username"
            placeholder="fixly_user"
          >
        </div>
        <div class="field">
          <label for="reg-email">Email</label>
          <input
            id="reg-email"
            v-model="registerForm.email"
            name="email"
            type="email"
            required
            autocomplete="email"
            placeholder="you@example.com"
          >
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
          <label for="reg-password-confirm">Подтвердите пароль</label>
          <input
            id="reg-password-confirm"
            v-model="registerForm.confirmPassword"
            name="confirmPassword"
            type="password"
            required
            minlength="8"
            placeholder="Повторите пароль"
          >
        </div>
        <div class="field">
          <label for="reg-name">Имя</label>
          <input
            id="reg-name"
            v-model="registerForm.name"
            name="name"
            required
            maxlength="20"
            autocomplete="given-name"
            placeholder="Имя"
          >
        </div>
        <div class="field">
          <label for="reg-surname">Фамилия</label>
          <input
            id="reg-surname"
            v-model="registerForm.surname"
            name="surname"
            required
            maxlength="60"
            autocomplete="family-name"
            placeholder="Фамилия"
          >
        </div>
        <div class="field">
          <label for="reg-lastname">Отчество (опционально)</label>
          <input
            id="reg-lastname"
            v-model="registerForm.lastName"
            name="lastName"
            maxlength="20"
            autocomplete="additional-name"
            placeholder="Отчество"
          >
        </div>
        <div class="field">
          <label for="reg-phone">Телефон (опционально)</label>
          <input
            id="reg-phone"
            v-model="registerForm.phone"
            name="phone"
            inputmode="tel"
            maxlength="18"
            pattern="\\+7\\d{10}"
            autocomplete="tel"
            placeholder="+7XXXXXXXXXX"
          >
        </div>
      </div>
      <p class="helper small">
        Нажимая “Зарегистрироваться”, вы соглашаетесь с правилами Fixly и даёте согласие на обработку данных.
      </p>
      <button type="submit" class="btn primary" :disabled="submitting">
        {{ submitting ? 'Создаём профиль...' : 'Зарегистрироваться' }}
      </button>
      <p class="helper">
        Уже с нами?
        <RouterLink class="link" to="/login">Войти</RouterLink>
      </p>
    </form>

    <div v-if="toast.visible" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </AuthShell>
</template>
