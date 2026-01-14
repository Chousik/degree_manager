<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { getCategories } from '../api/categories';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const router = useRouter();
const route = useRoute();
const { isLoggedIn } = useSession();

const categories = ref([]);
const categoriesLoading = ref(true);
const categoriesError = ref('');

const searchQuery = ref(route.query.q || '');
const searchResults = ref([]);
const searchLoading = ref(false);
const searchError = ref('');

const activeCategory = ref(route.query.category || null);

function formatPrice(value) {
  if (!value && value !== 0) {
    return 'Цена по запросу';
  }
  return `${Number(value).toLocaleString('ru-RU')} ₽/ч`;
}

function submitSearch() {
  router.push({
    path: '/search',
    query: {
      ...(searchQuery.value ? { q: searchQuery.value } : {}),
      ...(activeCategory.value ? { category: activeCategory.value } : {}),
    },
  });
}

function filterByCategory(categoryId) {
  activeCategory.value = categoryId ? String(categoryId) : null;
  submitSearch();
}

async function loadCategories() {
  categoriesLoading.value = true;
  categoriesError.value = '';
  try {
    const data = await getCategories();
    categories.value = Array.isArray(data) ? data.slice(0, 10) : [];
  } catch (err) {
    categoriesError.value = 'Не удалось загрузить категории';
  } finally {
    categoriesLoading.value = false;
  }
}

async function performSearch() {
  searchLoading.value = true;
  searchError.value = '';
  try {
    const params = new URLSearchParams({ size: '12' });
    if (searchQuery.value) {
      params.set('text', searchQuery.value);
    }
    if (activeCategory.value) {
      params.set('categoryId', activeCategory.value);
    }
    const data = await fetchJson(`${USER_API_BASE}/listings?${params.toString()}`, { auth: false });
    searchResults.value = Array.isArray(data?.content) ? data.content : [];
  } catch (err) {
    searchError.value = err?.message || 'Не удалось загрузить объявления';
    searchResults.value = [];
  } finally {
    searchLoading.value = false;
  }
}

onMounted(() => {
  loadCategories();
  performSearch();
});

watch(
  () => route.fullPath,
  () => {
    searchQuery.value = route.query.q || '';
    activeCategory.value = route.query.category || null;
    performSearch();
  }
);
</script>

<template>
  <div class="landing-page">
    <MainHeader />

    <div class="landing-hero">
      <h1>Поиск объявлений</h1>
      <p>Найдите нужный инструмент или услугу рядом с вами.</p>
    </div>

    <div class="landing-actions">
      <div class="landing-buttons">
        <button class="landing-btn primary" type="button" @click="submitSearch">Поиск</button>
      </div>
      <div class="landing-search">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Введите название инструмента или услуги"
          @keyup.enter="submitSearch"
        >
        <button type="button" @click="submitSearch">Найти</button>
      </div>
    </div>

    <section class="landing-section">
      <div class="landing-section__title">Категории</div>
      <p v-if="categoriesError" class="landing-note error">{{ categoriesError }}</p>
      <p v-else-if="categoriesLoading" class="landing-note">Загружаем категории...</p>
      <div v-else class="landing-grid categories">
        <button
          v-for="category in categories"
          :key="category.id"
          class="landing-card category category-button"
          type="button"
          :class="{ active: activeCategory === String(category.id) }"
          @click="filterByCategory(category.id)"
        >
          <span class="landing-card__category-name">{{ category.name }}</span>
        </button>
      </div>
    </section>

    <section class="landing-section">
      <div class="landing-section__title">Результаты поиска</div>
      <p v-if="searchError" class="landing-note error">{{ searchError }}</p>
      <p v-else-if="searchLoading" class="landing-note">Обновляем результаты...</p>
      <p v-else-if="!searchResults.length" class="landing-note">Пока ничего не найдено.</p>
      <div v-else class="landing-grid products">
        <RouterLink
          v-for="item in searchResults"
          :key="item.id"
          class="landing-card product product-link"
          :to="item.id ? `/catalog/${item.id}` : '/catalog'"
        >
          <div
            class="landing-card__product-image"
            :style="{ backgroundImage: `url('${item.previewPhotoUrl || '/media/basket.svg'}')` }"
          ></div>
          <div class="landing-card__product-title">{{ item.title }}</div>
          <div class="landing-card__product-price">{{ formatPrice(item.pricePerHour) }}</div>
        </RouterLink>
      </div>
    </section>
  </div>
</template>
