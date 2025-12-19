<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const router = useRouter();
const { isLoggedIn, logout } = useSession();

const filters = reactive({
  text: '',
  categoryId: '',
  minPrice: '',
  maxPrice: '',
});

const listings = ref([]);
const loading = ref(false);
const hasMore = ref(true);
const page = ref(0);
const pageSize = 12;
const sortOption = ref('relevance');
const errorMessage = ref('');
const showDrawer = ref(false);
const sentinel = ref(null);
let observer = null;

const heroTitle = computed(() =>
  filters.text ? `Результаты по запросу «${filters.text}»` : 'Каталог аренды');
const heroSubtitle = computed(() =>
  listings.value.length
    ? `Нашли ${listings.value.length} предложений, листайте ещё`
    : 'Инструменты, аренда и сервис - бесконечная лента карточек');

const formatPrice = (price) => (price ? `${price} ₽/ч` : 'Договорная');
const formatDeposit = (value) => (value ? `Депозит ${value} ₽` : 'Без депозита');

const mapListings = (data) => {
  if (Array.isArray(data?.content)) return data.content;
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.list)) return data.list;
  return [];
};

const fetchPage = async (reset = false) => {
  if (loading.value || (!hasMore.value && !reset)) return;
  if (reset) {
    page.value = 0;
    listings.value = [];
    hasMore.value = true;
  }

  loading.value = true;
  errorMessage.value = '';

  const params = new URLSearchParams();
  params.set('page', page.value.toString());
  params.set('size', pageSize.toString());
  Object.entries(filters).forEach(([key, val]) => {
    if (val) params.append(key, val);
  });
  if (sortOption.value === 'priceAsc') params.append('sort', 'pricePerHour,asc');
  if (sortOption.value === 'priceDesc') params.append('sort', 'pricePerHour,desc');

  try {
    const data = await fetchJson(`${USER_API_BASE}/listings?${params.toString()}`);
    const nextItems = mapListings(data);
    listings.value = reset ? nextItems : listings.value.concat(nextItems);

    const totalPages = data?.totalPages ?? data?.pageable?.totalPages ?? null;
    const isLast = data?.last ?? (totalPages ? page.value >= totalPages - 1 : nextItems.length < pageSize);
    hasMore.value = !isLast && nextItems.length > 0;
    if (nextItems.length > 0) page.value += 1;
  } catch (err) {
    errorMessage.value = err.message || 'Не удалось загрузить объявления';
    hasMore.value = false;
  } finally {
    loading.value = false;
  }
};

const resetAndSearch = () => fetchPage(true);
const clearFilters = () => {
  filters.text = '';
  filters.categoryId = '';
  filters.minPrice = '';
  filters.maxPrice = '';
  resetAndSearch();
};

const toggleDrawer = () => { showDrawer.value = !showDrawer.value; };
const goToAuth = (tab) => router.push({ path: '/auth', query: tab ? { tab } : {} });
const logoutAndRedirect = () => { logout(); router.push('/auth'); };

onMounted(() => {
  fetchPage(true);
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) fetchPage();
    },
    { rootMargin: '320px' }
  );
  if (sentinel.value) observer.observe(sentinel.value);
});

onBeforeUnmount(() => observer?.disconnect());
</script>

