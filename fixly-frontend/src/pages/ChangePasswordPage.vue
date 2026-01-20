<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';
import { getTwoFactorStatus } from '../api/twoFactor';
import { hasEmoji, isValidOtp } from '../utils/validation';

const router = useRouter();
const { isLoggedIn } = useSession();

const form = ref({ current: '', next: '', confirm: '', otp: '' });
const error = ref('');
const success = ref('');
const loading = ref(false);
const twoFactorEnabled = ref(false);

function resetForm() {
  form.value = { current: '', next: '', confirm: '', otp: '' };
}

onMounted(async () => {
  if (!isLoggedIn.value) return;
  try {
    const status = await getTwoFactorStatus();
    twoFactorEnabled.value = Boolean(status?.enabled);
  } catch (err) {
    twoFactorEnabled.value = false;
  }
});

async function handleSubmit() {
  if (loading.value) {
    return;
  }
  error.value = '';
  success.value = '';
  const { current, next: nextPassword, confirm, otp } = form.value;
  if (!current || !nextPassword || !confirm) {
    error.value = 'Заполните все поля формы';
    return;
  }
  if (nextPassword.length < 8) {
    error.value = 'Новый пароль должен содержать минимум 8 символов';
    return;
  }
  if (hasEmoji(current) || hasEmoji(nextPassword) || hasEmoji(confirm)) {
    error.value = 'Пароль не должен содержать эмоджи';
    return;
  }
  if (nextPassword !== confirm) {
    error.value = 'Новый пароль и подтверждение не совпадают';
    return;
  }
  if (twoFactorEnabled.value && !isValidOtp(otp || '')) {
    error.value = 'Введите 6-значный код из приложения Google Authenticator';
    return;
  }
  loading.value = true;
  try {
    const payload = { oldPassword: current, newPassword: nextPassword };
    if (twoFactorEnabled.value) {
      payload.otp = otp;
    }
    await fetchJson(`${API_BASE}/users/password`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    success.value = 'Пароль успешно обновлён';
    resetForm();
  } catch (err) {
    const message = err?.message || 'Не удалось сменить пароль';
    error.value = message;
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.back();
}
</script>

<template>
  <div class="dashboard">
    <MainHeader />
    <section class="dashboard-section profile-grid">
      <div>
        <h2>Смена пароля</h2>
        <p v-if="!isLoggedIn" class="dashboard-note">Войдите в аккаунт, чтобы менять пароль.</p>
        <template v-else>
          <p v-if="error" class="dashboard-note error">{{ error }}</p>
          <p v-else-if="success" class="dashboard-note success">{{ success }}</p>
          <form class="password-form" @submit.prevent="handleSubmit">
            <label>
              Текущий пароль
              <input
                v-model="form.current"
                type="password"
                name="currentPassword"
                autocomplete="current-password"
                placeholder="••••••••"
                required
              >
            </label>
            <label>
              Новый пароль
              <input
                v-model="form.next"
                type="password"
                name="newPassword"
                autocomplete="new-password"
                placeholder="Не менее 8 символов"
                required
              >
            </label>
            <label>
              Подтвердите пароль
              <input
                v-model="form.confirm"
                type="password"
                name="confirmPassword"
                autocomplete="new-password"
                placeholder="Повторите новый пароль"
                required
              >
            </label>
            <label v-if="twoFactorEnabled">
              Код из приложения
              <input
                v-model="form.otp"
                type="text"
                inputmode="numeric"
                autocomplete="one-time-code"
                placeholder="123456"
                maxlength="6"
                pattern="\\d{6}"
                required
              >
            </label>
            <div class="password-form__actions">
              <button type="button" class="landing-btn ghost" @click="goBack">Назад</button>
              <button class="landing-btn primary" type="submit" :disabled="loading">
                {{ loading ? 'Сохраняем...' : 'Обновить пароль' }}
              </button>
            </div>
          </form>
        </template>
      </div>
    </section>
  </div>
</template>
