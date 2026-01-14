<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { USER_API_BASE, fetchJson } from '../api/client';
import MainHeader from '../components/MainHeader.vue';
import { useSession } from '../state/session';

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
const selectedSlotId = ref('');

const formatPrice = (price) => (price ? `${Number(price).toLocaleString('ru-RU')} ₽/ч` : 'Договорная');
const formatDeposit = (value) => (value ? `Депозит ${Number(value).toLocaleString('ru-RU')} ₽` : 'Без депозита');
const locationLabel = computed(() => {
  if (listing.address) return listing.address;
  if (listing.latitude && listing.longitude) {
    return `${listing.latitude}, ${listing.longitude}`;
  }
  return '';
});
const hasPhotos = computed(() => Array.isArray(listing.photos) && listing.photos.length > 0);
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
const dateKey = (value) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
const slotGroups = computed(() => {
  const groups = new Map();
  availabilitySlots.value.forEach((slot) => {
    const key = dateKey(slot.startsAt);
    if (!key) return;
    if (!groups.has(key)) {
      groups.set(key, []);
    }
    groups.get(key).push(slot);
  });
  for (const [key, slots] of groups.entries()) {
    groups.set(key, [...slots].sort((a, b) => new Date(a.startsAt) - new Date(b.startsAt)));
  }
  return groups;
});
const slotDates = computed(() => {
  const dates = [];
  slotGroups.value.forEach((slots, key) => {
    const [year, month, day] = key.split('-').map(Number);
    const labelDate = new Date(year, month - 1, day);
    dates.push({
      key,
      slots,
      label: labelDate.toLocaleDateString('ru-RU', { day: 'numeric', month: 'long' }),
    });
  });
  return dates.sort((a, b) => a.key.localeCompare(b.key));
});
const hasSlots = computed(() => slotDates.value.length > 0);
const selectedDateKey = ref('');
const slotTimeLabel = (slot) => {
  const start = new Date(slot.startsAt);
  const end = new Date(slot.endsAt);
  return `${start.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })} — ` +
    `${end.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
};
const selectedDateSlots = computed(() => slotGroups.value.get(selectedDateKey.value) || []);
const slotLabel = (slot) => {
  const start = new Date(slot.startsAt);
  const end = new Date(slot.endsAt);
  return `${start.toLocaleDateString('ru-RU')} ${start.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })} — ` +
    `${end.toLocaleDateString('ru-RU')} ${end.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}`;
};

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

const pickDate = (key) => {
  selectedDateKey.value = key;
  if (selectedSlotId.value) {
    const selectedSlot = availabilitySlots.value.find((slot) => slot.id === selectedSlotId.value);
    if (selectedSlot && dateKey(selectedSlot.startsAt) !== key) {
      selectedSlotId.value = '';
      booking.startAt = '';
      booking.endAt = '';
    }
  }
};

const pickSlot = (slot) => {
  if (!slot) return;
  selectedDateKey.value = dateKey(slot.startsAt);
  selectedSlotId.value = slot.id;
  booking.startAt = slot.startsAt?.slice(0, 16) || '';
  booking.endAt = slot.endsAt?.slice(0, 16) || '';
};

const canBook = computed(() => {
  if (hasSlots.value) {
    return Boolean(selectedSlotId.value);
  }
  return Boolean(booking.startAt && booking.endAt);
});

const selectedSlot = computed(() => {
  if (!selectedSlotId.value) return null;
  return availabilitySlots.value.find((slot) => slot.id === selectedSlotId.value) || null;
});

const slotSummary = computed(() => {
  if (!selectedSlot.value) return '';
  return slotLabel(selectedSlot.value);
});

const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
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
    selectedSlotId.value = '';
    selectedDateKey.value = '';
    booking.startAt = '';
    booking.endAt = '';
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
  if (!booking.startAt || !booking.endAt) {
    showToast('Укажите дату и время начала и конца', 'error');
    return;
  }
  if (hasSlots.value && !selectedSlotId.value) {
    showToast('Выберите доступный слот', 'error');
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
    await fetchJson(`${USER_API_BASE}/rentals`, { method: 'POST', body: JSON.stringify(payload) });
    showToast('Бронь создана. Мы держим слоты за вами', 'success');
  } catch (err) {
    showToast(err.message || 'Не удалось забронировать', 'error');
  } finally {
    bookingLoading.value = false;
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

        <aside class="landing-card listing-book">
          <div class="booking-head">
            <div>
              <p class="eyebrow muted">Бронирование</p>
              <h3>Запланируйте время</h3>
            </div>
          </div>

          <div v-if="hasSlots" class="slot-picker">
            <p class="muted small">Свободные даты</p>
            <div class="date-grid">
              <button
                v-for="date in slotDates"
                :key="date.key"
                type="button"
                class="date-pill"
                :class="{ active: date.key === selectedDateKey }"
                @click="pickDate(date.key)"
              >
                <span class="date-pill__label">{{ date.label }}</span>
                <span class="muted small">{{ date.slots.length }} слотов</span>
              </button>
            </div>
            <div v-if="selectedDateKey" class="time-grid">
              <button
                v-for="slot in selectedDateSlots"
                :key="slot.id"
                type="button"
                class="time-pill"
                :class="{ active: slot.id === selectedSlotId }"
                @click="pickSlot(slot)"
              >
                {{ slotTimeLabel(slot) }}
              </button>
            </div>
            <p v-else class="muted small">Сначала выберите дату.</p>
            <p v-if="slotSummary" class="helper small">Выбрано: {{ slotSummary }}</p>
          </div>

          <div class="price-stack">
            <div class="price-value">{{ formatPrice(listing.pricePerHour) }}</div>
            <div class="muted small">{{ formatDeposit(listing.depositAmount) }}</div>
          </div>

          <template v-if="!hasSlots">
            <div class="field">
              <label>Начало</label>
              <input v-model="booking.startAt" type="datetime-local">
            </div>
            <div class="field">
              <label>Конец</label>
              <input v-model="booking.endAt" type="datetime-local">
            </div>
          </template>
          <button class="landing-btn primary" type="button" :disabled="bookingLoading || !canBook" @click="bookRental">
            {{ bookingLoading ? 'Бронируем...' : 'Забронировать' }}
          </button>
          <p class="helper small">Указывайте время с учетом выдачи и возврата.</p>

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
            <span class="chip ghost" v-if="listing.ownerId">Владелец: {{ listing.ownerId }}</span>
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
  </div>
</template>
