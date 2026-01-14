<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import {
  ADMIN_API_BASE,
  OAUTH_BASE,
  OAUTH_CLIENT_ID,
  OAUTH_CLIENT_SECRET,
  OAUTH_REDIRECT,
  OAUTH_SCOPE,
  fetchJson,
} from './api/client';
import { useSession } from './state/session';

const session = useSession();
const search = ref('');
const activeSection = ref('moderation');
const loading = ref(false);
const error = ref('');
const authStatus = ref('');
const moderationTab = ref('open');
const rentalTab = ref('open');
const ticketTab = ref('open');

const loginForm = reactive({ username: '', password: '', otp: '' });
const toast = reactive({ message: '', type: 'success', visible: false });

const tickets = ref([]);
const reports = ref([]);
const bans = ref([]);
const listingDetail = ref(null);
const reviewDetail = ref(null);
const detailReportReason = ref('');
const detailReportId = ref('');
const detailReportStatus = ref('');
const detailReportModeratorComment = ref('');
const detailTargetId = ref('');
const detailTargetType = ref('');
const detailError = ref('');
const detailLoading = ref(false);
const detailView = ref(null);
const adminIdOverride = ref(localStorage.getItem('fx_admin_id_override') || '');
const archiveModal = reactive({
  open: false,
  listingId: '',
  reportId: '',
  reason: '',
});
const resolveModal = reactive({
  open: false,
  reportId: '',
  targetId: '',
  targetType: '',
  action: '',
  comment: '',
});
const ticketResolveModal = reactive({
  open: false,
  ticketId: '',
  comment: '',
});

const banForm = reactive({
  bannedUserId: '',
  banReason: '',
  banType: 'TEMP',
  banDuration: '',
});

const isAdminReady = computed(() => session.isLoggedIn.value && session.isAdmin.value);
const openTickets = computed(() => tickets.value.filter((t) => t.status === 'OPEN').length);
const needsAdminId = computed(() => !isUuid(session.userId.value));
const effectiveAdminId = computed(() => {
  return isUuid(session.userId.value) ? session.userId.value : adminIdOverride.value;
});

const authUrl = computed(() => {
  const redirect = encodeURIComponent(OAUTH_REDIRECT);
  const scope = encodeURIComponent(OAUTH_SCOPE);
  const clientId = encodeURIComponent(OAUTH_CLIENT_ID);
  return `${OAUTH_BASE}/oauth2/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirect}&scope=${scope}`;
});

const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};

const isUuid = (value) => {
  if (!value) return false;
  return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/.test(
    value
  );
};

const requireAdminId = () => {
  const adminId = effectiveAdminId.value;
  if (!isUuid(adminId)) {
    showToast('Укажите UUID администратора', 'error');
    return null;
  }
  return adminId;
};

const saveAdminIdOverride = () => {
  const trimmed = adminIdOverride.value.trim();
  adminIdOverride.value = trimmed;
  if (trimmed) {
    localStorage.setItem('fx_admin_id_override', trimmed);
  } else {
    localStorage.removeItem('fx_admin_id_override');
  }
};

const goToSection = (sectionId) => {
  activeSection.value = sectionId;
  listingDetail.value = null;
  reviewDetail.value = null;
  detailError.value = '';
  detailView.value = null;
};

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    showToast('Введите логин и пароль', 'error');
    return;
  }

  try {
    const formData = new FormData();
    formData.append('username', loginForm.username.trim());
    formData.append('password', loginForm.password);
    formData.append('otp', loginForm.otp || '');

    const res = await fetch(`${OAUTH_BASE}/login`, {
      method: 'POST',
      credentials: 'include',
      headers: { Accept: 'application/json' },
      body: formData,
    });

    if (!res.ok) {
      throw new Error('Неверный логин или пароль');
    }

    showToast('Пароль верен. Перенаправляем для получения токена...', 'success');
    window.location.href = authUrl.value;
  } catch (err) {
    showToast(err.message || 'Ошибка при входе', 'error');
  }
};

