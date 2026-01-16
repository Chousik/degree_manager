<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { USER_API_BASE, fetchJson } from '../api/client';
import MainHeader from '../components/MainHeader.vue';
import { useSession } from '../state/session';
import { startListingConversation } from '../api/conversations';
import { getLessorReviews } from '../api/reviews';

const route = useRoute();
const router = useRouter();
const { isLoggedIn, userId } = useSession();

const listing = reactive({
  id: '',
  title: '',
  description: '',
  pricePerHour: null,
  depositAmount: null,
  autoConfirmation: false,
  latitude: '',
  longitude: '',
  address: '',
  photos: [],
  categories: [],
  availabilitySlots: [],
  ownerId: '',
  ownerName: '',
  ownerUsername: '',
  ownerRating: null,
  ownerReviewCount: 0,
  status: '',
  createdAt: '',
});

const booking = reactive({
  startAt: '',
  endAt: '',
});

const loading = ref(true);
const bookingLoading = ref(false);
const toast = reactive({ message: '', type: 'success', visible: false });
const errorMessage = ref('');
const selectedPhoto = ref('');
const isPhotoModalOpen = ref(false);
const ownerActionLoading = ref(false);
const rentalRanges = ref([]);
const selectedStartDay = ref('');
const selectedEndDay = ref('');
const bookingError = ref('');
const questionText = ref('');
const questionLoading = ref(false);
const ownerReviews = ref([]);
const reviewsLoading = ref(false);
const reviewsError = ref('');
const isReviewsOpen = ref(false);
const reportModal = reactive({
  open: false,
  targetType: 'listing',
  targetId: '',
  reason: '',
  note: '',
});
const reportSubmitting = ref(false);
const reportError = ref('');
const reportReasons = [
  'Мошенничество',
  'Запрещенный товар',
  'Неверное описание',
  'Оскорбления/спам',
  'Другое',
];
const todayStart = () => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return today;
};
const currentMonth = ref(new Date(todayStart().getFullYear(), todayStart().getMonth(), 1));

