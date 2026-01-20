<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';
import { getConversationMessages, getConversations, sendConversationMessage } from '../api/conversations';

const router = useRouter();
const route = useRoute();
const { isLoggedIn, accessToken, logout } = useSession();
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
const reviewDrafts = ref({});
const reviewErrors = ref({});
const reviewSuccess = ref({});
const reportDrafts = ref({});
const reportErrors = ref({});
const chatSectionRef = ref(null);
const conversations = ref([]);
const conversationsLoading = ref(false);
const conversationsError = ref('');
const activeConversationId = ref(route.query.conversation || '');
const conversationMessages = ref([]);
const messagesLoading = ref(false);
const messageDraft = ref('');
const messageError = ref('');

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

async function loadConversations() {
  if (!isLoggedIn.value || !profile.value?.id) {
    conversations.value = [];
    return;
  }
  conversationsLoading.value = true;
  conversationsError.value = '';
  try {
    const data = await getConversations(profile.value.id);
    conversations.value = Array.isArray(data) ? data : [];
  } catch (err) {
    conversationsError.value = 'Не удалось загрузить чаты.';
    conversations.value = [];
  } finally {
    conversationsLoading.value = false;
  }
}

async function loadMessages(conversationId) {
  if (!conversationId || !profile.value?.id) {
    conversationMessages.value = [];
    return;
  }
  messagesLoading.value = true;
  messageError.value = '';
  try {
    const data = await getConversationMessages(conversationId, profile.value.id);
    conversationMessages.value = Array.isArray(data) ? data : [];
  } catch (err) {
    messageError.value = err?.message || 'Не удалось загрузить сообщения.';
    conversationMessages.value = [];
  } finally {
    messagesLoading.value = false;
  }
}

async function openConversation(conversationId) {
  if (!conversationId) return;
  activeConversationId.value = conversationId;
  await loadMessages(conversationId);
  router.replace({
    query: { ...route.query, tab: 'chats', conversation: conversationId },
  });
}

async function sendChatMessage() {
  if (!activeConversationId.value || !profile.value?.id) return;
  if (!messageDraft.value.trim()) {
    messageError.value = 'Введите сообщение.';
    return;
  }
  messageError.value = '';
  try {
    await sendConversationMessage(activeConversationId.value, {
      senderId: profile.value.id,
      body: messageDraft.value.trim(),
    });
    messageDraft.value = '';
    await loadMessages(activeConversationId.value);
    await loadConversations();
  } catch (err) {
    messageError.value = err?.message || 'Не удалось отправить сообщение.';
  }
}

const logoutAndGoHome = () => {
  logout();
  router.push('/login');
};

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
    loadConversations();
  }
);

