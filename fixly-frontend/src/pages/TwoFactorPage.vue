<script setup>
import { onMounted, ref, watch } from 'vue';
import MainHeader from '../components/MainHeader.vue';
import { useSession } from '../state/session';
import { getTwoFactorStatus, startTwoFactorSetup, confirmTwoFactor, disableTwoFactor } from '../api/twoFactor';

const { isLoggedIn } = useSession();
const loading = ref(false);
const bannerError = ref('');
const setupError = ref('');
const disableError = ref('');
const status = ref({ enabled: false });
const setup = ref(null);
const qrUrl = ref('');
const code = ref('');
const disableCode = ref('');

async function loadStatus() {
  if (!isLoggedIn.value) {
    status.value = { enabled: false };
    setup.value = null;
    return;
  }
  try {
    const data = await getTwoFactorStatus();
    status.value = data || { enabled: false };
    if (!status.value.enabled) {
      await beginSetup();
    } else {
      setup.value = null;
      qrUrl.value = '';
    }
  } catch (err) {
    bannerError.value = err?.message || 'Не удалось получить статус 2FA';
  }
}

async function beginSetup() {
  if (!isLoggedIn.value) return;
  loading.value = true;
  setupError.value = '';
  bannerError.value = '';
  try {
    const data = await startTwoFactorSetup();
    setup.value = data;
    qrUrl.value = data?.otpauthUrl
      ? `https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=${encodeURIComponent(data.otpauthUrl)}`
      : '';
    code.value = '';
  } catch (err) {
    setupError.value = err?.message || 'Не удалось сгенерировать QR-код, попробуйте позже';
  } finally {
    loading.value = false;
  }
}

async function confirmSetup() {
  if (!code.value) {
    setupError.value = 'Введите код из приложения Google Authenticator';
    return;
  }
  loading.value = true;
  setupError.value = '';
  try {
    await confirmTwoFactor(code.value.trim());
    code.value = '';
    await loadStatus();
  } catch (err) {
    setupError.value = err?.message || 'Код не подошёл, попробуйте снова';
  } finally {
    loading.value = false;
  }
}

async function disable() {
  if (!disableCode.value) {
    disableError.value = 'Введите код из приложения, чтобы отключить 2FA';
    return;
  }
  loading.value = true;
  disableError.value = '';
  bannerError.value = '';
  try {
    await disableTwoFactor(disableCode.value.trim());
    disableCode.value = '';
    await loadStatus();
  } catch (err) {
    disableError.value = err?.message || 'Не удалось отключить 2FA';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadStatus();
});

watch(
  () => isLoggedIn.value,
  () => {
    loadStatus();
  }
);
</script>

<template>
  <div class="dashboard">
    <MainHeader />

    <section class="dashboard-section profile-grid">
      <h2>Двухфакторная аутентификация</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите в систему, чтобы управлять двухфакторной аутентификацией.</p>
      <template v-else>
        <p v-if="bannerError" class="dashboard-note error">{{ bannerError }}</p>

        <div v-if="!status.enabled" class="twofactor-card">
          <p class="twofactor-text">
            Сканируйте QR-код в Google Authenticator или введите ключ вручную:
            <strong>{{ setup?.secret || '...' }}</strong>
          </p>
          <p class="twofactor-note warning">
            Сохраните эту фразу в надёжном месте. Без неё восстановить доступ при проблемах с устройством невозможно.
          </p>
          <div v-if="setup" class="twofactor-qr">
            <img :src="qrUrl" alt="QR-код для двухфакторной аутентификации">
          </div>
          <label class="twofactor-label">
            Код подтверждения
            <input
              v-model="code"
              type="text"
              inputmode="numeric"
              autocomplete="one-time-code"
              placeholder="123456"
            >
          </label>
        <p v-if="setupError" class="twofactor-note error">{{ setupError }}</p>
          <div class="twofactor-actions">
            <button class="landing-btn primary" type="button" :disabled="loading" @click="confirmSetup">
              {{ loading ? 'Проверяем...' : 'Подтвердить' }}
            </button>
          </div>
        </div>

        <div v-else class="twofactor-card">
          <p>Двухфакторная аутентификация уже включена. Введите текущий код, чтобы отключить её.</p>
          <label class="twofactor-label">
            Код из приложения
            <input
              v-model="disableCode"
              type="text"
              inputmode="numeric"
              autocomplete="one-time-code"
              placeholder="123456"
            >
          </label>
          <p v-if="disableError" class="twofactor-note error">{{ disableError }}</p>
          <div class="twofactor-actions">
            <button class="landing-btn danger" type="button" :disabled="loading" @click="disable">
              {{ loading ? 'Проверяем...' : 'Отключить 2FA' }}
            </button>
          </div>
        </div>
      </template>
    </section>
  </div>
</template>

<style scoped>
.twofactor-note {
  margin: 8px 0;
  font-size: 0.95rem;
}

.twofactor-note.error {
  color: #d64545;
}

.twofactor-note.warning {
  color: #b55b00;
}
</style>

