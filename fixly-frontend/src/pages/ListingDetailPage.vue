<script setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const route = useRoute();
const router = useRouter();
const { isLoggedIn, userId } = useSession();

const listing = reactive({
  id: '',
  title: '',
  description: '',
  pricePerHour: '',
  depositAmount: '',
  autoConfirmation: false,
  latitude: '',
  longitude: '',
});

const booking = reactive({
  startAt: '',
  endAt: '',
});

const loading = ref(true);
const bookingLoading = ref(false);
const toast = reactive({ message: '', type: 'success', visible: false });
const errorMessage = ref('');

const formatPrice = (price) => (price ? `${price} ₽/ч` : 'Договорная');
const formatDeposit = (value) => (value ? `Депозит ${value} ₽` : 'Без депозита');
const hasCoords = () => listing.latitude && listing.longitude;

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
  } catch (err) {
    errorMessage.value = err.message || 'Не удалось загрузить объявление';
  } finally {
    loading.value = false;
  }
};

const bookRental = async () => {
  if (!isLoggedIn.value) {
    showToast('Сначала войдите, чтобы бронировать', 'error');
    router.push({ path: '/auth', query: { tab: 'login' } });
    return;
  }
  if (!booking.startAt || !booking.endAt) {
    showToast('Укажите дату и время начала и конца', 'error');
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
  <div class="detail-shell">
    <div class="detail-header">
      <div>
        <p class="eyebrow muted">Карточка товара</p>
        <h1 class="detail-title">{{ listing.title || 'Объявление' }}</h1>
        <p class="muted">{{ formatDeposit(listing.depositAmount) }}</p>
      </div>
      <div class="price-chip">{{ formatPrice(listing.pricePerHour) }}</div>
    </div>

    <div v-if="errorMessage" class="toast error">{{ errorMessage }}</div>

    <div class="detail-grid">
      <section class="detail-body">
        <div v-if="loading" class="skeleton-card large"></div>
        <template v-else>
          <div class="detail-card">
            <h3>Описание</h3>
            <p class="body-text">
              {{ listing.description || 'Автор еще не добавил описание' }}
            </p>
            <div class="meta-row">
              <span class="chip ghost">{{ listing.autoConfirmation ? 'Автоподтверждение' : 'Ручное подтверждение' }}</span>
              <span class="chip ghost" v-if="hasCoords()">Локация: {{ listing.latitude }}, {{ listing.longitude }}</span>
            </div>
          </div>
        </template>
      </section>

      <aside class="booking-card">
        <div class="booking-head">
          <div>
            <p class="eyebrow muted">Бронирование</p>
            <h3>Запланируйте время</h3>
          </div>
          <span class="badge">{{ isLoggedIn ? 'Вы вошли' : 'Гость' }}</span>
        </div>

        <div class="field">
          <label>Начало</label>
          <input v-model="booking.startAt" type="datetime-local">
        </div>
        <div class="field">
          <label>Конец</label>
          <input v-model="booking.endAt" type="datetime-local">
        </div>
        <button class="btn primary" type="button" :disabled="bookingLoading" @click="bookRental">
          {{ bookingLoading ? 'Бронируем...' : 'Забронировать' }}
        </button>
        <p class="helper small">Указывайте время с учетом выдачи и возврата.</p>

        <div v-if="toast.visible" class="toast" :class="toast.type">{{ toast.message }}</div>
      </aside>
    </div>
  </div>
</template>
