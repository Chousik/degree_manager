<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import MainHeader from '../components/MainHeader.vue';
import { getUserNotifications } from '../api/notifications';
import { useSession } from '../state/session';

const { isLoggedIn, userId } = useSession();

const notifications = ref([]);
const loading = ref(false);
const error = ref('');
let pollTimer = null;
const POLL_MS = 15000;

function formatDate(value) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'short' });
}

const displayedNotifications = computed(() =>
  notifications.value.map((item) => ({
    id: item.id,
    title: item.type || 'Уведомление',
    body: item.body || '',
    time: formatDate(item.createdAt),
    isRead: Boolean(item.isRead),
  }))
);

async function loadNotifications() {
  if (!isLoggedIn.value || !userId.value) {
    notifications.value = [];
    stopPolling();
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const data = await getUserNotifications(userId.value);
    notifications.value = Array.isArray(data) ? data : [];
    startPolling();
  } catch (err) {
    error.value = 'Не удалось загрузить уведомления. Попробуйте обновить страницу.';
    notifications.value = [];
    stopPolling();
  } finally {
    loading.value = false;
  }
}

function startPolling() {
  stopPolling();
  pollTimer = setInterval(() => {
    // не блокируем UI — делаем тихое обновление, ошибки игнорируем
    if (!isLoggedIn.value || !userId.value) {
      stopPolling();
      return;
    }
    getUserNotifications(userId.value)
      .then((data) => {
        if (Array.isArray(data)) {
          notifications.value = data;
        }
      })
      .catch(() => {});
  }, POLL_MS);
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

onMounted(() => {
  loadNotifications();
});

watch(
  [() => userId.value, () => isLoggedIn.value],
  () => {
    loadNotifications();
  }
);

onUnmounted(() => {
  stopPolling();
});
</script>

<template>
  <div class="notifications-page">
    <MainHeader />
    <section class="notifications-section">
      <div class="notifications-header">
        <div class="notifications-title">Уведомления</div>
      </div>

      <p v-if="!isLoggedIn" class="notifications-empty">
        Авторизуйтесь, чтобы увидеть уведомления.
      </p>
      <p v-else-if="loading" class="notifications-empty">
        Загружаем уведомления...
      </p>
      <p v-else-if="error" class="notifications-empty">
        {{ error }}
      </p>

      <div v-else-if="displayedNotifications.length" class="notifications-list">
        <article
          v-for="item in displayedNotifications"
          :key="item.id"
          class="notification-card"
          :data-read="item.isRead"
        >
          <div class="notification-card__head">
            <h3>{{ item.title }}</h3>
            <span>{{ item.time }}</span>
          </div>
          <p>{{ item.body }}</p>
        </article>
      </div>
      <div v-else class="notifications-empty">
        Пока нет уведомлений
      </div>
    </section>
  </div>
</template>
