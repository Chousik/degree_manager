<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const router = useRouter();
const { isLoggedIn, userId } = useSession();
const listings = ref({ active: [], archived: [] });
const loading = ref(false);
const error = ref('');
const ownerRentals = ref([]);
const rentalsLoading = ref(false);
const rentalsError = ref('');
const ownerActionLoading = ref('');

async function loadMyListings() {
  if (!isLoggedIn.value) {
    listings.value = { active: [], archived: [] };
    ownerRentals.value = [];
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const data = await fetchJson(`${USER_API_BASE}/account/me`);
    listings.value = {
      active: Array.isArray(data?.activeListings) ? data.activeListings : [],
      archived: Array.isArray(data?.archivedListings) ? data.archivedListings : [],
    };
  } catch (err) {
    error.value = 'Не удалось загрузить ваши объявления. Перезайдите и попробуйте снова.';
    listings.value = { active: [], archived: [] };
  } finally {
    loading.value = false;
  }
}

async function loadOwnerRentals() {
  if (!isLoggedIn.value || !userId.value) {
    ownerRentals.value = [];
    return;
  }
  rentalsLoading.value = true;
  rentalsError.value = '';
  try {
    const data = await fetchJson(`${USER_API_BASE}/rentals/owner?ownerId=${userId.value}`);
    ownerRentals.value = Array.isArray(data) ? data : [];
  } catch (err) {
    rentalsError.value = 'Не удалось загрузить занятость.';
    ownerRentals.value = [];
  } finally {
    rentalsLoading.value = false;
  }
}

onMounted(() => {
  loadMyListings();
  loadOwnerRentals();
});

watch(
  [() => isLoggedIn.value, () => userId.value],
  () => {
    loadMyListings();
    loadOwnerRentals();
  }
);

function formatPrice(value) {
  if (!value && value !== 0) {
    return 'Цена по запросу';
  }
  const formatted = Number(value).toLocaleString('ru-RU');
  return `${formatted} ₽/день`;
}

function formatDate(value) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleString('ru-RU', { dateStyle: 'short', timeStyle: 'short' });
}

function openListing(id) {
  if (!id) return;
  router.push(`/catalog/${id}`);
}

function editListing(id, event) {
  event?.stopPropagation();
  if (!id) return;
  router.push(`/listings/${id}/edit`);
}

function hasActiveRental(listingId) {
  return ownerRentals.value.some((rental) => rental.listingId === listingId);
}

async function archiveListing(id, event) {
  event?.stopPropagation();
  if (!id || !userId.value) return;
  if (ownerActionLoading.value) return;
  if (!window.confirm('Перенести объявление в архив?')) return;
  ownerActionLoading.value = id;
  try {
    await fetchJson(`${USER_API_BASE}/listings/${id}/archive?ownerId=${userId.value}`, { method: 'POST' });
    await loadMyListings();
  } catch (err) {
    error.value = err.message || 'Не удалось архивировать объявление.';
  } finally {
    ownerActionLoading.value = '';
  }
}

async function unarchiveListing(id, event) {
  event?.stopPropagation();
  if (!id || !userId.value) return;
  if (ownerActionLoading.value) return;
  ownerActionLoading.value = id;
  try {
    await fetchJson(`${USER_API_BASE}/listings/${id}/unarchive?ownerId=${userId.value}`, { method: 'POST' });
    await loadMyListings();
  } catch (err) {
    error.value = err.message || 'Не удалось разархивировать объявление.';
  } finally {
    ownerActionLoading.value = '';
  }
}

async function deleteListing(id, event) {
  event?.stopPropagation();
  if (!id || !userId.value) return;
  if (ownerActionLoading.value) return;
  if (!window.confirm('Удалить объявление? Действие нельзя отменить.')) return;
  ownerActionLoading.value = id;
  try {
    await fetchJson(`${USER_API_BASE}/listings/${id}?ownerId=${userId.value}`, { method: 'DELETE' });
    await loadMyListings();
    await loadOwnerRentals();
  } catch (err) {
    error.value = err.message || 'Не удалось удалить объявление.';
  } finally {
    ownerActionLoading.value = '';
  }
}

async function confirmRental(id, event) {
  event?.stopPropagation();
  if (!id || !userId.value) return;
  try {
    await fetchJson(`${USER_API_BASE}/rentals/${id}/confirm`, {
      method: 'POST',
      body: JSON.stringify({ actorId: userId.value }),
    });
    await loadOwnerRentals();
  } catch (err) {
    rentalsError.value = err.message || 'Не удалось подтвердить аренду.';
  }
}
</script>