watch(
  () => route.query.tab,
  (value) => {
    if (value === 'pending' || value === 'active' || value === 'completed') {
      rentalsTab.value = value;
    }
    if (value === 'chats') {
      nextTick(() => {
        chatSectionRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    }
  },
  { immediate: true }
);

watch(
  () => route.query.conversation,
  (value) => {
    if (value) {
      activeConversationId.value = value;
      loadMessages(value);
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
    if (tab === 'active') return rental.status === 'ACTIVE' || rental.status === 'COMPLETION_PENDING';
    if (tab === 'completed') return rental.status === 'COMPLETED';
    return false;
  });
});

const rentalStatusLabels = {
  PENDING: 'Ожидает подтверждения',
  ACTIVE: 'Подтверждена',
  COMPLETION_PENDING: 'Ожидает завершения',
  COMPLETED: 'Завершена',
  CANCELLED: 'Отменена',
};
const paymentStatusLabels = {
  succeeded: 'Оплачено',
  pending: 'Ожидает оплаты',
  canceled: 'Отменено',
  refunded: 'Возвращено',
};

const reportReasons = [
  'Мошенничество',
  'Не соблюдены условия',
  'Спор по оплате',
  'Оскорбления/спам',
  'Другое',
];

const ensureReportDraft = (rentalId) => {
  if (!reportDrafts.value[rentalId]) {
    reportDrafts.value = {
      ...reportDrafts.value,
      [rentalId]: { reason: '', note: '', open: false },
    };
  }
  return reportDrafts.value[rentalId];
};

const toggleReportForm = (rentalId) => {
  const draft = ensureReportDraft(rentalId);
  reportDrafts.value = {
    ...reportDrafts.value,
    [rentalId]: { ...draft, open: !draft.open },
  };
};

const submitReport = async (rentalId) => {
  if (!profile.value?.id) return;
  const draft = ensureReportDraft(rentalId);
  reportErrors.value = { ...reportErrors.value, [rentalId]: '' };
  if (!draft.reason) {
    reportErrors.value = { ...reportErrors.value, [rentalId]: 'Выберите причину' };
    return;
  }
  const reason = draft.note ? `${draft.reason}: ${draft.note}` : draft.reason;
  try {
    await fetchJson(`${USER_API_BASE}/moderation/rentals/${rentalId}/flag`, {
      method: 'POST',
      body: JSON.stringify({ reporterId: profile.value.id, reason }),
    });
    reportDrafts.value = {
      ...reportDrafts.value,
      [rentalId]: { reason: '', note: '', open: false },
    };
  } catch (err) {
    reportErrors.value = { ...reportErrors.value, [rentalId]: err?.message || 'Не удалось отправить жалобу.' };
  }
};

const ensureReviewDraft = (rentalId) => {
  if (!reviewDrafts.value[rentalId]) {
    reviewDrafts.value = {
      ...reviewDrafts.value,
      [rentalId]: { rating: 5, text: '', open: false },
    };
  }
  return reviewDrafts.value[rentalId];
};

const toggleReviewForm = (rentalId) => {
  const draft = ensureReviewDraft(rentalId);
  reviewDrafts.value = {
    ...reviewDrafts.value,
    [rentalId]: { ...draft, open: !draft.open },
  };
};

const submitReview = async (rentalId) => {
  if (!profile.value?.id) return;
  const draft = ensureReviewDraft(rentalId);
  reviewErrors.value = { ...reviewErrors.value, [rentalId]: '' };
  reviewSuccess.value = { ...reviewSuccess.value, [rentalId]: '' };
  try {
    await fetchJson(`${USER_API_BASE}/rentals/${rentalId}/reviews`, {
      method: 'POST',
      body: JSON.stringify({
        authorId: profile.value.id,
        rating: Number(draft.rating),
        text: draft.text || '',
      }),
    });
    reviewSuccess.value = { ...reviewSuccess.value, [rentalId]: 'Отзыв отправлен' };
    reviewDrafts.value = {
      ...reviewDrafts.value,
      [rentalId]: { ...draft, text: '', open: false },
    };
  } catch (err) {
    reviewErrors.value = { ...reviewErrors.value, [rentalId]: err?.message || 'Не удалось отправить отзыв.' };
  }
};

const formatDate = (value) => {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'short' });
};

const formatTime = (value) => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'short' });
};

const activeConversation = computed(() =>
  conversations.value.find((conversation) => conversation.conversationId === activeConversationId.value)
);

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

const requestCompletion = async (rentalId) => {
  if (!profile.value?.id) return;
  try {
    await fetchJson(`${USER_API_BASE}/rentals/${rentalId}/complete`, {
      method: 'POST',
      body: JSON.stringify({ actorId: profile.value.id }),
    });
    await loadRentals();
  } catch (err) {
    rentalsError.value = err?.message || 'Не удалось завершить аренду.';
  }
};

const cancelRental = async (rentalId) => {
  if (!profile.value?.id) return;
  try {
    await fetchJson(`${USER_API_BASE}/rentals/${rentalId}/cancel`, {
      method: 'POST',
      body: JSON.stringify({ actorId: profile.value.id }),
    });
    await loadRentals();
  } catch (err) {
    rentalsError.value = err?.message || 'Не удалось отменить аренду.';
  }
};

const supportForm = reactive({
  subject: '',
  message: '',
  rentalId: '',
});
const supportSending = ref(false);
const supportError = ref('');
const supportSuccess = ref('');

const submitSupport = async () => {
  if (!profile.value?.id) return;
  if (!supportForm.subject.trim() || !supportForm.message.trim()) {
    supportError.value = 'Заполните тему и сообщение';
    return;
  }
  supportSending.value = true;
  supportError.value = '';
  supportSuccess.value = '';
  try {
    await fetchJson(`${USER_API_BASE}/support/tickets`, {
      method: 'POST',
      body: JSON.stringify({
        requesterId: profile.value.id,
        rentalId: supportForm.rentalId || null,
        subject: supportForm.subject.trim(),
        message: supportForm.message.trim(),
      }),
    });
    supportSuccess.value = 'Обращение отправлено';
    supportForm.subject = '';
    supportForm.message = '';
    supportForm.rentalId = '';
  } catch (err) {
    supportError.value = err?.message || 'Не удалось отправить обращение.';
  } finally {
    supportSending.value = false;
  }
};