<template>
  <div class="catalog-shell">
    <div class="search-hero">
      <div class="hero-header">
        <button class="icon-button" type="button" @click="toggleDrawer">☰</button>
        <div>
          <p class="eyebrow">Fixly Marketplace</p>
          <h1 class="hero-title">{{ heroTitle }}</h1>
          <p class="hero-subtitle">{{ heroSubtitle }}</p>
        </div>
      </div>
      <div class="search-stack">
        <div class="search-bar">
          <input
            v-model="filters.text"
            type="search"
            placeholder="Поиск по названию, бренду или городу"
            @keydown.enter.prevent="resetAndSearch"
          >
          <select v-model="sortOption" @change="resetAndSearch">
            <option value="relevance">По релевантности</option>
            <option value="priceAsc">Цена: по возрастанию</option>
            <option value="priceDesc">Цена: по убыванию</option>
          </select>
          <button class="btn primary" type="button" @click="resetAndSearch">
            Искать
          </button>
        </div>
        <div class="quick-row">
          <button class="pill" type="button" @click="filters.text = 'аренда'; resetAndSearch();">Аренда без залога</button>
          <button class="pill" type="button" @click="filters.text = 'пил'; resetAndSearch();">Электроинструменты</button>
          <button class="pill" type="button" @click="filters.text = 'бетон'; resetAndSearch();">Стройка</button>
          <button class="pill ghost" type="button" @click="clearFilters">Сбросить</button>
        </div>
      </div>
    </div>

    <div class="catalog-grid">
      <aside :class="['catalog-drawer', { open: showDrawer }]">
        <div class="drawer-head">
          <div>
            <p class="eyebrow muted">Управление</p>
            <div class="drawer-title">
              {{ isLoggedIn ? 'Личный кабинет' : 'Гость' }}
            </div>
            <p class="muted small">
              {{ isLoggedIn ? 'Сохраняйте избранное и арендуйте быстрее' : 'Войдите, чтобы бронировать и общаться' }}
            </p>
          </div>
          <button class="icon-button ghost" type="button" @click="toggleDrawer">X</button>
        </div>

        <div class="drawer-actions">
          <button
            v-if="isLoggedIn"
            class="nav-link"
            type="button"
            @click="router.push('/account')"
          >
            Личный кабинет
          </button>
          <button
            v-if="isLoggedIn"
            class="nav-link logout"
            type="button"
            @click="logoutAndRedirect"
          >
            Sign out
          </button>
          <template v-else>
            <button class="nav-link" type="button" @click="goToAuth()">Sign in</button>
            <button class="nav-link primary-ghost" type="button" @click="goToAuth('register')">Create account</button>
          </template>
        </div>

        <div class="drawer-links">
          <RouterLink class="drawer-link" to="/listings">Мои объявления</RouterLink>
          <RouterLink class="drawer-link" to="/favorites">Избранное</RouterLink>
          <RouterLink class="drawer-link" to="/rentals">Аренды</RouterLink>
          <RouterLink class="drawer-link" to="/support">Поддержка</RouterLink>
        </div>
      </aside>

      <section class="catalog-main">
        <div class="filter-card">
          <div class="filter-grid">
            <div class="field">
              <label>Категория UUID</label>
              <input v-model="filters.categoryId" placeholder="опционально">
            </div>
            <div class="field">
              <label>Мин. цена</label>
              <input v-model="filters.minPrice" type="number" min="0" step="0.01" placeholder="0">
            </div>
            <div class="field">
              <label>Макс. цена</label>
              <input v-model="filters.maxPrice" type="number" min="0" step="0.01" placeholder="5000">
            </div>
          </div>
          <div class="filter-actions">
            <button class="btn primary" type="button" @click="resetAndSearch">Применить фильтры</button>
            <button class="btn" type="button" @click="clearFilters">Сбросить</button>
          </div>
        </div>

        <div class="results-header">
          <div>
            <p class="muted small">Показываем предложения маркетплейса</p>
            <div class="result-title">Лента объявлений</div>
          </div>
          <div class="muted small" v-if="listings.length">Уже подгружено {{ listings.length }}</div>
        </div>

        <div class="listing-feed">
          <article v-for="card in listings" :key="card.id || card.title" class="listing-card">
            <div class="card-badge">В тренде</div>
            <div class="card-title-row">
              <RouterLink
                :to="card.id ? `/catalog/${card.id}` : '/catalog'"
                class="card-link"
              >
                <h3>{{ card.title || 'Без названия' }}</h3>
              </RouterLink>
              <span class="chip">{{ formatPrice(card.pricePerHour) }}</span>
            </div>
            <p class="card-text">{{ card.description || 'Описание появится позже' }}</p>
            <div class="card-meta">
              <span>{{ formatDeposit(card.depositAmount) }}</span>
              <span>{{ card.autoConfirmation ? 'Мгновенное бронирование' : 'Ручное подтверждение' }}</span>
            </div>
            <div class="card-footer">
              <RouterLink
                class="pill ghost"
                :to="card.id ? `/catalog/${card.id}` : '/catalog'"
              >
                Смотреть детали
              </RouterLink>
              <span class="muted small">ID {{ card.id || '-' }}</span>
            </div>
          </article>

          <div v-if="loading" class="skeleton-grid">
            <div v-for="n in 6" :key="n" class="skeleton-card"></div>
          </div>

          <div v-if="!loading && !listings.length" class="empty-state">
            <p class="result-title">Ничего не найдено</p>
            <p class="muted">Попробуйте другой запрос или уберите фильтры</p>
          </div>

          <div v-if="errorMessage" class="toast error">{{ errorMessage }}</div>
          <div v-if="!hasMore && listings.length" class="endcap">Листинг закончился - вы видели всё</div>
          <div ref="sentinel" class="feed-sentinel"></div>
        </div>
      </section>
    </div>
  </div>
</template>
