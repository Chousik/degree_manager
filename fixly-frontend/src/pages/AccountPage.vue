<script setup>
import { onMounted, ref, watch } from 'vue';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const { isLoggedIn, userId } = useSession();
const profile = ref(null);
const notifications = ref(null);
const loading = ref(false);
const error = ref('');

async function loadAccount() {
  if (!isLoggedIn.value || !userId.value) {
    profile.value = null;
    notifications.value = null;
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const data = await fetchJson(`${USER_API_BASE}/account/${userId.value}`);
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
  [() => isLoggedIn.value, () => userId.value],
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
</script>

<template>
  <div class="dashboard">
    <MainHeader />

    <section class="dashboard-section">
      <h2>Профиль</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите, чтобы увидеть данные профиля.</p>
      <p v-else-if="error" class="dashboard-note error">{{ error }}</p>
      <p v-else-if="loading" class="dashboard-note">Загружаем данные...</p>
      <div v-else-if="profile" class="dashboard-card profile-card">
        <div class="profile-heading">
          <div>
            <div class="profile-name">{{ profile.name }} {{ profile.surname }}</div>
            <div class="profile-email">{{ profile.email }}</div>
          </div>
          <span class="chip ghost">ID: {{ profile.id }}</span>
        </div>
        <div class="profile-details">
          <span>Город: {{ profile.city || '—' }}</span>
          <span>Телефон: {{ profile.phone || '—' }}</span>
          <span>Статус: {{ profile.status || '—' }}</span>
        </div>
        <div class="profile-details">
          <span>Создан: {{ profile.createdAt ? new Date(profile.createdAt).toLocaleDateString('ru-RU') : '—' }}</span>
          <span>Рейтинг: {{ profile.rating ?? '—' }}</span>
        </div>
      </div>
    </section>

    <section class="dashboard-section">
      <h2>Настройки уведомлений</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">После входа вы сможете увидеть свои настройки уведомлений.</p>
      <p v-else-if="error && !notifications" class="dashboard-note error">{{ error }}</p>
      <p v-else-if="loading" class="dashboard-note">Обновляем настройки...</p>
      <ul v-else-if="notifications" class="notifications-list">
        <li v-for="(label, key) in notificationLabels" :key="key">
          <span>{{ label }}</span>
          <span :class="notifications[key] ? 'status-on' : 'status-off'">{{ notifications[key] ? 'Включено' : 'Выключено' }}</span>
        </li>
      </ul>
    </section>
  </div>
</template>