const formatPrice = (price) => (price ? `${Number(price).toLocaleString('ru-RU')} ₽/день` : 'Договорная');
const formatDeposit = (value) => (value ? `Депозит ${Number(value).toLocaleString('ru-RU')} ₽` : 'Без депозита');
const locationLabel = computed(() => {
  if (listing.address) return listing.address;
  if (listing.latitude && listing.longitude) {
    return `${listing.latitude}, ${listing.longitude}`;
  }
  return '';
});
const ownerLabel = computed(() => {
  if (listing.ownerName) {
    return listing.ownerUsername ? `${listing.ownerName} (@${listing.ownerUsername})` : listing.ownerName;
  }
  if (listing.ownerUsername) return `@${listing.ownerUsername}`;
  return listing.ownerId || '';
});
const formattedOwnerRating = computed(() => {
  if (!listing.ownerRating && listing.ownerRating !== 0) return '—';
  return Number(listing.ownerRating).toLocaleString('ru-RU', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
});
const renderStars = (rating) => {
  const rounded = Math.round(Number(rating) || 0);
  return Array.from({ length: 5 }, (_, index) => index < rounded);
};
const hasPhotos = computed(() => Array.isArray(listing.photos) && listing.photos.length > 0);
const isOwner = computed(() => Boolean(listing.ownerId && userId.value && listing.ownerId === userId.value));
const sortedPhotos = computed(() => {
  if (!Array.isArray(listing.photos)) return [];
  return [...listing.photos].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));
});
const galleryMain = computed(() => {
  if (selectedPhoto.value) return selectedPhoto.value;
  return sortedPhotos.value[0]?.url || '';
});
const currentPhotoIndex = computed(() => {
  if (!sortedPhotos.value.length) return -1;
  const matchIndex = sortedPhotos.value.findIndex((photo) => photo.url === galleryMain.value);
  return matchIndex >= 0 ? matchIndex : 0;
});
const totalPhotos = computed(() => sortedPhotos.value.length);
const photoPosition = computed(() => (currentPhotoIndex.value >= 0 ? currentPhotoIndex.value + 1 : 0));
const formattedDate = computed(() => {
  if (!listing.createdAt) return '';
  try {
    return new Date(listing.createdAt).toLocaleDateString('ru-RU');
  } catch (err) {
    return listing.createdAt;
  }
});
const availabilitySlots = computed(() => {
  if (!Array.isArray(listing.availabilitySlots)) return [];
  return listing.availabilitySlots;
});
const availableDaySet = computed(() => {
  if (!availabilitySlots.value.length) return null;
  const days = new Set();
  availabilitySlots.value.forEach((slot) => {
    const key = dateKey(slot.startsAt);
    if (key) days.add(key);
  });
  return days;
});
const dateKey = (value) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
const blockedDaySet = computed(() => {
  const blocked = new Set();
  rentalRanges.value.forEach((range) => {
    const start = new Date(range.startAt);
    const end = new Date(range.endAt);
    if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) return;
    const cursor = new Date(start);
    while (cursor < end) {
      blocked.add(dateKey(cursor));
      cursor.setDate(cursor.getDate() + 1);
    }
  });
  return blocked;
});
const monthLabel = computed(() =>
  currentMonth.value.toLocaleDateString('ru-RU', { month: 'long', year: 'numeric' })
);
const maxMonth = computed(() => {
  const today = new Date();
  return new Date(today.getFullYear(), today.getMonth() + 11, 1);
});
const minMonth = computed(() => {
  const today = new Date();
  return new Date(today.getFullYear(), today.getMonth(), 1);
});
const canPrevMonth = computed(() => {
  const prev = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1);
  return prev >= minMonth.value;
});
const canNextMonth = computed(() => {
  const next = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1);
  return next <= maxMonth.value;
});
const calendarCells = computed(() => {
  const cells = [];
  const monthStart = new Date(currentMonth.value);
  const year = monthStart.getFullYear();
  const month = monthStart.getMonth();
  const firstDay = new Date(year, month, 1);
  const startOffset = (firstDay.getDay() + 6) % 7;
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  for (let i = 0; i < startOffset; i += 1) {
    cells.push({ key: `blank-${year}-${month}-${i}`, isBlank: true });
  }
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const availableSet = availableDaySet.value;
  for (let day = 1; day <= daysInMonth; day += 1) {
    const current = new Date(year, month, day);
    const key = dateKey(current);
    const isAvailable = !availableSet || availableSet.has(key);
    const isBlocked = blockedDaySet.value.has(key);
    const isPast = current < today;
    cells.push({
      key,
      label: day,
      isAvailable,
      isBlocked,
      isPast,
    });
  }
  return cells;
});

const selectPhotoByIndex = (index) => {
  const count = sortedPhotos.value.length;
  if (!count) return;
  const normalized = ((index % count) + count) % count;
  selectedPhoto.value = sortedPhotos.value[normalized].url;
};

const goNextPhoto = () => selectPhotoByIndex(currentPhotoIndex.value + 1);
const goPrevPhoto = () => selectPhotoByIndex(currentPhotoIndex.value - 1);
const openPhotoModal = () => {
  if (!hasPhotos.value) return;
  isPhotoModalOpen.value = true;
};
const closePhotoModal = () => {
  isPhotoModalOpen.value = false;
};

const isDayDisabled = (dayKey) => {
  if (!dayKey) return true;
  const availableSet = availableDaySet.value;
  if (availableSet && !availableSet.has(dayKey)) return true;
  if (blockedDaySet.value.has(dayKey)) return true;
  const dayDate = new Date(dayKey);
  if (Number.isNaN(dayDate.getTime())) return true;
  dayDate.setHours(0, 0, 0, 0);
  return dayDate < todayStart();
};

const setBookingRange = (startKey, endKey) => {
  booking.startAt = `${startKey}T00:00`;
  booking.endAt = `${endKey}T00:00`;
};

