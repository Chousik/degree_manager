<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { getCategories } from '../api/categories';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const router = useRouter();
const { isLoggedIn } = useSession();

const categories = ref([]);
const categoriesLoading = ref(true);
const categoriesError = ref('');

const products = ref([]);
const productsLoading = ref(true);
const productsError = ref('');

const goToLogin = () => router.push('/login');
const goToCatalog = () => router.push('/catalog');

onMounted(() => {
  if (isLoggedIn.value) {
    loadCategories();
    loadProducts();
  }
});

watch(() => isLoggedIn.value, (logged) => {
  if (logged) {
    loadCategories();
    loadProducts();
  } else {
    resetCategories();
    resetProducts();
  }
});

function resetCategories() {
  categories.value = [];
  categoriesLoading.value = false;
  categoriesError.value = '';
}

function resetProducts() {
  products.value = [];
  productsLoading.value = false;
  productsError.value = '';
}

async function loadCategories() {
  if (!isLoggedIn.value) {
    resetCategories();
    return;
  }
  categoriesLoading.value = true;
  categoriesError.value = '';
  try {
    const data = await getCategories();
    categories.value = Array.isArray(data) ? data.slice(0, 10) : [];
  } catch (error) {
    categoriesError.value = 'Что-то пошло не так при загрузке категорий. Попробуйте обновить страницу.';
  } finally {
    categoriesLoading.value = false;
  }
}

async function loadProducts() {
  if (!isLoggedIn.value) {
    resetProducts();
    return;
  }
  productsLoading.value = true;
  productsError.value = '';
  try {
    const data = await fetchJson(`${USER_API_BASE}/listings?size=8`, { auth: false });
    const content = Array.isArray(data?.content) ? data.content : [];
    products.value = content.slice(0, 8);
  } catch (error) {
    productsError.value = 'Не удалось загрузить товары. Попробуйте позже.';
  } finally {
    productsLoading.value = false;
  }
}

function filterByCategory(categoryId) {
  router.push({
    path: '/catalog',
    query: categoryId ? { category: categoryId } : undefined,
  });
}

function formatPrice(value) {
  if (!value && value !== 0) {
    return 'Цена по запросу';
  }
  const formatted = Number(value).toLocaleString('ru-RU');
  return `${formatted} ₽/ч`;
}
</script>

<template>
  <div class="landing">
    <MainHeader />

    <div class="landing-actions">
      <div class="landing-buttons">
        <button type="button" class="landing-btn primary" @click="goToCatalog">Все категории</button>
      </div>
      <div class="landing-search">
        <input type="text" placeholder="Найдите инструмент, услугу или продавца">
        <button type="button">Найти</button>
      </div>
    </div>

    <section class="landing-section">
      <div class="landing-section__title">Категории</div>
      <p v-if="!isLoggedIn" class="landing-note">Авторизуйтесь, чтобы увидеть категории.</p>
      <p v-else-if="categoriesError" class="landing-note error">{{ categoriesError }}</p>
      <p v-else-if="categoriesLoading" class="landing-note">Загружаем категории...</p>
      <p v-else-if="!categories.length" class="landing-note">Категории скоро появятся.</p>

      <div v-else class="landing-grid categories">
        <button
          v-for="category in categories"
          :key="category.id"
          class="landing-card category category-button"
          type="button"
          @click="filterByCategory(category.id)"
        >
          <span class="landing-card__category-name">{{ category.name }}</span>
        </button>
      </div>
    </section>

    <section class="landing-section">
      <div class="landing-section__title">Для вас</div>
      <p v-if="!isLoggedIn" class="landing-note">Авторизуйтесь, чтобы увидеть предложения.</p>
      <p v-else-if="productsError" class="landing-note error">{{ productsError }}</p>
      <p v-else-if="productsLoading" class="landing-note">Загружаем предложения...</p>
      <p v-else-if="!products.length" class="landing-note">Подходящих предложений пока нет.</p>

      <div v-else class="landing-grid products">
        <RouterLink
          v-for="item in products"
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

    <div class="landing-cta" v-if="!isLoggedIn">
      <div class="landing-cta__buttons">
        <button class="landing-btn primary" type="button" @click="goToLogin">Войти</button>
        <button class="landing-btn ghost" type="button" @click="router.push('/register')">Зарегистрироваться</button>
      </div>
    </div>
  </div>
</template>
