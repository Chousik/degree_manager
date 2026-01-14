<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import MainHeader from '../components/MainHeader.vue';
import { getFavorites } from '../api/favorites';
import { useSession } from '../state/session';

const { isLoggedIn, userId } = useSession();

const favorites = ref([]);
const loading = ref(false);
const error = ref('');

const favoriteCards = computed(() =>
  favorites.value.map((item) => ({
    id: item.id,
    title: item.title || 'Без названия',
    description: item.description || 'Без описания',
    pricePerHour: item.pricePerHour ?? 0,
    depositAmount: item.depositAmount,
    status: item.status || '',
  }))
);

async function loadFavorites() {
  if (!isLoggedIn.value || !userId.value) {
    favorites.value = [];
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const data = await getFavorites(userId.value);
    favorites.value = Array.isArray(data) ? data : [];
  } catch (err) {
    error.value = 'Не удалось загрузить избранные объявления. Попробуйте обновить страницу.';
    favorites.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadFavorites();
});

watch(
  [() => userId.value, () => isLoggedIn.value],
  () => {
    loadFavorites();
  }
);
</script>

<template>
  <div class="favorites-page">
    <MainHeader />
    <section class="favorites-section">
      <div class="favorites-header">
        <div class="favorites-title">Избранные</div>
      </div>

      <p v-if="!isLoggedIn" class="favorites-empty">
        Авторизуйтесь, чтобы увидеть избранные объявления.
      </p>
      <p v-else-if="loading" class="favorites-empty">
        Загружаем избранное...
      </p>
      <p v-else-if="error" class="favorites-empty">
        {{ error }}
      </p>

      <div v-else-if="favoriteCards.length" class="favorites-list">
        <article v-for="item in favoriteCards" :key="item.id" class="favorites-card">
          <div class="favorites-card__title">{{ item.title }}</div>
          <p>
            {{ item.pricePerHour }} ₽/день ·
            <span v-if="item.depositAmount">Залог {{ item.depositAmount }} ₽</span>
            <span v-else>Без залога</span>
            <span v-if="item.status"> · Статус: {{ item.status }}</span>
          </p>
          <p>{{ item.description }}</p>
        </article>
      </div>
      <div v-else class="favorites-empty">
        У вас пока нет избранных объявлений
      </div>
    </section>
  </div>
</template>