const selectDay = (dayKey) => {
  if (!dayKey || isDayDisabled(dayKey)) return;
  bookingError.value = '';
  if (!selectedStartDay.value || (selectedStartDay.value && selectedEndDay.value)) {
    selectedStartDay.value = dayKey;
    selectedEndDay.value = '';
    booking.startAt = '';
    booking.endAt = '';
    return;
  }
  if (dayKey <= selectedStartDay.value) {
    selectedStartDay.value = dayKey;
    selectedEndDay.value = '';
    booking.startAt = '';
    booking.endAt = '';
    return;
  }
  const start = new Date(selectedStartDay.value);
  const end = new Date(dayKey);
  const cursor = new Date(start);
  while (cursor < end) {
    const key = dateKey(cursor);
    if (isDayDisabled(key)) {
      bookingError.value = 'В выбранном диапазоне есть занятые дни';
      return;
    }
    cursor.setDate(cursor.getDate() + 1);
  }
  selectedEndDay.value = dayKey;
  setBookingRange(selectedStartDay.value, selectedEndDay.value);
};

const prevMonth = () => {
  const prev = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1);
  if (canPrevMonth.value) {
    currentMonth.value = prev;
  }
};

const nextMonth = () => {
  const next = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1);
  if (canNextMonth.value) {
    currentMonth.value = next;
  }
};

const canBook = computed(() => {
  if (isOwner.value) {
    return false;
  }
  return Boolean(booking.startAt && booking.endAt);
});

const rentalDays = computed(() => {
  if (!selectedStartDay.value || !selectedEndDay.value) return 0;
  const start = new Date(selectedStartDay.value);
  const end = new Date(selectedEndDay.value);
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) return 0;
  const diff = (end - start) / (1000 * 60 * 60 * 24);
  return diff > 0 ? diff : 0;
});

const rentalAmount = computed(() => {
  if (!rentalDays.value || !listing.pricePerHour) return 0;
  return Number(listing.pricePerHour) * rentalDays.value;
});

const depositAmount = computed(() => Number(listing.depositAmount || 0));

const totalAmount = computed(() => rentalAmount.value + depositAmount.value);

const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};

const loadListingRentals = async (id) => {
  if (!id) {
    rentalRanges.value = [];
    return;
  }
  try {
    const data = await fetchJson(`${USER_API_BASE}/rentals/listing?listingId=${id}`);
    rentalRanges.value = Array.isArray(data) ? data : [];
  } catch (err) {
    rentalRanges.value = [];
  }
};

const fetchListing = async (id) => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const data = await fetchJson(`${USER_API_BASE}/listings/${id}`);
    Object.assign(listing, data || {});
    listing.id = data?.id || id;
    if (Array.isArray(data?.photos) && data.photos.length > 0) {
      selectedPhoto.value = data.photos[0].url;
    }
    await loadListingRentals(listing.id);
    booking.startAt = '';
    booking.endAt = '';
    selectedStartDay.value = '';
    selectedEndDay.value = '';
    bookingError.value = '';
  } catch (err) {
    errorMessage.value = err.message || 'Не удалось загрузить объявление';
  } finally {
    loading.value = false;
  }
};

const bookRental = async () => {
  if (!isLoggedIn.value) {
    showToast('Сначала войдите, чтобы бронировать', 'error');
    router.push('/login');
    return;
  }
  if (isOwner.value) {
    showToast('Нельзя арендовать собственное объявление', 'error');
    return;
  }
  if (!booking.startAt || !booking.endAt) {
    showToast('Выберите даты аренды', 'error');
    return;
  }
  if (bookingError.value) {
    showToast(bookingError.value, 'error');
    return;
  }
  if (bookingLoading.value) return;
  bookingLoading.value = true;
  try {
    const payload = {
      listingId: listing.id,
      lesseeId: userId.value,
      startAt: booking.startAt,
      endAt: booking.endAt,
      depositAmount: listing.depositAmount || 0,
    };
    const response = await fetchJson(`${USER_API_BASE}/rentals`, { method: 'POST', body: JSON.stringify(payload) });
    showToast('Бронь создана. Даты закреплены за вами', 'success');
    if (response?.paymentConfirmationUrl) {
      window.location.href = response.paymentConfirmationUrl;
      return;
    }
    await loadListingRentals(listing.id);
  } catch (err) {
    showToast(err.message || 'Не удалось забронировать', 'error');
  } finally {
    bookingLoading.value = false;
  }
};

