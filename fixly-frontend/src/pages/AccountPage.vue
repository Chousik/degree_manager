<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';
import { getTwoFactorStatus, startTwoFactorSetup, confirmTwoFactor, disableTwoFactor as disableTwoFactorApi } from '../api/twoFactor';

const router = useRouter();
const { isLoggedIn, userId } = useSession();
const profile = ref(null);
const notifications = ref(null);
const loading = ref(false);
const error = ref('');
const notificationError = ref('');
const updatingNotification = ref('');
const twoFactor = reactive({
  enabled: false,
  loading: false,
  setup: null,
  qrUrl: '',
  code: '',
  disableCode: '',
  error: '',
  disableError: '',
  showDisable: false,
});

async function loadAccount() {
  if (!isLoggedIn.value) {
    profile.value = null;
    notifications.value = null;
    notificationError.value = '';
    return;
  }
  loading.value = true;
  error.value = '';
  notificationError.value = '';
  try {
    const data = await fetchJson(`${USER_API_BASE}/account/me`);
    profile.value = data?.profile ?? null;
    notifications.value = data?.notificationSettings ?? null;
  } catch (err) {
    error.value = 'Не удалось получить данные профиля. Перезайдите и попробуйте снова.';
    profile.value = null;
    notifications.value = null;
  } finally {
    loading.value = false;
  }
}

async function loadTwoFactorStatus() {
  if (!isLoggedIn.value) {
    twoFactor.enabled = false;
    twoFactor.setup = null;
    twoFactor.code = '';
    twoFactor.disableCode = '';
    twoFactor.showDisable = false;
    return;
  }
  try {
    const status = await getTwoFactorStatus();
    twoFactor.enabled = Boolean(status?.enabled);
    if (!twoFactor.enabled) {
      twoFactor.showDisable = false;
      twoFactor.disableCode = '';
    }
  } catch (err) {
    // ignore
  }
}

onMounted(() => {
  loadAccount();
  loadTwoFactorStatus();
});

watch(
  [() => isLoggedIn.value, () => userId.value],
  () => {
    loadAccount();
    loadTwoFactorStatus();
  }
);

const notificationLabels = {
  systemNotifications: 'Системные уведомления',
  rentalNotifications: 'Аренда и сделки',
  messageNotifications: 'Сообщения и чат',
  paymentNotifications: 'Оплаты и возвраты',
};

const statusLabels = {
  ACTIVE: 'Активен',
  BLOCKED: 'Заблокирован',
  PENDING: 'На проверке',
};

const formattedCreatedAt = computed(() => {
  if (!profile.value?.createdAt) {
    return '—';
  }
  return new Date(profile.value.createdAt).toLocaleDateString('ru-RU');
});