const exchangeCode = async (code) => {
  authStatus.value = 'Обмениваю код на токен...';
  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    code,
    redirect_uri: OAUTH_REDIRECT,
    client_id: OAUTH_CLIENT_ID,
  });
  const res = await fetch(`${OAUTH_BASE}/oauth2/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      Authorization: 'Basic ' + btoa(`${OAUTH_CLIENT_ID}:${OAUTH_CLIENT_SECRET}`),
    },
    body,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'Не удалось получить токен');
  }
  const data = await res.json();
  session.setTokens(data.access_token, data.refresh_token);
  authStatus.value = 'Токен получен.';
};

const ensureAdminAccess = () => {
  if (!session.isAdmin.value) {
    session.logout();
    error.value = 'Нужна роль администратора для входа.';
    return false;
  }
  return true;
};

const formatDate = (value) => {
  if (!value) return '';
  try {
    return new Date(value).toLocaleString('ru-RU', {
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch (e) {
    return value;
  }
};

const mapTicket = (item) => ({
  id: item.id,
  title: item.subject,
  message: item.message,
  requester: item.requesterId ? `User ${item.requesterId}` : 'Пользователь',
  requesterId: item.requesterId,
  rentalId: item.rentalId,
  createdAt: formatDate(item.createdAt),
  resolvedAt: formatDate(item.resolvedAt),
  resolutionNotes: item.resolutionNotes,
  status: item.status,
});

const loadQueues = async () => {
  loading.value = true;
  error.value = '';
  try {
    const [ticketData, reportData, banData] = await Promise.all([
      fetchJson(`${ADMIN_API_BASE}/support/tickets`),
      fetchJson(`${ADMIN_API_BASE}/reports`),
      fetchJson(`${ADMIN_API_BASE}/bans`),
    ]);

    tickets.value = ticketData.map(mapTicket);

    reports.value = reportData.map((item) => ({
      id: item.id,
      reporterId: item.reporterId,
      status: item.status,
      targetType: item.targetType,
      targetId: item.targetId,
      reasonBody: item.reasonBody,
      moderatorComment: item.moderatorComment,
      createdAt: formatDate(item.createdAt),
      resolvedById: item.resolvedById,
    }));

    bans.value = banData.map((item) => ({
      id: item.id,
      bannedUserId: item.bannedUserId,
      adminUserId: item.adminUserId,
      banReason: item.banReason,
      banType: item.banType,
      banDuration: formatDate(item.banDuration),
      status: item.status,
      createdAt: formatDate(item.createdAt),
    }));
  } catch (err) {
    error.value = err.message || 'Не удалось загрузить данные';
  } finally {
    loading.value = false;
  }
};


const openListingDetail = async (report) => {
  detailError.value = '';
  detailLoading.value = true;
  reviewDetail.value = null;
  detailView.value = 'listing';
  detailReportReason.value = report?.reasonBody || '';
  detailReportId.value = report?.id || '';
  detailReportStatus.value = report?.status || '';
  detailReportModeratorComment.value = report?.moderatorComment || '';
  detailTargetId.value = report?.targetId || '';
  detailTargetType.value = report?.targetType || '';
  try {
    listingDetail.value = await fetchJson(`${ADMIN_API_BASE}/listings/${report.targetId}`);
  } catch (err) {
    detailError.value = err.message || 'Не удалось загрузить объявление';
  } finally {
    detailLoading.value = false;
  }
};

const openReviewDetail = async (report) => {
  detailError.value = '';
  detailLoading.value = true;
  listingDetail.value = null;
  detailView.value = 'review';
  detailReportReason.value = report?.reasonBody || '';
  detailReportId.value = report?.id || '';
  detailReportStatus.value = report?.status || '';
  detailReportModeratorComment.value = report?.moderatorComment || '';
  detailTargetId.value = report?.targetId || '';
  detailTargetType.value = report?.targetType || '';
  try {
    reviewDetail.value = await fetchJson(`${ADMIN_API_BASE}/reviews/${report.targetId}`);
  } catch (err) {
    detailError.value = err.message || 'Не удалось загрузить отзыв';
  } finally {
    detailLoading.value = false;
  }
};

const closeDetail = () => {
  listingDetail.value = null;
  reviewDetail.value = null;
  detailError.value = '';
  detailView.value = null;
  detailReportReason.value = '';
  detailReportId.value = '';
  detailReportStatus.value = '';
  detailReportModeratorComment.value = '';
  detailTargetId.value = '';
  detailTargetType.value = '';
};

const openResolveModal = (report, action = 'resolve') => {
  resolveModal.open = true;
  resolveModal.reportId = report?.id || detailReportId.value || '';
  resolveModal.targetId = report?.targetId || detailTargetId.value || '';
  resolveModal.targetType = report?.targetType || detailTargetType.value || '';
  resolveModal.action = action;
  resolveModal.comment = '';
};

const closeResolveModal = () => {
  resolveModal.open = false;
  resolveModal.reportId = '';
  resolveModal.targetId = '';
  resolveModal.targetType = '';
  resolveModal.action = '';
  resolveModal.comment = '';
};

const resolveModalTitle = computed(() => {
  if (resolveModal.action === 'hide_review') {
    return 'Скрыть отзыв';
  }
  return 'Комментарий модератора';
});

const resolveModalSubmitLabel = computed(() => {
  if (resolveModal.action === 'hide_review') {
    return 'Скрыть отзыв';
  }
  if (resolveModal.targetType === 'RENTAL') {
    return 'Закрыть';
  }
  return 'Снять флаг';
});

const openArchiveModal = (listingId, reportId, presetReason = '') => {
  archiveModal.open = true;
  archiveModal.listingId = listingId || '';
  archiveModal.reportId = reportId || '';
  archiveModal.reason = presetReason || '';
};

const closeArchiveModal = () => {
  archiveModal.open = false;
  archiveModal.listingId = '';
  archiveModal.reportId = '';
  archiveModal.reason = '';
};

const archiveListing = async () => {
  const adminId = requireAdminId();
  if (!adminId || !archiveModal.listingId) {
    return;
  }
  const reason = archiveModal.reason.trim() || null;
  await fetchJson(`${ADMIN_API_BASE}/listings/${archiveModal.listingId}/archive`, {
    method: 'POST',
    body: JSON.stringify({
      adminId,
      comment: reason,
    }),
  });
  if (archiveModal.reportId) {
    await resolveReport(archiveModal.reportId, reason);
  }
  if (listingDetail.value?.id === archiveModal.listingId) {
    closeDetail();
  }
  closeArchiveModal();
  await loadQueues();
};

const hideReview = async (reviewId, reportId, comment) => {
  const adminId = requireAdminId();
  if (!adminId || !reviewId) {
    return;
  }
  await fetchJson(`${ADMIN_API_BASE}/reviews/${reviewId}/hide`, {
    method: 'POST',
    body: JSON.stringify({
      adminId,
      reportId: reportId || null,
      comment: comment || null,
    }),
  });
  if (reportId) {
    await resolveReport(reportId, comment || null);
  }
  await loadQueues();
};

const submitResolveModal = async () => {
  if (!resolveModal.reportId) {
    showToast('Не найден reportId', 'error');
    return;
  }
  if (resolveModal.action === 'hide_review') {
    await hideReview(resolveModal.targetId, resolveModal.reportId, resolveModal.comment);
  } else {
    await resolveReport(resolveModal.reportId, resolveModal.comment);
  }
  closeResolveModal();
};

const openTicketResolveModal = (ticketId) => {
  ticketResolveModal.open = true;
  ticketResolveModal.ticketId = ticketId || '';
  ticketResolveModal.comment = '';
};

const closeTicketResolveModal = () => {
  ticketResolveModal.open = false;
  ticketResolveModal.ticketId = '';
  ticketResolveModal.comment = '';
};

const startTicket = async (id) => {
  const adminId = requireAdminId();
  if (!adminId) {
    return;
  }
  const response = await fetchJson(`${ADMIN_API_BASE}/support/tickets/${id}/start`, {
    method: 'POST',
    body: JSON.stringify({ adminId }),
  });
  tickets.value = tickets.value.map((ticket) => (ticket.id === id ? mapTicket(response) : ticket));
  ticketTab.value = 'progress';
  await loadQueues();
};

const resolveTicket = async (id) => {
  const adminId = requireAdminId();
  if (!adminId) {
    return;
  }
  if (!ticketResolveModal.comment.trim()) {
    showToast('Нужен комментарий для закрытия тикета', 'error');
    return;
  }
  const response = await fetchJson(`${ADMIN_API_BASE}/support/tickets/${id}/resolve`, {
    method: 'POST',
    body: JSON.stringify({
      adminId,
      resolutionNotes: ticketResolveModal.comment.trim(),
    }),
  });
  tickets.value = tickets.value.map((ticket) => (ticket.id === id ? mapTicket(response) : ticket));
  ticketTab.value = 'resolved';
  closeTicketResolveModal();
  await loadQueues();
};

const resolveReport = async (id, comment) => {
  const adminId = requireAdminId();
  if (!adminId) {
    return;
  }
  const note = comment ? comment.trim() : null;
  const response = await fetchJson(`${ADMIN_API_BASE}/reports/${id}/resolve`, {
    method: 'POST',
    body: JSON.stringify({
      adminId,
      status: 'RESOLVED',
      comment: note || 'Resolved in admin panel',
    }),
  });
  reports.value = reports.value.map((report) => (report.id === id ? response : report));
  if (activeSection.value === 'reports') {
    rentalTab.value = 'resolved';
  } else {
    moderationTab.value = 'resolved';
  }
  if (detailReportId.value && detailReportId.value === id) {
    closeDetail();
  }
  await loadQueues();
};

const createBan = async () => {
  const adminId = requireAdminId();
  if (!adminId) {
    return;
  }
  if (!banForm.bannedUserId.trim()) {
    showToast('Нужен ID пользователя', 'error');
    return;
  }
  if (!banForm.banType) {
    showToast('Выберите тип бана', 'error');
    return;
  }
  if (banForm.banType === 'TEMP' && !banForm.banDuration) {
    showToast('Укажите дату окончания для временного бана', 'error');
    return;
  }
  if (banForm.banType === 'TEMP' && banForm.banDuration) {
    const until = new Date(banForm.banDuration);
    if (Number.isNaN(until.getTime()) || until <= new Date()) {
      showToast('Дата окончания должна быть позже текущего времени', 'error');
      return;
    }
  }

  const duration =
    banForm.banType === 'TEMP' && banForm.banDuration
      ? new Date(banForm.banDuration).toISOString()
      : null;
  const payload = {
    adminId,
    bannedUserId: banForm.bannedUserId.trim(),
    banReason: banForm.banReason || null,
    banType: banForm.banType,
    banDuration: duration,
    status: 'ACTIVE',
  };

  const response = await fetchJson(`${ADMIN_API_BASE}/bans`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  bans.value = [
    {
      id: response.id,
      bannedUserId: response.bannedUserId,
      adminUserId: response.adminUserId,
      banReason: response.banReason,
      banType: response.banType,
      banDuration: formatDate(response.banDuration),
      status: response.status,
      createdAt: formatDate(response.createdAt),
    },
    ...bans.value,
  ];

  banForm.bannedUserId = '';
  banForm.banReason = '';
  banForm.banType = 'TEMP';
  banForm.banDuration = '';
  await loadQueues();
};

const handleBanTypeChange = () => {
  if (banForm.banType !== 'TEMP') {
    banForm.banDuration = '';
    return;
  }
  if (!banForm.banDuration) {
    const now = new Date();
    now.setDate(now.getDate() + 7);
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    banForm.banDuration = local.toISOString().slice(0, 16);
  }
};

const liftBan = async (id) => {
  const adminId = requireAdminId();
  if (!adminId) {
    return;
  }
  const response = await fetchJson(`${ADMIN_API_BASE}/bans/${id}/lift`, {
    method: 'POST',
    body: JSON.stringify({
      adminId,
      status: 'LIFTED',
      comment: 'Lifted in admin panel',
    }),
  });

  bans.value = bans.value.map((ban) =>
    ban.id === id
      ? {
          ...ban,
          status: response.status,
        }
      : ban
  );
  await loadQueues();
};

const openListingReports = computed(() =>
  reports.value.filter((item) => item.targetType === 'LISTING' && item.status === 'OPEN')
);
const resolvedListingReports = computed(() =>
  reports.value.filter((item) => item.targetType === 'LISTING' && item.status === 'RESOLVED')
);
const openReviewReports = computed(() =>
  reports.value.filter((item) => item.targetType === 'REVIEW' && item.status === 'OPEN')
);
const resolvedReviewReports = computed(() =>
  reports.value.filter((item) => item.targetType === 'REVIEW' && item.status === 'RESOLVED')
);
const rentalReports = computed(() =>
  reports.value.filter((item) => item.targetType === 'RENTAL')
);

const openRentalReports = computed(() =>
  rentalReports.value.filter((item) => item.status === 'OPEN')
);
const resolvedRentalReports = computed(() =>
  rentalReports.value.filter((item) => item.status === 'RESOLVED')
);

const openTicketsList = computed(() => tickets.value.filter((ticket) => ticket.status === 'OPEN'));
const inProgressTicketsList = computed(() => tickets.value.filter((ticket) => ticket.status === 'IN_PROGRESS'));
const resolvedTicketsList = computed(() => tickets.value.filter((ticket) => ticket.status === 'RESOLVED'));
const activeBans = computed(() => bans.value.filter((ban) => ban.status === 'ACTIVE'));

const currentListingReports = computed(() => {
  return moderationTab.value === 'resolved' ? resolvedListingReports.value : openListingReports.value;
});

const currentReviewReports = computed(() => {
  return moderationTab.value === 'resolved' ? resolvedReviewReports.value : openReviewReports.value;
});

const currentRentalReports = computed(() => {
  return rentalTab.value === 'resolved' ? resolvedRentalReports.value : openRentalReports.value;
});

const currentTickets = computed(() => {
  if (ticketTab.value === 'resolved') return resolvedTicketsList.value;
  if (ticketTab.value === 'progress') return inProgressTicketsList.value;
  return openTicketsList.value;
});

const filteredListings = computed(() =>
  currentListingReports.value.filter((item) =>
    (item.targetId || '').toLowerCase().includes(search.value.toLowerCase())
  )
);

const filteredReviews = computed(() =>
  currentReviewReports.value.filter((item) =>
    (item.targetId || '').toLowerCase().includes(search.value.toLowerCase())
  )
);

const queueCount = computed(() => openListingReports.value.length + openReviewReports.value.length);

onMounted(async () => {
  const { pathname, search: queryString } = window.location;
  if (pathname === '/auth-callback') {
    const params = new URLSearchParams(queryString);
    const code = params.get('code');
    if (!code) {
      authStatus.value = 'Нет кода авторизации.';
      return;
    }
    try {
      await exchangeCode(code);
      if (!ensureAdminAccess()) {
        return;
      }
      window.history.replaceState({}, '', '/');
    } catch (err) {
      authStatus.value = err.message || 'Ошибка при обмене кода';
      return;
    }
  }

  if (isAdminReady.value) {
    await loadQueues();
  }
});
</script>

<template>
  <div v-if="!isAdminReady" class="login-shell">
    <div class="login-card">
      <div class="brand">
        <div class="brand-icon">FX</div>
        <div class="brand-text">
          <h1>Fixly Admin</h1>
          <p>Вход для модераторов и поддержки</p>
        </div>
      </div>

      <form class="login-form" @submit.prevent="handleLogin">
        <label>
          Логин
          <input v-model="loginForm.username" type="text" autocomplete="username" required />
        </label>
        <label>
          Пароль
          <input v-model="loginForm.password" type="password" autocomplete="current-password" required />
        </label>
        <label>
          Код из Google Authenticator
          <input v-model="loginForm.otp" type="text" inputmode="numeric" autocomplete="one-time-code" placeholder="123456" />
        </label>
        <button class="btn primary" type="submit">Войти</button>
      </form>

      <p v-if="authStatus" class="helper">{{ authStatus }}</p>
      <p v-if="error" class="helper error">{{ error }}</p>
      <div v-if="toast.visible" class="toast" :class="toast.type">{{ toast.message }}</div>
    </div>
  </div>

  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-icon">FX</div>
        <div class="brand-text">
          <h1>Fixly Admin</h1>
          <p>Moderation and support desk</p>
        </div>
      </div>

      <nav class="nav">
        <button
          class="nav-item"
          :class="{ active: activeSection === 'moderation' }"
          type="button"
          @click="goToSection('moderation')"
        >
          <span>Модерация</span>
          <span class="count">{{ queueCount }}</span>
        </button>
        <button
          class="nav-item"
          :class="{ active: activeSection === 'tickets' }"
          type="button"
          @click="goToSection('tickets')"
        >
          <span>Тикеты</span>
          <span class="count">{{ openTickets }}</span>
        </button>
        <button
          class="nav-item"
          :class="{ active: activeSection === 'reports' }"
          type="button"
          @click="goToSection('reports')"
        >
          <span>Жалобы</span>
          <span class="count">{{ openRentalReports.length }}</span>
        </button>
        <button
          class="nav-item"
          :class="{ active: activeSection === 'bans' }"
          type="button"
          @click="goToSection('bans')"
        >
          <span>Баны</span>
          <span class="count">{{ activeBans.length }}</span>
        </button>
      </nav>
    </aside>

    <main class="main">
      <header class="topbar fade-in">
        <div class="search">
          <span>Search</span>
          <input v-model="search" type="text" placeholder="Listing ID, review, or ticket" />
        </div>
        <div class="top-actions">
          <div v-if="needsAdminId" class="admin-id">
            <input
              v-model="adminIdOverride"
              type="text"
              placeholder="UUID администратора"
              @change="saveAdminIdOverride"
            />
          </div>
          <button class="btn ghost" type="button" @click="session.logout()">Выйти</button>
        </div>
      </header>

      <section class="hero stagger">
        <div>
          <h2>Command desk</h2>
          <p>Очередь проверок, жалоб и обращений поддержки.</p>
        </div>
        <div class="stat">
          <div class="stat-value">{{ queueCount }}</div>
          <p>Ожидают модерации</p>
        </div>
        <div class="stat">
          <div class="stat-value">{{ openTickets }}</div>
          <p>Открытых тикетов</p>
        </div>
        <div class="stat">
          <div class="stat-value">{{ openRentalReports.length }}</div>
          <p>Жалоб на сделки</p>
        </div>
      </section>

      <section v-if="loading" class="panel">Загружаю данные...</section>
      <section v-else-if="error" class="panel">{{ error }}</section>

      <section v-else-if="detailView" class="panel">
        <div class="detail-header">
          <div>
            <h3 v-if="detailView === 'listing'">Детали объявления</h3>
            <h3 v-else>Детали отзыва</h3>
            <p class="muted">Проверьте описание, фото и контекст жалобы.</p>
          </div>
          <button class="btn ghost" type="button" @click="closeDetail">Назад</button>
        </div>

        <div v-if="detailLoading">Загрузка...</div>
        <div v-else-if="detailError" class="helper error">{{ detailError }}</div>

        <div v-else-if="detailView === 'listing' && listingDetail" class="detail-body">
          <div class="detail-main">
            <h4>{{ listingDetail.title }}</h4>
            <p class="muted">{{ listingDetail.description || 'Описание не заполнено' }}</p>
            <div class="detail-meta">
              <span>Причина жалобы: {{ detailReportReason || '—' }}</span>
              <span v-if="detailReportStatus === 'RESOLVED'">
                Комментарий модератора: {{ detailReportModeratorComment || '—' }}
              </span>
              <span>Цена/час: {{ listingDetail.pricePerHour }}</span>
              <span>Залог: {{ listingDetail.depositAmount || '—' }}</span>
              <span>Статус: {{ listingDetail.status }}</span>
              <span>Owner: {{ listingDetail.ownerId }}</span>
              <span>Создано: {{ listingDetail.createdAt }}</span>
            </div>
            <div v-if="detailReportStatus !== 'RESOLVED'" class="row-actions">
              <button
                v-if="detailReportId"
                class="approve"
                type="button"
                @click="openResolveModal({ id: detailReportId, targetId: detailTargetId, targetType: detailTargetType })"
              >
                Снять флаг
              </button>
              <button
                v-if="detailReportId"
                class="hold"
                type="button"
                @click="openArchiveModal(listingDetail.id, detailReportId, detailReportReason)"
              >
                Архивировать
              </button>
            </div>
          </div>

          <div class="detail-gallery">
            <div v-if="!listingDetail.photos || listingDetail.photos.length === 0" class="ticket">
              Фотографии не добавлены
            </div>
            <div v-else class="photo-grid">
              <img v-for="photo in listingDetail.photos" :key="photo.id || photo.url" :src="photo.url" alt="listing" />
            </div>
          </div>
        </div>

        <div v-else-if="detailView === 'review' && reviewDetail" class="detail-body">
          <div class="detail-main">
            <h4>Оценка: {{ reviewDetail.rating }}</h4>
            <p class="muted">{{ reviewDetail.text || 'Без текста' }}</p>
            <div class="detail-meta">
              <span>Причина жалобы: {{ detailReportReason || '—' }}</span>
              <span v-if="detailReportStatus === 'RESOLVED'">
                Комментарий модератора: {{ detailReportModeratorComment || '—' }}
              </span>
              <span>Автор: {{ reviewDetail.authorRole }}</span>
              <span>Listing: {{ reviewDetail.listingId || '—' }}</span>
              <span>Rental: {{ reviewDetail.rentalId || '—' }}</span>
              <span>Создано: {{ reviewDetail.createdAt }}</span>
            </div>
            <div v-if="detailReportStatus !== 'RESOLVED'" class="row-actions">
              <button
                v-if="detailReportId"
                class="approve"
                type="button"
                @click="openResolveModal({ id: detailReportId, targetId: detailTargetId, targetType: detailTargetType })"
              >
                Снять флаг
              </button>
              <button
                v-if="detailReportId"
                class="hold"
                type="button"
                @click="openResolveModal({ id: detailReportId, targetId: detailTargetId, targetType: detailTargetType }, 'hide_review')"
              >
                Скрыть отзыв
              </button>
            </div>
          </div>
        </div>
      </section>

      <section v-else class="content-grid">
        <div v-if="activeSection === 'moderation'" class="panel" id="moderation">
          <div class="section-header">
            <h3>Объявления с жалобами</h3>
            <div class="segmented">
              <button
                type="button"
                :class="{ active: moderationTab === 'open' }"
                @click="moderationTab = 'open'"
              >
                Открытые
              </button>
              <button
                type="button"
                :class="{ active: moderationTab === 'resolved' }"
                @click="moderationTab = 'resolved'"
              >
                Решенные
              </button>
            </div>
          </div>
          <div class="table">
            <div class="row row-header">
              <div>Объявление</div>
              <div>Причина</div>
              <div>Действия</div>
            </div>
            <div v-if="filteredListings.length === 0" class="row">Очередь пуста</div>
            <div v-for="listing in filteredListings" :key="listing.id" class="row">
              <div>
                <strong>Объявление</strong>
                <div class="badge">{{ listing.targetId }}</div>
              </div>
              <div>
                <div v-if="moderationTab === 'open'">{{ listing.reasonBody || 'Без причины' }}</div>
                <div v-else>{{ listing.moderatorComment || 'Комментарий не оставлен' }}</div>
                <small>
                  {{ listing.createdAt }}
                  <span v-if="moderationTab === 'resolved'">· Status: {{ listing.status }}</span>
                </small>
              </div>
              <div class="row-actions">
                <button class="hold" type="button" @click="openListingDetail(listing)">
                  Смотреть
                </button>
                <button
                  v-if="moderationTab === 'open'"
                  class="approve"
                  type="button"
                  @click="openResolveModal(listing)"
                >
                  Снять флаг
                </button>
                <button
                  v-if="moderationTab === 'open'"
                  class="hold"
                  type="button"
                  @click="openArchiveModal(listing.targetId, listing.id, listing.reasonBody)"
                >
                  Архивировать
                </button>
              </div>
            </div>
          </div>

          <div class="support">
            <h3>Отзывы с жалобами</h3>
            <div class="table">
              <div class="row row-header">
                <div>Отзыв</div>
                <div>Причина</div>
                <div>Действия</div>
              </div>
            <div v-if="filteredReviews.length === 0" class="row">Очередь пуста</div>
            <div v-for="review in filteredReviews" :key="review.id" class="row">
              <div>
                <strong>Отзыв</strong>
                <div class="badge danger">{{ review.targetId }}</div>
              </div>
              <div>
                <div v-if="moderationTab === 'open'">{{ review.reasonBody || 'Без причины' }}</div>
                <div v-else>{{ review.moderatorComment || 'Комментарий не оставлен' }}</div>
                <small>
                  {{ review.createdAt }}
                  <span v-if="moderationTab === 'resolved'">· Status: {{ review.status }}</span>
                </small>
              </div>
                <div class="row-actions">
                  <button class="hold" type="button" @click="openReviewDetail(review)">
                    Смотреть
                  </button>
                  <button
                    v-if="moderationTab === 'open'"
                    class="approve"
                    type="button"
                    @click="openResolveModal(review)"
                  >
                    Снять флаг
                  </button>
                  <button
                    v-if="moderationTab === 'open'"
                    class="hold"
                    type="button"
                    @click="openResolveModal(review, 'hide_review')"
                  >
                    Скрыть отзыв
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="activeSection === 'tickets'" class="panel" id="tickets">
          <div class="section-header">
            <h3>Тикеты поддержки</h3>
            <div class="segmented">
              <button
                type="button"
                :class="{ active: ticketTab === 'open' }"
                @click="ticketTab = 'open'"
              >
                Открытые
              </button>
              <button
                type="button"
                :class="{ active: ticketTab === 'progress' }"
                @click="ticketTab = 'progress'"
              >
                В работе
              </button>
              <button
                type="button"
                :class="{ active: ticketTab === 'resolved' }"
                @click="ticketTab = 'resolved'"
              >
                Решенные
              </button>
            </div>
          </div>
          <div class="activity">
            <div v-if="currentTickets.length === 0" class="ticket">Тикетов нет</div>
            <div v-for="ticket in currentTickets" :key="ticket.id" class="ticket">
              <div class="ticket-header">
                <h4>{{ ticket.title }}</h4>
                <span class="badge" :class="{ danger: ticket.status === 'OPEN' }">
                  {{ ticket.status }}
                </span>
              </div>
              <small>{{ ticket.requester }} · {{ ticket.createdAt }}</small>
              <p class="ticket-message">{{ ticket.message }}</p>
              <p v-if="ticket.status === 'RESOLVED'" class="ticket-resolution">
                Решение: {{ ticket.resolutionNotes || 'Комментарий не оставлен' }} · {{ ticket.resolvedAt || '—' }}
              </p>
              <div v-if="ticketTab === 'open'" class="row-actions">
                <button class="hold" type="button" @click="startTicket(ticket.id)">
                  В работу
                </button>
              </div>
              <div v-if="ticketTab === 'progress'" class="row-actions">
                <button class="approve" type="button" @click="openTicketResolveModal(ticket.id)">
                  Закрыть
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="activeSection === 'reports'" class="panel" id="reports">
          <div class="section-header">
            <h3>Жалобы на сделки</h3>
            <div class="segmented">
              <button
                type="button"
                :class="{ active: rentalTab === 'open' }"
                @click="rentalTab = 'open'"
              >
                Открытые
              </button>
              <button
                type="button"
                :class="{ active: rentalTab === 'resolved' }"
                @click="rentalTab = 'resolved'"
              >
                Решенные
              </button>
            </div>
          </div>
          <div class="table">
            <div class="row row-header">
              <div>Жалоба</div>
              <div>Причина</div>
              <div>Действия</div>
            </div>
            <div v-if="currentRentalReports.length === 0" class="row">Жалоб нет</div>
            <div v-for="report in currentRentalReports" :key="report.id" class="row">
              <div>
                <strong>Сделка</strong>
                <div class="badge">{{ report.targetId }}</div>
              </div>
              <div>
                <div v-if="rentalTab === 'open'">{{ report.reasonBody || 'Без причины' }}</div>
                <div v-else>{{ report.moderatorComment || 'Комментарий не оставлен' }}</div>
                <small>
                  {{ report.createdAt }}
                  · Reporter: {{ report.reporterId || '—' }}
                  · Status: {{ report.status || '—' }}
                </small>
              </div>
              <div class="row-actions">
                <button
                  v-if="rentalTab === 'open'"
                  class="approve"
                  type="button"
                  @click="openResolveModal(report)"
                >
                  Закрыть
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="panel" id="bans">
          <h3>Баны пользователей</h3>
          <form class="ban-form" @submit.prevent="createBan">
            <input v-model="banForm.bannedUserId" type="text" placeholder="User ID" />
            <input v-model="banForm.banReason" type="text" placeholder="Причина" />
            <select v-model="banForm.banType" @change="handleBanTypeChange">
              <option value="TEMP">Временный</option>
              <option value="PERM">Постоянный</option>
            </select>
            <input
              v-if="banForm.banType === 'TEMP'"
              v-model="banForm.banDuration"
              type="datetime-local"
            />
            <button class="btn primary" type="submit">Забанить</button>
          </form>

          <div class="table">
            <div class="row row-header">
              <div>Пользователь</div>
              <div>Причина</div>
              <div>Действия</div>
            </div>
            <div v-if="activeBans.length === 0" class="row">Банов нет</div>
            <div v-for="ban in activeBans" :key="ban.id" class="row">
              <div>
                <strong>{{ ban.bannedUserId }}</strong>
                <div class="badge">{{ ban.status }}</div>
              </div>
              <div>
                <div>{{ ban.banReason || 'Без причины' }}</div>
                <small>
                  {{ ban.banType === 'PERM' ? 'Постоянный' : ban.banDuration || 'Без срока' }}
                </small>
              </div>
              <div class="row-actions">
                <button class="approve" type="button" @click="liftBan(ban.id)">
                  Снять
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <div v-if="archiveModal.open" class="modal-backdrop">
      <div class="panel modal">
        <div class="detail-header">
          <div>
            <h3>Архивировать объявление</h3>
            <p class="muted">Укажи причину, она сохранится в flag_reason.</p>
          </div>
          <button class="btn ghost" type="button" @click="closeArchiveModal">Закрыть</button>
        </div>
        <label class="field">
          Причина
          <textarea v-model="archiveModal.reason" rows="4" placeholder="Опиши причину архивирования"></textarea>
        </label>
        <div class="row-actions">
          <button class="approve" type="button" @click="archiveListing">Архивировать</button>
          <button class="btn ghost" type="button" @click="closeArchiveModal">Отмена</button>
        </div>
      </div>
    </div>

    <div v-if="resolveModal.open" class="modal-backdrop">
      <div class="panel modal">
        <div class="detail-header">
          <div>
            <h3>{{ resolveModalTitle }}</h3>
            <p class="muted">Комментарий будет сохранен как решение модератора.</p>
          </div>
          <button class="btn ghost" type="button" @click="closeResolveModal">Закрыть</button>
        </div>
        <label class="field">
          Комментарий модератора
          <textarea v-model="resolveModal.comment" rows="4" placeholder="Комментарий модератора"></textarea>
        </label>
        <div class="row-actions">
          <button class="approve" type="button" @click="submitResolveModal">
            {{ resolveModalSubmitLabel }}
          </button>
          <button class="btn ghost" type="button" @click="closeResolveModal">Отмена</button>
        </div>
      </div>
    </div>

    <div v-if="ticketResolveModal.open" class="modal-backdrop">
      <div class="panel modal">
        <div class="detail-header">
          <div>
            <h3>Закрыть тикет</h3>
            <p class="muted">Комментарий будет сохранен как решение поддержки.</p>
          </div>
          <button class="btn ghost" type="button" @click="closeTicketResolveModal">Закрыть</button>
        </div>
        <label class="field">
          Комментарий
          <textarea v-model="ticketResolveModal.comment" rows="4" placeholder="Комментарий решения"></textarea>
        </label>
        <div class="row-actions">
          <button class="approve" type="button" @click="resolveTicket(ticketResolveModal.ticketId)">
            Закрыть
          </button>
          <button class="btn ghost" type="button" @click="closeTicketResolveModal">Отмена</button>
        </div>
      </div>
    </div>

  </div>
</template>