const askQuestion = async () => {
  if (!isLoggedIn.value) {
    showToast('Сначала войдите, чтобы написать', 'error');
    router.push('/login');
    return;
  }
  if (isOwner.value) {
    showToast('Нельзя писать самому себе', 'error');
    return;
  }
  if (!questionText.value.trim()) {
    showToast('Введите вопрос', 'error');
    return;
  }
  if (questionLoading.value) return;
  questionLoading.value = true;
  try {
    const response = await startListingConversation(listing.id, {
      senderId: userId.value,
      body: questionText.value.trim(),
    });
    questionText.value = '';
    if (response?.conversationId) {
      router.push({ path: '/account', query: { tab: 'chats', conversation: response.conversationId } });
      return;
    }
    showToast('Сообщение отправлено', 'success');
  } catch (err) {
    showToast(err.message || 'Не удалось отправить сообщение', 'error');
  } finally {
    questionLoading.value = false;
  }
};

const openReviewsModal = async () => {
  if (!listing.ownerId) return;
  isReviewsOpen.value = true;
  reviewsLoading.value = true;
  reviewsError.value = '';
  try {
    const data = await getLessorReviews(listing.ownerId);
    ownerReviews.value = Array.isArray(data) ? data : [];
  } catch (err) {
    reviewsError.value = err?.message || 'Не удалось загрузить отзывы.';
    ownerReviews.value = [];
  } finally {
    reviewsLoading.value = false;
  }
};

const closeReviewsModal = () => {
  isReviewsOpen.value = false;
};

const openReportModal = (type, id) => {
  if (!isLoggedIn.value) {
    showToast('Сначала войдите, чтобы пожаловаться', 'error');
    router.push('/login');
    return;
  }
  reportModal.open = true;
  reportModal.targetType = type;
  reportModal.targetId = id;
  reportModal.reason = '';
  reportModal.note = '';
  reportError.value = '';
};

const closeReportModal = () => {
  reportModal.open = false;
};

const submitReport = async () => {
  if (!reportModal.targetId || !userId.value) return;
  if (!reportModal.reason) {
    reportError.value = 'Выберите причину';
    return;
  }
  const reason = reportModal.note
    ? `${reportModal.reason}: ${reportModal.note}`
    : reportModal.reason;
  reportSubmitting.value = true;
  reportError.value = '';
  try {
    const endpoint =
      reportModal.targetType === 'review'
        ? `${USER_API_BASE}/moderation/reviews/${reportModal.targetId}/flag`
        : `${USER_API_BASE}/moderation/listings/${reportModal.targetId}/flag`;
    await fetchJson(endpoint, {
      method: 'POST',
      body: JSON.stringify({ reporterId: userId.value, reason }),
    });
    showToast('Жалоба отправлена', 'success');
    closeReportModal();
  } catch (err) {
    reportError.value = err?.message || 'Не удалось отправить жалобу.';
  } finally {
    reportSubmitting.value = false;
  }
};

const archiveListing = async () => {
  if (!isLoggedIn.value || !userId.value || !listing.id) return;
  if (ownerActionLoading.value) return;
  ownerActionLoading.value = true;
  try {
    await fetchJson(`${USER_API_BASE}/listings/${listing.id}/archive?ownerId=${userId.value}`, { method: 'POST' });
    showToast('Объявление перенесено в архив', 'success');
  } catch (err) {
    showToast(err.message || 'Не удалось архивировать', 'error');
  } finally {
    ownerActionLoading.value = false;
  }
};

const unarchiveListing = async () => {
  if (!isLoggedIn.value || !userId.value || !listing.id) return;
  if (ownerActionLoading.value) return;
  ownerActionLoading.value = true;
  try {
    await fetchJson(`${USER_API_BASE}/listings/${listing.id}/unarchive?ownerId=${userId.value}`, { method: 'POST' });
    showToast('Объявление восстановлено', 'success');
  } catch (err) {
    showToast(err.message || 'Не удалось разархивировать', 'error');
  } finally {
    ownerActionLoading.value = false;
  }
};

const deleteListing = async () => {
  if (!isLoggedIn.value || !userId.value || !listing.id) return;
  if (!window.confirm('Удалить объявление? Действие нельзя отменить.')) return;
  if (ownerActionLoading.value) return;
  ownerActionLoading.value = true;
  try {
    await fetchJson(`${USER_API_BASE}/listings/${listing.id}?ownerId=${userId.value}`, { method: 'DELETE' });
    showToast('Объявление удалено', 'success');
    router.push('/listings');
  } catch (err) {
    showToast(err.message || 'Не удалось удалить объявление', 'error');
  } finally {
    ownerActionLoading.value = false;
  }
};

