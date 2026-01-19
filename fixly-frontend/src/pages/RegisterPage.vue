<script setup>
import { reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { API_BASE } from '../api/client';
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
    if (registerForm.password !== registerForm.confirmPassword) {
      throw new Error('Пароли не совпадают');
    }
    const payload = {
      username: registerForm.username.trim(),
      email: registerForm.email.trim(),
      password: registerForm.password,
      name: registerForm.name.trim(),
      surname: registerForm.surname.trim(),
      lastName: registerForm.lastName.trim(),
      phone: registerForm.phone.trim(),
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