<template>
  <div class="dashboard">
    <MainHeader />

    <section class="dashboard-section">
      <div class="section-header">
        <h2>Мои объявления</h2>
        <RouterLink v-if="isLoggedIn" to="/listings/new" class="btn primary">Создать объявление</RouterLink>
      </div>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите, чтобы управлять объявлениями.</p>
      <p v-else-if="error" class="dashboard-note error">{{ error }}</p>
      <p v-else-if="loading" class="dashboard-note">Загружаем объявления...</p>
      <div v-else>
        <div v-if="listings.active.length" class="dashboard-subsection">
          <h3>Активные</h3>
          <div class="listings-grid">
            <article
              v-for="item in listings.active"
              :key="item.id"
              class="listing-card"
              @click="openListing(item.id)"
            >
              <div class="listing-card__title">{{ item.title }}</div>
              <p class="listing-card__description">{{ item.description || 'Без описания' }}</p>
              <div class="listing-card__meta">
                <span>{{ formatPrice(item.pricePerHour) }}</span>
                <span class="chip">{{ item.status }}</span>
              </div>
              <div class="listing-card__actions">
                <button class="btn secondary" type="button" @click="editListing(item.id, $event)">Редактировать</button>
                <button class="btn secondary" type="button" @click="archiveListing(item.id, $event)">Архивировать</button>
                <button
                  class="btn secondary"
                  type="button"
                  :disabled="hasActiveRental(item.id)"
                  :title="hasActiveRental(item.id) ? 'Есть незавершенные аренды' : ''"
                  @click="deleteListing(item.id, $event)"
                >
                  Удалить
                </button>
              </div>
            </article>
          </div>
        </div>

        <div v-if="listings.archived.length" class="dashboard-subsection">
          <h3>Архив</h3>
          <div class="listings-grid muted">
            <article
              v-for="item in listings.archived"
              :key="item.id"
              class="listing-card"
              @click="openListing(item.id)"
            >
              <div class="listing-card__title">{{ item.title }}</div>
              <p class="listing-card__description">{{ item.description || 'Без описания' }}</p>
              <div class="listing-card__meta">
                <span>{{ formatPrice(item.pricePerHour) }}</span>
                <span class="chip ghost">{{ item.status }}</span>
              </div>
              <div class="listing-card__actions">
                <button class="btn secondary" type="button" @click="editListing(item.id, $event)">Редактировать</button>
                <button class="btn secondary" type="button" @click="unarchiveListing(item.id, $event)">Разархивировать</button>
                <button class="btn secondary" type="button" @click="deleteListing(item.id, $event)">Удалить</button>
              </div>
            </article>
          </div>
        </div>

        <p v-if="!listings.active.length && !listings.archived.length" class="dashboard-note">У вас пока нет объявлений.</p>
      </div>
    </section>

    <section class="dashboard-section">
      <h2>Календарь занятости</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите, чтобы увидеть занятость.</p>
      <p v-else-if="rentalsLoading" class="dashboard-note">Загружаем аренды...</p>
      <p v-else-if="rentalsError" class="dashboard-note error">{{ rentalsError }}</p>
      <div v-else-if="ownerRentals.length" class="rental-groups">
        <details v-for="rental in ownerRentals" :key="rental.rentalId" class="rental-item" open>
          <summary>
            <span class="rental-title">{{ rental.listingTitle || 'Объявление' }}</span>
            <span class="chip">{{ rental.status }}</span>
          </summary>
          <div class="rental-body">
            <div>Арендатор: {{ rental.lesseeName }} <span v-if="rental.lesseeUsername">(@{{ rental.lesseeUsername }})</span></div>
            <div>Период: {{ formatDate(rental.startAt) }} — {{ formatDate(rental.endAt) }}</div>
            <div>Создано: {{ formatDate(rental.createdAt) }}</div>
            <button
              v-if="rental.status === 'PENDING'"
              class="btn secondary"
              type="button"
              @click="confirmRental(rental.rentalId, $event)"
            >
              Подтвердить
            </button>
          </div>
        </details>
      </div>
      <p v-else class="dashboard-note">Пока нет незавершённых аренд.</p>
    </section>
  </div>
</template>