const downloadContract = async (rentalId) => {
  if (!profile.value?.id || !accessToken.value) return;
  try {
    const response = await fetch(
      `${USER_API_BASE}/contracts/rentals/${rentalId}/file?userId=${profile.value.id}`,
      {
        headers: { Authorization: `Bearer ${accessToken.value}` },
      },
    );
    if (!response.ok) {
      throw new Error('Не удалось получить договор');
    }
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `contract-${rentalId}.txt`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  } catch (err) {
    rentalsError.value = err?.message || 'Не удалось скачать договор.';
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
                <button type="button" class="landing-btn logout" @click="logoutAndGoHome">Выйти</button>
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

    <section ref="chatSectionRef" class="dashboard-section">
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
                v-if="rental.status === 'ACTIVE'"
                type="button"
                class="btn secondary"
                @click="requestCompletion(rental.rentalId)"
              >
                Запросить завершение
              </button>
              <button
                v-if="rental.status === 'COMPLETION_PENDING' && rental.completionRequestedBy !== profile?.id"
                type="button"
                class="btn secondary"
                @click="requestCompletion(rental.rentalId)"
              >
                Подтвердить завершение
              </button>
              <button
                v-if="rental.status === 'COMPLETION_PENDING' && rental.completionRequestedBy === profile?.id"
                type="button"
                class="btn secondary"
                disabled
              >
                Ожидаем подтверждения
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
              <button
                v-if="rental.status === 'PENDING' || rental.status === 'ACTIVE' || rental.status === 'COMPLETION_PENDING'"
                type="button"
                class="btn secondary"
                @click="cancelRental(rental.rentalId)"
              >
                Отменить
              </button>
              <button
                v-if="rental.status === 'COMPLETED' || rental.status === 'CANCELLED'"
                type="button"
                class="btn secondary"
                @click="toggleReviewForm(rental.rentalId)"
              >
                Оставить отзыв
              </button>
              <button
                v-if="rental.status !== 'COMPLETED'"
                type="button"
                class="btn secondary"
                @click="toggleReportForm(rental.rentalId)"
              >
                Пожаловаться на сделку
              </button>
              <button
                v-if="rental.status === 'ACTIVE' || rental.status === 'COMPLETION_PENDING' || rental.status === 'COMPLETED'"
                type="button"
                class="btn secondary"
                @click="downloadContract(rental.rentalId)"
              >
                Скачать договор
              </button>
            </div>
            <div v-if="ensureReportDraft(rental.rentalId).open" class="report-form">
              <label>
                Причина
                <select v-model="ensureReportDraft(rental.rentalId).reason">
                  <option value="" disabled>Выберите причину</option>
                  <option v-for="reason in reportReasons" :key="reason" :value="reason">{{ reason }}</option>
                </select>
              </label>
              <label>
                Детали (необязательно)
                <textarea v-model="ensureReportDraft(rental.rentalId).note" rows="3" placeholder="Опишите проблему"></textarea>
              </label>
              <button type="button" class="btn secondary" @click="submitReport(rental.rentalId)">
                Отправить жалобу
              </button>
              <p v-if="reportErrors[rental.rentalId]" class="dashboard-note error">
                {{ reportErrors[rental.rentalId] }}
              </p>
            </div>
            <div v-if="reviewSuccess[rental.rentalId]" class="dashboard-note success">
              {{ reviewSuccess[rental.rentalId] }}
            </div>
            <div v-if="ensureReviewDraft(rental.rentalId).open" class="review-form">
              <label>
                Оценка
                <select v-model="ensureReviewDraft(rental.rentalId).rating">
                  <option v-for="star in 5" :key="star" :value="star">{{ star }}</option>
                </select>
              </label>
              <label>
                Отзыв
                <textarea v-model="ensureReviewDraft(rental.rentalId).text" rows="3" placeholder="Напишите кратко..."></textarea>
              </label>
              <button type="button" class="btn secondary" @click="submitReview(rental.rentalId)">
                Отправить отзыв
              </button>
              <p v-if="reviewErrors[rental.rentalId]" class="dashboard-note error">
                {{ reviewErrors[rental.rentalId] }}
              </p>
            </div>
          </article>
        </div>
        <p v-else class="dashboard-note">В этом разделе пока пусто.</p>
      </div>
    </section>

    <section class="dashboard-section">
      <h2>Чаты</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите, чтобы увидеть переписки.</p>
      <p v-else-if="conversationsLoading" class="dashboard-note">Загружаем переписки...</p>
      <p v-else-if="conversationsError" class="dashboard-note error">{{ conversationsError }}</p>
      <div v-else class="chat-grid">
        <div class="chat-list">
          <button
            v-for="conversation in conversations"
            :key="conversation.conversationId"
            type="button"
            class="chat-list__item"
            :class="{ active: conversation.conversationId === activeConversationId }"
            @click="openConversation(conversation.conversationId)"
          >
            <div class="chat-list__thumb">
              <img v-if="conversation.listingPhotoUrl" :src="conversation.listingPhotoUrl" alt="" />
              <span v-else>📷</span>
            </div>
            <div class="chat-list__body">
              <div class="chat-list__title">{{ conversation.listingTitle || 'Объявление' }}</div>
              <div class="chat-list__meta">
                {{ conversation.counterpartyName || 'Пользователь' }}
                <span v-if="conversation.counterpartyUsername">(@{{ conversation.counterpartyUsername }})</span>
              </div>
              <div class="chat-list__preview">
                {{ conversation.lastMessagePreview || 'Нет сообщений' }}
              </div>
            </div>
            <div class="chat-list__time">{{ formatTime(conversation.lastMessageAt) }}</div>
          </button>
          <p v-if="!conversations.length" class="dashboard-note">Пока нет переписок.</p>
        </div>

        <div class="chat-panel">
          <div v-if="!activeConversationId" class="chat-empty">
            Выберите чат слева, чтобы открыть переписку.
          </div>
          <template v-else>
            <div class="chat-panel__head">
              <div>
                <div class="chat-panel__title">{{ activeConversation?.listingTitle || 'Объявление' }}</div>
                <div class="chat-panel__subtitle">
                  {{ activeConversation?.counterpartyName || 'Пользователь' }}
                  <span v-if="activeConversation?.counterpartyUsername">(@{{ activeConversation.counterpartyUsername }})</span>
                </div>
              </div>
            </div>
            <div v-if="messagesLoading" class="dashboard-note">Загружаем сообщения...</div>
            <p v-else-if="messageError" class="dashboard-note error">{{ messageError }}</p>
            <div v-else class="chat-messages">
              <div
                v-for="message in conversationMessages"
                :key="message.id"
                class="chat-message"
                :class="{ outgoing: message.senderId === profile?.id }"
              >
                <div class="chat-message__bubble">{{ message.body }}</div>
                <div class="chat-message__time">{{ formatTime(message.sentAt) }}</div>
              </div>
              <div v-if="!conversationMessages.length" class="chat-empty">
                Сообщений пока нет.
              </div>
            </div>
            <div class="chat-input">
              <textarea v-model="messageDraft" rows="2" placeholder="Введите сообщение"></textarea>
              <button type="button" class="btn secondary" @click="sendChatMessage">Отправить</button>
            </div>
          </template>
        </div>
      </div>
    </section>

    <section class="dashboard-section">
      <h2>Поддержка</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите, чтобы написать в поддержку.</p>
      <form v-else class="support-form" @submit.prevent="submitSupport">
        <label>
          Тема
          <input v-model="supportForm.subject" type="text" placeholder="Кратко опишите проблему" />
        </label>
        <label>
          Связать с арендой (необязательно)
          <select v-model="supportForm.rentalId">
            <option value="">Без аренды</option>
            <option v-for="rental in rentals" :key="rental.rentalId" :value="rental.rentalId">
              {{ rental.listingTitle || 'Объявление' }} · {{ formatDate(rental.startAt) }}
            </option>
          </select>
        </label>
        <label>
          Сообщение
          <textarea v-model="supportForm.message" rows="4" placeholder="Опишите ситуацию"></textarea>
        </label>
        <button type="submit" class="btn secondary" :disabled="supportSending">
          {{ supportSending ? 'Отправляем...' : 'Отправить в поддержку' }}
        </button>
        <p v-if="supportError" class="dashboard-note error">{{ supportError }}</p>
        <p v-if="supportSuccess" class="dashboard-note success">{{ supportSuccess }}</p>
      </form>
    </section>
  </div>
</template>