async function toggleNotification(settingKey) {
  if (!notifications.value || updatingNotification.value) {
    return;
  }
  const currentValue = Boolean(notifications.value[settingKey]);
  const nextValue = !currentValue;
  updatingNotification.value = settingKey;
  notificationError.value = '';
  notificationError.value = '';
  try {
    const payload = { [settingKey]: nextValue };
    const response = await fetchJson(`${USER_API_BASE}/account/me/notifications`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
    if (response) {
      notifications.value = response;
    } else {
      notifications.value = {
        ...notifications.value,
        [settingKey]: nextValue,
      };
    }
  } catch (err) {
    notificationError.value = err?.message || 'Не удалось обновить настройки уведомлений.';
  } finally {
    updatingNotification.value = '';
  }
}

function goToPasswordChange() {
  router.push('/account/password');
}

async function beginTwoFactorSetup() {
  if (twoFactor.loading) return;
  twoFactor.error = '';
  twoFactor.disableError = '';
  twoFactor.loading = true;
  try {
    const setup = await startTwoFactorSetup();
    twoFactor.setup = setup;
    twoFactor.qrUrl = `https://chart.googleapis.com/chart?chs=200x200&cht=qr&chl=${encodeURIComponent(setup.otpauthUrl)}`;
    twoFactor.code = '';
    twoFactor.showDisable = false;
  } catch (err) {
    twoFactor.error = err?.message || 'Не удалось подготовить двухфакторную аутентификацию';
  } finally {
    twoFactor.loading = false;
  }
}

function cancelTwoFactorSetup() {
  twoFactor.setup = null;
  twoFactor.qrUrl = '';
  twoFactor.code = '';
  twoFactor.error = '';
}

async function confirmTwoFactorCode() {
  if (!twoFactor.code) {
    twoFactor.error = 'Введите код из приложения';
    return;
  }
  twoFactor.loading = true;
  twoFactor.error = '';
  try {
    await confirmTwoFactor(twoFactor.code.trim());
    twoFactor.enabled = true;
    twoFactor.setup = null;
    twoFactor.qrUrl = '';
    twoFactor.code = '';
    await loadTwoFactorStatus();
  } catch (err) {
    twoFactor.error = err?.message || 'Код не подошёл, попробуйте снова';
  } finally {
    twoFactor.loading = false;
  }
}

function toggleDisableTwoFactor() {
  twoFactor.showDisable = !twoFactor.showDisable;
  twoFactor.disableError = '';
  twoFactor.disableCode = '';
}

async function disableTwoFactor() {
  if (!twoFactor.disableCode) {
    twoFactor.disableError = 'Укажите код из приложения';
    return;
  }
  twoFactor.loading = true;
  twoFactor.disableError = '';
  try {
    await disableTwoFactorApi(twoFactor.disableCode.trim());
    twoFactor.enabled = false;
    twoFactor.showDisable = false;
    twoFactor.disableCode = '';
    await loadTwoFactorStatus();
  } catch (err) {
    twoFactor.disableError = err?.message || 'Не удалось отключить 2FA';
  } finally {
    twoFactor.loading = false;
  }
}
</script>

<template>
  <div class="dashboard">
    <MainHeader />

    <section class="dashboard-section profile-grid">
      <h2>Профиль</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите, чтобы увидеть данные профиля.</p>
      <p v-else-if="error" class="dashboard-note error">{{ error }}</p>
      <p v-else-if="loading" class="dashboard-note">Загружаем данные...</p>
      <template v-else-if="profile">
        <div class="profile-panel">
          <div class="profile-summary">
            <div class="profile-details-block">
              <div class="profile-name">
                {{ profile.name }} {{ profile.surname }}
                <span v-if="profile.status" class="profile-status">
                  {{ statusLabels[profile.status] || profile.status }}
                </span>
              </div>
              <div class="profile-email">{{ profile.email }}</div>
              <div class="profile-meta">Город: {{ profile.city || '—' }}</div>
              <div class="profile-meta">Телефон: {{ profile.phone || '—' }}</div>
              <div class="profile-meta">Логин: {{ profile.username || '—' }}</div>
              <div class="profile-meta">Создан: {{ formattedCreatedAt }}</div>
              <button type="button" class="landing-btn ghost profile-password-btn" @click="goToPasswordChange">
                Сменить пароль
              </button>
              <div class="twofactor-actions">
                <p class="twofactor-status">
                  Двухфакторная аутентификация:
                  <span :class="twoFactor.enabled ? 'status-on' : 'status-off'">
                    {{ twoFactor.enabled ? 'Включена' : 'Выключена' }}
                  </span>
                </p>
                <button
                  type="button"
                  class="landing-btn ghost"
                  :disabled="twoFactor.loading"
                  @click="twoFactor.enabled ? toggleDisableTwoFactor() : beginTwoFactorSetup()"
                >
                  {{ twoFactor.enabled ? (twoFactor.showDisable ? 'Скрыть форму отключения' : 'Отключить 2FA') : 'Включить 2FA' }}
                </button>
              </div>
            </div>
            <div class="profile-stat">
              <div class="profile-stat__value">{{ profile.rating ?? '—' }}</div>
              <div class="profile-stat__label">Рейтинг</div>
            </div>
          </div>
          <div v-if="twoFactor.error" class="dashboard-note error">{{ twoFactor.error }}</div>
          <div v-if="twoFactor.setup" class="twofactor-setup">
            <p>Сканируйте QR-код в Google Authenticator или введите ключ вручную: <strong>{{ twoFactor.setup.secret }}</strong></p>
            <img :src="twoFactor.qrUrl" alt="QR-код для двухфакторной аутентификации" class="twofactor-qr">
            <label>
              Код подтверждения
              <input v-model="twoFactor.code" type="text" inputmode="numeric" placeholder="123456" />
            </label>
            <div class="twofactor-buttons">
              <button type="button" class="landing-btn primary" :disabled="twoFactor.loading" @click="confirmTwoFactorCode">
                Подтвердить
              </button>
              <button type="button" class="landing-btn ghost" @click="cancelTwoFactorSetup">Отмена</button>
            </div>
          </div>
          <div v-else-if="twoFactor.enabled && twoFactor.showDisable" class="twofactor-setup">
            <p>Введите текущий код из приложения, чтобы отключить 2FA</p>
            <label>
              Код подтверждения
              <input v-model="twoFactor.disableCode" type="text" inputmode="numeric" placeholder="123456" />
            </label>
            <p v-if="twoFactor.disableError" class="dashboard-note error">{{ twoFactor.disableError }}</p>
            <div class="twofactor-buttons">
              <button type="button" class="landing-btn danger" :disabled="twoFactor.loading" @click="disableTwoFactor">
                Отключить 2FA
              </button>
              <button type="button" class="landing-btn ghost" @click="toggleDisableTwoFactor">Отмена</button>
            </div>
          </div>
        </div>
      </template>
    </section>

    <section class="dashboard-section">
      <h2>Настройки уведомлений</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">После входа вы сможете увидеть свои настройки уведомлений.</p>
      <p v-else-if="error && !notifications" class="dashboard-note error">{{ error }}</p>
      <p v-else-if="loading" class="dashboard-note">Обновляем настройки...</p>
      <div v-else-if="notifications" class="notifications-settings">
        <p v-if="notificationError" class="dashboard-note error">{{ notificationError }}</p>
        <ul class="notifications-list">
          <li v-for="(label, key) in notificationLabels" :key="key">
            <span>{{ label }}</span>
            <button
              type="button"
              class="notification-toggle"
              :class="notifications[key] ? 'on' : 'off'"
              :aria-pressed="notifications[key]"
              :disabled="updatingNotification === key"
              @click="toggleNotification(key)"
            >
              {{ notifications[key] ? 'Включено' : 'Выключено' }}
            </button>
          </li>
        </ul>
      </div>
    </section>

  </div>
</template>
