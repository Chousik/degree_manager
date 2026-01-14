<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const router = useRouter();
const route = useRoute();
const { isLoggedIn } = useSession();
const profile = ref(null);
const notifications = ref(null);
const loading = ref(false);
const error = ref('');
const notificationError = ref('');
const updatingNotification = ref('');
const rentals = ref([]);
const rentalsLoading = ref(false);
const rentalsError = ref('');
const rentalsTab = ref('pending');

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

async function loadRentals() {
  if (!isLoggedIn.value || !profile.value?.id) {
    rentals.value = [];
    return;
  }
  rentalsLoading.value = true;
  rentalsError.value = '';
  try {
    const data = await fetchJson(`${USER_API_BASE}/rentals/user?userId=${profile.value.id}`);
    rentals.value = Array.isArray(data) ? data : [];
  } catch (err) {
    rentalsError.value = 'Не удалось загрузить аренды.';
    rentals.value = [];
  } finally {
    rentalsLoading.value = false;
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

watch(
  () => profile.value?.id,
  () => {
    loadRentals();
  }
);

watch(
  () => route.query.tab,
  (value) => {
    if (value === 'pending' || value === 'active' || value === 'completed') {
      rentalsTab.value = value;
    }
  },
  { immediate: true }
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

const filteredRentals = computed(() => {
  const tab = rentalsTab.value;
  return rentals.value.filter((rental) => {
    if (tab === 'pending') return rental.status === 'PENDING';
    if (tab === 'active') return rental.status === 'ACTIVE';
    if (tab === 'completed') return rental.status === 'COMPLETED';
    return false;
  });
});

const rentalStatusLabels = {
  PENDING: 'Ожидает подтверждения',
  ACTIVE: 'Подтверждена',
  COMPLETED: 'Завершена',
  CANCELLED: 'Отменена',
};
const paymentStatusLabels = {
  succeeded: 'Оплачено',
  pending: 'Ожидает оплаты',
  canceled: 'Отменено',
  refunded: 'Возвращено',
};

const formatDate = (value) => {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'short' });
};

const confirmRental = async (rentalId) => {
  if (!profile.value?.id) return;
  try {
    await fetchJson(`${USER_API_BASE}/rentals/${rentalId}/confirm`, {
      method: 'POST',
      body: JSON.stringify({ actorId: profile.value.id }),
    });
    await loadRentals();
  } catch (err) {
    rentalsError.value = err?.message || 'Не удалось подтвердить аренду.';
  }
};

const payDeposit = async (rentalId) => {
  if (!profile.value?.id) return;
  try {
    const response = await fetchJson(`${USER_API_BASE}/payments/rentals/${rentalId}/initiate`, {
      method: 'POST',
      body: JSON.stringify({ actorId: profile.value.id, purpose: 'DEPOSIT' }),
    });
    if (response?.confirmationUrl) {
      window.location.href = response.confirmationUrl;
    }
  } catch (err) {
    rentalsError.value = err?.message || 'Не удалось оплатить депозит.';
  }
};

const payRental = async (rentalId) => {
  if (!profile.value?.id) return;
  try {
    const response = await fetchJson(`${USER_API_BASE}/payments/rentals/${rentalId}/initiate`, {
      method: 'POST',
      body: JSON.stringify({ actorId: profile.value.id, purpose: 'RENTAL' }),
    });
    if (response?.confirmationUrl) {
      window.location.href = response.confirmationUrl;
    }
  } catch (err) {
    rentalsError.value = err?.message || 'Не удалось оплатить аренду.';
  }
};
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

    <section class="dashboard-section">
      <h2>Мои аренды</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите, чтобы увидеть аренды.</p>
      <p v-else-if="rentalsLoading" class="dashboard-note">Загружаем аренды...</p>
      <p v-else-if="rentalsError" class="dashboard-note error">{{ rentalsError }}</p>
      <div v-else class="rentals-panel">
        <div class="rentals-tabs">
          <button
            type="button"
            class="rentals-tab"
            :class="{ active: rentalsTab === 'pending' }"
            @click="rentalsTab = 'pending'"
          >
            Не подтвержденные
          </button>
          <button
            type="button"
            class="rentals-tab"
            :class="{ active: rentalsTab === 'active' }"
            @click="rentalsTab = 'active'"
          >
            Подтвержденные
          </button>
          <button
            type="button"
            class="rentals-tab"
            :class="{ active: rentalsTab === 'completed' }"
            @click="rentalsTab = 'completed'"
          >
            Завершенные
          </button>
        </div>
        <div v-if="filteredRentals.length" class="rentals-list">
          <article v-for="rental in filteredRentals" :key="rental.rentalId" class="rental-card">
            <div class="rental-card__head">
              <div>
                <div class="rental-card__title">{{ rental.listingTitle || 'Объявление' }}</div>
                <div class="rental-card__meta">
                  {{ rental.role === 'LESSOR' ? 'Вы сдаете' : 'Вы арендуете' }}
                </div>
              </div>
              <span class="chip">{{ rentalStatusLabels[rental.status] || rental.status }}</span>
            </div>
            <div class="rental-card__body">
              <div>
                {{ rental.role === 'LESSOR' ? 'Арендатор' : 'Владелец' }}:
                {{ rental.counterpartyName }}
                <span v-if="rental.counterpartyUsername">(@{{ rental.counterpartyUsername }})</span>
              </div>
              <div>Период: {{ formatDate(rental.startAt) }} — {{ formatDate(rental.endAt) }}</div>
              <div>Цена аренды: {{ rental.totalAmount ? `${rental.totalAmount} ₽` : '—' }}</div>
              <div>Залог: {{ rental.depositAmount ? `${rental.depositAmount} ₽` : '—' }}</div>
              <div>Статус аренды: {{ paymentStatusLabels[rental.rentalStatus] || rental.rentalStatus || '—' }}</div>
              <div>Статус залога: {{ paymentStatusLabels[rental.depositStatus] || rental.depositStatus || '—' }}</div>
              <div>Создано: {{ formatDate(rental.createdAt) }}</div>
            </div>
            <div class="rental-card__actions">
              <button
                v-if="rental.status === 'PENDING' && rental.role === 'LESSOR'"
                type="button"
                class="btn secondary"
                @click="confirmRental(rental.rentalId)"
              >
                Подтвердить
              </button>
              <button
                v-if="rental.role === 'LESSEE' && rental.status === 'ACTIVE' && rental.totalAmount && rental.rentalStatus !== 'succeeded'"
                type="button"
                class="btn secondary"
                @click="payRental(rental.rentalId)"
              >
                Оплатить аренду
              </button>
              <button
                v-if="rental.role === 'LESSEE' && rental.status === 'ACTIVE' && rental.depositAmount && rental.depositStatus !== 'succeeded'"
                type="button"
                class="btn secondary"
                @click="payDeposit(rental.rentalId)"
              >
                Оплатить залог
              </button>
            </div>
          </article>
        </div>
        <p v-else class="dashboard-note">В этом разделе пока пусто.</p>
      </div>
    </section>
  </div>
</template>
