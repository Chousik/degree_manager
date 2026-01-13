<script setup>
import { onMounted, ref, watch } from 'vue';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const { isLoggedIn, userId } = useSession();
const listings = ref({ active: [], archived: [] });
const loading = ref(false);
const error = ref('');

async function loadMyListings() {
  if (!isLoggedIn.value) {
    listings.value = { active: [], archived: [] };
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

onMounted(() => {
  loadMyListings();
});

watch(
  [() => isLoggedIn.value, () => userId.value],
  () => {
    loadMyListings();
  }
);

function formatPrice(value) {
  if (!value && value !== 0) {
    return 'Цена по запросу';
  }
  const formatted = Number(value).toLocaleString('ru-RU');
  return `${formatted} ₽/ч`;
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
            >
              <div class="listing-card__title">{{ item.title }}</div>
              <p class="listing-card__description">{{ item.description || 'Без описания' }}</p>
              <div class="listing-card__meta">
                <span>{{ formatPrice(item.pricePerHour) }}</span>
                <span class="chip">{{ item.status }}</span>
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
            >
              <div class="listing-card__title">{{ item.title }}</div>
              <p class="listing-card__description">{{ item.description || 'Без описания' }}</p>
              <div class="listing-card__meta">
                <span>{{ formatPrice(item.pricePerHour) }}</span>
                <span class="chip ghost">{{ item.status }}</span>
              </div>
            </article>
          </div>
        </div>

        <p v-if="!listings.active.length && !listings.archived.length" class="dashboard-note">У вас пока нет объявлений.</p>
      </div>
    </section>
  </div>
</template>
