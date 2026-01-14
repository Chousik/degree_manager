<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const router = useRouter();
const { isLoggedIn } = useSession();
const profile = ref(null);
const notifications = ref(null);
const loading = ref(false);
const error = ref('');
const notificationError = ref('');
const updatingNotification = ref('');

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

onMounted(() => {
  loadAccount();
});

watch(
  () => isLoggedIn.value,
  () => {
    loadAccount();
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

function goToTwoFactor() {
  router.push('/account/2fa');
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
              <div class="profile-actions">
                <button type="button" class="landing-btn ghost" @click="goToPasswordChange">Сменить пароль</button>
                <button type="button" class="landing-btn ghost" @click="goToTwoFactor">Настроить 2FA</button>
              </div>
            </div>
            <div class="profile-stat">
              <div class="profile-stat__value">{{ profile.rating ?? '—' }}</div>
              <div class="profile-stat__label">Рейтинг</div>
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