onMounted(() => {
  const id = route.params.id;
  if (!id) {
    router.replace('/catalog');
    return;
  }
  fetchListing(id);
});

watch(() => route.params.id, (newId) => newId && fetchListing(newId));
</script>

<template>
  <div class="landing listing-detail-page">
    <MainHeader />

    <section class="landing-section listing-hero">
      <div class="landing-card listing-head">
        <div>
          <button class="landing-link" type="button" @click="router.push('/catalog')">Каталог</button>
          <h1 class="listing-title">{{ listing.title || 'Объявление' }}</h1>
          <div class="listing-subtitle">
            <span class="chip ghost">{{ listing.autoConfirmation ? 'Мгновенное подтверждение' : 'Ручное подтверждение' }}</span>
            <span class="chip ghost" v-if="listing.status">Статус: {{ listing.status }}</span>
            <span class="chip ghost" v-if="formattedDate">Создано: {{ formattedDate }}</span>
          </div>
        </div>
        <div class="price-chip">{{ formatPrice(listing.pricePerHour) }}</div>
      </div>

      <p v-if="errorMessage" class="landing-note error">{{ errorMessage }}</p>

      <div class="listing-grid">
        <section class="listing-gallery">
          <div v-if="loading" class="skeleton-card large"></div>
          <template v-else>
            <div class="gallery-main">
              <div v-if="!hasPhotos" class="gallery-placeholder">
                Фото появятся позже
              </div>
              <img v-else :src="galleryMain" :alt="listing.title" @click="openPhotoModal" />
              <button
                v-if="hasPhotos && totalPhotos > 1"
                type="button"
                class="gallery-nav prev"
                aria-label="Предыдущее фото"
                @click="goPrevPhoto"
              >
                ‹
              </button>
              <button
                v-if="hasPhotos && totalPhotos > 1"
                type="button"
                class="gallery-nav next"
                aria-label="Следующее фото"
                @click="goNextPhoto"
              >
                ›
              </button>
              <div v-if="hasPhotos && totalPhotos > 1" class="gallery-counter">
                {{ photoPosition }} / {{ totalPhotos }}
              </div>
            </div>
            <div class="gallery-thumbs" v-if="hasPhotos">
              <button
                v-for="photo in sortedPhotos"
                :key="photo.id || photo.url"
                type="button"
                class="thumb"
                :class="{ active: photo.url === galleryMain }"
                @click="selectedPhoto = photo.url"
              >
                <img :src="photo.url" :alt="listing.title" />
              </button>
            </div>
          </template>
        </section>

        <aside class="landing-card listing-book" v-if="!isOwner">
          <div class="booking-head">
            <div>
              <p class="eyebrow muted">Бронирование</p>
              <h3>Запланируйте время</h3>
            </div>
          </div>

          <div class="day-picker">
            <div class="calendar-header">
              <button class="calendar-nav" type="button" :disabled="!canPrevMonth" @click="prevMonth">‹</button>
              <div class="calendar-title">{{ monthLabel }}</div>
              <button class="calendar-nav" type="button" :disabled="!canNextMonth" @click="nextMonth">›</button>
            </div>
            <div class="calendar-weekdays">
              <span>Пн</span>
              <span>Вт</span>
              <span>Ср</span>
              <span>Чт</span>
              <span>Пт</span>
              <span>Сб</span>
              <span>Вс</span>
            </div>
            <div class="calendar-grid">
              <span v-for="cell in calendarCells" :key="cell.key" class="calendar-cell" :class="{ blank: cell.isBlank }">
                <button
                  v-if="!cell.isBlank"
                  type="button"
                  class="calendar-day"
                  :class="{
                    active: cell.key === selectedStartDay || cell.key === selectedEndDay,
                    inRange: selectedStartDay && selectedEndDay && cell.key > selectedStartDay && cell.key < selectedEndDay,
                    disabled: cell.isPast || !cell.isAvailable || cell.isBlocked,
                  }"
                  :disabled="cell.isPast || !cell.isAvailable || cell.isBlocked"
                  @click="selectDay(cell.key)"
                >
                  {{ cell.label }}
                </button>
              </span>
            </div>
            <p v-if="selectedStartDay && !selectedEndDay" class="muted small">Выберите дату возврата.</p>
            <p v-if="selectedStartDay && selectedEndDay" class="helper small">
              Период: {{ selectedStartDay }} — {{ selectedEndDay }}
            </p>
            <p v-if="bookingError" class="helper small error">{{ bookingError }}</p>
            <p class="muted small">Занятые дни недоступны для выбора.</p>
          </div>

          <div class="price-stack">
            <div class="price-value">{{ formatPrice(listing.pricePerHour) }}</div>
            <div class="muted small">{{ formatDeposit(listing.depositAmount) }}</div>
          </div>
          <div v-if="rentalDays" class="price-breakdown">
            <div>Аренда ({{ rentalDays }} дн.): {{ rentalAmount.toLocaleString('ru-RU') }} ₽</div>
            <div>Залог: {{ depositAmount.toLocaleString('ru-RU') }} ₽</div>
            <div class="price-breakdown__total">Итого: {{ totalAmount.toLocaleString('ru-RU') }} ₽</div>
          </div>

          <button class="landing-btn primary" type="button" :disabled="bookingLoading || !canBook" @click="bookRental">
            {{ bookingLoading ? 'Бронируем...' : 'Забронировать' }}
          </button>
          <p class="helper small">Аренда считается посуточно.</p>

          <div class="listing-question">
            <label class="listing-question__label">
              Вопрос владельцу
              <textarea v-model="questionText" rows="3" placeholder="Уточните детали по аренде"></textarea>
            </label>
            <button class="landing-btn ghost" type="button" :disabled="questionLoading" @click="askQuestion">
              {{ questionLoading ? 'Отправляем...' : 'Задать вопрос' }}
            </button>
          </div>

          <div v-if="toast.visible" class="toast" :class="toast.type">{{ toast.message }}</div>
        </aside>
        <aside class="landing-card listing-book" v-else>
          <div class="booking-head">
            <div>
              <p class="eyebrow muted">Ваше объявление</p>
              <h3>Управление</h3>
            </div>
          </div>
          <button class="landing-btn ghost" type="button" @click="router.push(`/listings/${listing.id}/edit`)">
            Редактировать
          </button>
          <button
            v-if="listing.status === 'ARCHIVED'"
            class="landing-btn ghost"
            type="button"
            :disabled="ownerActionLoading"
            @click="unarchiveListing"
          >
            Разархивировать
          </button>
          <button
            v-else
            class="landing-btn ghost"
            type="button"
            :disabled="ownerActionLoading"
            @click="archiveListing"
          >
            Архивировать
          </button>
          <button class="landing-btn danger" type="button" :disabled="ownerActionLoading" @click="deleteListing">
            Удалить
          </button>
          <p class="helper small">Удаление возможно, только если нет незавершённых аренд.</p>
          <div v-if="toast.visible" class="toast" :class="toast.type">{{ toast.message }}</div>
        </aside>
      </div>

      <div class="listing-details-grid">
        <div class="landing-card">
          <h3>Описание</h3>
          <p class="body-text">
            {{ listing.description || 'Автор еще не добавил описание' }}
          </p>
          <div class="meta-row">
            <span class="chip ghost">{{ formatDeposit(listing.depositAmount) }}</span>
            <span class="chip ghost" v-if="locationLabel">Локация: {{ locationLabel }}</span>
            <span class="chip ghost" v-if="ownerLabel">
              Владелец: {{ ownerLabel }}
            </span>
            <button
              v-if="listing.ownerId"
              type="button"
              class="chip ghost chip-action"
              @click="openReviewsModal"
            >
              Рейтинг: {{ formattedOwnerRating }} · отзывов: {{ listing.ownerReviewCount || 0 }}
            </button>
            <button
              v-if="listing.id"
              type="button"
              class="chip ghost chip-action"
              @click="openReportModal('listing', listing.id)"
            >
              Пожаловаться
            </button>
          </div>
          <div class="tag-row">
            <span v-if="!listing.categories?.length" class="muted">Категории не указаны</span>
            <span v-else v-for="cat in listing.categories" :key="cat.id" class="chip">
              {{ cat.name }}
            </span>
          </div>
        </div>
      </div>
    </section>

    <div v-if="isPhotoModalOpen" class="photo-modal" @click.self="closePhotoModal">
      <div class="photo-modal__window">
        <button class="photo-modal__close" type="button" aria-label="Закрыть" @click="closePhotoModal">×</button>
        <div class="photo-modal__frame">
          <img :src="galleryMain" :alt="listing.title" />
        </div>
        <div class="photo-modal__nav">
          <button
            v-if="totalPhotos > 1"
            type="button"
            class="photo-modal__button"
            aria-label="Предыдущее фото"
            @click="goPrevPhoto"
          >
            ‹ Назад
          </button>
          <span class="photo-modal__count">{{ photoPosition }} / {{ totalPhotos }}</span>
          <button
            v-if="totalPhotos > 1"
            type="button"
            class="photo-modal__button"
            aria-label="Следующее фото"
            @click="goNextPhoto"
          >
            Вперед ›
          </button>
        </div>
      </div>
    </div>

    <div v-if="isReviewsOpen" class="photo-modal" @click.self="closeReviewsModal">
      <div class="photo-modal__window reviews-modal">
        <button class="photo-modal__close" type="button" aria-label="Закрыть" @click="closeReviewsModal">×</button>
        <div class="reviews-modal__head">
          <h3>Отзывы об арендодателе</h3>
          <div class="muted small">Рейтинг: {{ formattedOwnerRating }} · всего: {{ listing.ownerReviewCount || 0 }}</div>
        </div>
        <p v-if="reviewsLoading" class="helper">Загружаем отзывы...</p>
        <p v-else-if="reviewsError" class="helper error">{{ reviewsError }}</p>
        <div v-else class="reviews-list">
          <article v-for="review in ownerReviews" :key="review.id" class="review-card">
            <div class="review-card__head">
              <div class="review-card__title">{{ review.listingTitle || 'Объявление' }}</div>
              <div class="review-card__rating">
                <span class="stars">
                  <span
                    v-for="(active, index) in renderStars(review.rating)"
                    :key="index"
                    class="star"
                    :class="{ active }"
                  >
                    ★
                  </span>
                </span>
                <span class="rating-value">{{ review.rating }}</span>
              </div>
            </div>
            <div class="review-card__meta">
              {{ review.rentalStatus === 'COMPLETED' ? 'Аренда состоялась' : 'Аренда не состоялась' }}
            </div>
            <p class="review-card__text">{{ review.text }}</p>
            <div class="review-card__date">{{ review.createdAt ? new Date(review.createdAt).toLocaleDateString('ru-RU') : '' }}</div>
            <div class="review-card__actions">
              <button type="button" class="report-btn" @click="openReportModal('review', review.id)">
                Пожаловаться
              </button>
            </div>
          </article>
          <p v-if="!ownerReviews.length" class="helper">Отзывов пока нет.</p>
        </div>
      </div>
    </div>

    <div v-if="reportModal.open" class="photo-modal" @click.self="closeReportModal">
      <div class="photo-modal__window report-modal">
        <button class="photo-modal__close" type="button" aria-label="Закрыть" @click="closeReportModal">×</button>
        <h3>Жалоба</h3>
        <form class="report-modal__form" @submit.prevent="submitReport">
          <label>
            Причина
            <select v-model="reportModal.reason">
              <option value="" disabled>Выберите причину</option>
              <option v-for="reason in reportReasons" :key="reason" :value="reason">{{ reason }}</option>
            </select>
          </label>
          <label>
            Детали (необязательно)
            <textarea v-model="reportModal.note" rows="3" placeholder="Опишите, что не так"></textarea>
          </label>
          <div v-if="reportError" class="helper error">{{ reportError }}</div>
          <div class="report-modal__actions">
            <button type="button" class="photo-modal__button" @click="closeReportModal">Отмена</button>
            <button type="submit" class="photo-modal__button" :disabled="reportSubmitting">
              {{ reportSubmitting ? 'Отправляем...' : 'Отправить' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
