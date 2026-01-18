<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { getCategories } from '../api/categories';
import { USER_API_BASE, fetchJson } from '../api/client';
import { addFavorite, getFavorites, removeFavorite } from '../api/favorites';
import { useSession } from '../state/session';

const router = useRouter();
const route = useRoute();
const { isLoggedIn, userId } = useSession();

const categories = ref([]);
const categoriesLoading = ref(true);
const categoriesError = ref('');

const searchQuery = ref(route.query.q || '');
const searchResults = ref([]);
const totalResults = ref(0);
const searchLoading = ref(false);
const searchError = ref('');

const activeCategory = ref(route.query.category || '');
const minPrice = ref(route.query.minPrice || '');
const maxPrice = ref(route.query.maxPrice || '');
const availableFrom = ref(route.query.availableFrom ? route.query.availableFrom.slice(0, 10) : '');
const availableTo = ref(route.query.availableTo ? route.query.availableTo.slice(0, 10) : '');
const mapView = ref(route.query.view === 'map');
const mapPoints = ref([]);
const favorites = ref(new Set());
const favoritesLoading = ref(false);
const mapContainer = ref(null);
const mapLoading = ref(false);
const mapError = ref('');
let mapInstance = null;
let yandexLoaderPromise = null;
let mapMarkers = [];

const yandexApiKey = import.meta.env.VITE_YANDEX_MAPS_API_KEY || '995b7ac5-4b58-4894-953c-3e8531a4e52c';

const mapFallbackList = computed(() =>
  mapPoints.value.filter((p) => !Number.isFinite(p.latitude) || !Number.isFinite(p.longitude))
);

const loadYandexMaps = () => {
  if (window.ymaps3 && window.ymaps3.ready) {
    return window.ymaps3.ready;
  }
  if (yandexLoaderPromise) {
    return yandexLoaderPromise;
  }
  yandexLoaderPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    const params = new URLSearchParams({
      apikey: yandexApiKey,
      lang: 'ru_RU',
    });
    script.src = `https://api-maps.yandex.ru/v3/?${params.toString()}`;
    script.async = true;
    script.onload = () => {
      if (window.ymaps3 && window.ymaps3.ready) {
        window.ymaps3.ready.then(resolve).catch(reject);
      } else {
        reject(new Error('Yandex Maps API недоступна'));
      }
    };
    script.onerror = () => reject(new Error('Не удалось загрузить Яндекс Карты'));
    document.head.appendChild(script);
  });
  return yandexLoaderPromise;
};

const preloadYandexMaps = async () => {
  try {
    await loadYandexMaps();
  } catch (err) {
    mapError.value = err?.message || 'Не удалось загрузить Яндекс Карты.';
  }
};

const ensureMap = async () => {
  if (!mapView.value) {
    return;
  }
  if (!mapContainer.value) {
    await nextTick();
  }
  if (!mapContainer.value || mapInstance) {
    return;
  }
  mapLoading.value = true;
  mapError.value = '';
  try {
    await loadYandexMaps();
    mapContainer.value.style.width = '100%';
    mapContainer.value.style.height = '100%';
    const { YMap, YMapDefaultSchemeLayer, YMapDefaultFeaturesLayer } = window.ymaps3;
    mapInstance = new YMap(mapContainer.value, {
      location: {
        center: [55.751244, 37.618423],
        zoom: 10,
      },
    });
    mapInstance.addChild(new YMapDefaultSchemeLayer());
    mapInstance.addChild(new YMapDefaultFeaturesLayer());
  } catch (err) {
    mapError.value = err?.message || 'Не удалось инициализировать карту.';
  } finally {
    mapLoading.value = false;
  }
};

const updateMapMarkers = () => {
  if (!mapInstance || !window.ymaps3) {
    return;
  }
  if (mapMarkers.length) {
    mapMarkers.forEach((marker) => mapInstance.removeChild(marker));
    mapMarkers = [];
  }
  const points = mapPoints.value.filter((p) => Number.isFinite(p.latitude) && Number.isFinite(p.longitude));
  if (!points.length) {
    return;
  }
  const { YMapMarker } = window.ymaps3;
  points.forEach((point, index) => {
    const id = point.listingId || point.id;
    if (!id) return;
    const markerEl = document.createElement('button');
    markerEl.type = 'button';
    markerEl.className = 'map-price-marker';
    markerEl.textContent = formatPrice(point.pricePerHour);
    markerEl.addEventListener('click', () => router.push(`/catalog/${id}`));
    const marker = new YMapMarker(
      { coordinates: [Number(point.longitude), Number(point.latitude)] },
      markerEl
    );
    mapInstance.addChild(marker);
    mapMarkers.push(marker);
    if (index === 0) {
      mapInstance.setLocation?.({
        center: [Number(point.longitude), Number(point.latitude)],
        zoom: 11,
      });
    }
  });
};

function formatPrice(value) {
  if (!value && value !== 0) {
    return 'Цена по запросу';
  }
  return `${Number(value).toLocaleString('ru-RU')} ₽/день`;
}

const isFavorite = (listingId) => favorites.value.has(listingId);

const toggleFavorite = async (listingId) => {
  if (!isLoggedIn.value || !userId.value) {
    router.push('/login');
    return;
  }
  if (!listingId) return;
  const next = new Set(favorites.value);
  try {
    if (next.has(listingId)) {
      await removeFavorite(userId.value, listingId);
      next.delete(listingId);
    } else {
      await addFavorite(userId.value, listingId);
      next.add(listingId);
    }
    favorites.value = next;
  } catch (err) {
    // ignore; keep current state
  }
};

const buildIsoDate = (value) => {
  if (!value) return '';
  const date = new Date(`${value}T00:00:00`);
  if (Number.isNaN(date.getTime())) return '';
  const pad = (num) => String(num).padStart(2, '0');
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  const seconds = pad(date.getSeconds());
  const offsetMinutes = -date.getTimezoneOffset();
  const sign = offsetMinutes >= 0 ? '+' : '-';
  const absOffset = Math.abs(offsetMinutes);
  const offsetHours = pad(Math.floor(absOffset / 60));
  const offsetMins = pad(absOffset % 60);
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}${sign}${offsetHours}:${offsetMins}`;
};

const buildSearchParams = () => {
  const params = new URLSearchParams({ size: '12' });
  if (searchQuery.value) params.set('text', searchQuery.value);
  if (activeCategory.value) params.set('categoryId', activeCategory.value);
  if (minPrice.value && Number.isFinite(Number(minPrice.value))) params.set('minPrice', Number(minPrice.value));
  if (maxPrice.value && Number.isFinite(Number(maxPrice.value))) params.set('maxPrice', Number(maxPrice.value));
  if (availableFrom.value) params.set('availableFrom', buildIsoDate(availableFrom.value));
  if (availableTo.value) params.set('availableTo', buildIsoDate(availableTo.value));
  return params;
};

const submitSearch = async () => {
  router.push({
    path: '/search',
    query: {
      ...(searchQuery.value ? { q: searchQuery.value } : {}),
      ...(activeCategory.value ? { category: activeCategory.value } : {}),
      ...(minPrice.value ? { minPrice: minPrice.value } : {}),
      ...(maxPrice.value ? { maxPrice: maxPrice.value } : {}),
      ...(availableFrom.value ? { availableFrom: availableFrom.value } : {}),
      ...(availableTo.value ? { availableTo: availableTo.value } : {}),
      ...(mapView.value ? { view: 'map' } : {}),
    },
  });
};

const clearFilters = () => {
  activeCategory.value = '';
  minPrice.value = '';
  maxPrice.value = '';
  availableFrom.value = '';
  availableTo.value = '';
  submitSearch();
};

async function loadCategories() {
  categoriesLoading.value = true;
  categoriesError.value = '';
  try {
    const data = await getCategories();
    categories.value = Array.isArray(data) ? data : [];
  } catch (err) {
    categoriesError.value = 'Не удалось загрузить категории';
  } finally {
    categoriesLoading.value = false;
  }
}

async function loadFavorites() {
  if (!isLoggedIn.value || !userId.value) {
    favorites.value = new Set();
    return;
  }
  favoritesLoading.value = true;
  try {
    const data = await getFavorites(userId.value);
    const ids = Array.isArray(data) ? data.map((item) => item.listingId || item.id) : [];
    favorites.value = new Set(ids.filter(Boolean));
  } catch (err) {
    favorites.value = new Set();
  } finally {
    favoritesLoading.value = false;
  }
}

async function performSearch() {
  searchLoading.value = true;
  searchError.value = '';
  try {
    const params = buildSearchParams();
    const data = await fetchJson(`${USER_API_BASE}/listings?${params.toString()}`, { auth: false });
    searchResults.value = Array.isArray(data?.content) ? data.content : [];
    totalResults.value = Number.isFinite(data?.totalElements)
      ? Number(data.totalElements)
      : searchResults.value.length;
    if (mapView.value) {
      await fetchMapPoints(params);
      await ensureMap();
    }
  } catch (err) {
    searchError.value = err?.message || 'Не удалось загрузить объявления';
    searchResults.value = [];
    totalResults.value = 0;
    mapPoints.value = [];
  } finally {
    searchLoading.value = false;
  }
}

async function fetchMapPoints(params) {
  try {
    const mapData = await fetchJson(`${USER_API_BASE}/listings/map?${params.toString()}`, { auth: false });
    mapPoints.value = Array.isArray(mapData)
      ? mapData.map((point) => ({
        ...point,
        latitude: Number(point.latitude),
        longitude: Number(point.longitude),
      }))
      : [];
  } catch (err) {
    mapPoints.value = [];
  }
}

onMounted(() => {
  loadCategories();
  loadFavorites();
  performSearch();
  preloadYandexMaps();
  if (mapView.value) {
    ensureMap();
  }
});

watch(
  () => route.fullPath,
  () => {
    searchQuery.value = route.query.q || '';
    activeCategory.value = route.query.category || '';
    minPrice.value = route.query.minPrice || '';
    maxPrice.value = route.query.maxPrice || '';
    availableFrom.value = route.query.availableFrom ? String(route.query.availableFrom).slice(0, 10) : '';
    availableTo.value = route.query.availableTo ? String(route.query.availableTo).slice(0, 10) : '';
    mapView.value = route.query.view === 'map';
    performSearch();
  }
);

watch(mapView, (next) => {
  if (next) {
    const params = buildSearchParams();
    fetchMapPoints(params);
    ensureMap();
  } else if (mapInstance) {
    mapInstance.destroy?.();
    mapInstance = null;
    mapMarkers = [];
  }
});

watch(mapPoints, () => {
  if (mapView.value) {
    updateMapMarkers();
  }
});
</script>

<template>
  <div class="search-shell">
    <MainHeader />

    <section class="search-hero">
      <div>
        <h1>Найдите инструмент за минуту</h1>
        <p>Поиск по названию, ключевым словам, описанию и карте.</p>
      </div>
      <div class="search-toggle">
        <button type="button" class="btn secondary" :class="{ active: !mapView }" @click="mapView = false">Список</button>
        <button type="button" class="btn secondary" :class="{ active: mapView }" @click="mapView = true">Карта</button>
      </div>
    </section>

    <section class="search-panel">
      <div class="filter-card">
        <div class="filter-header">
          <h3>Фильтры</h3>
        </div>
        <div class="filter-section">
          <div class="filter-section__title">Быстрый поиск</div>
          <label>
            Поиск
            <input v-model="searchQuery" type="text" placeholder="Название, ключевые слова, описание" @keyup.enter="submitSearch">
          </label>
        </div>
        <div class="filter-section">
          <div class="filter-section__title">Категория и цена</div>
          <label>
            Категория
            <select v-model="activeCategory">
              <option value="">Все категории</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </label>
          <div class="filter-grid">
            <label>
              Цена от
              <input v-model="minPrice" type="number" min="0" step="1">
            </label>
            <label>
              Цена до
              <input v-model="maxPrice" type="number" min="0" step="1">
            </label>
          </div>
        </div>
        <div class="filter-section">
          <div class="filter-section__title">Доступность</div>
          <div class="filter-grid">
            <label>
              Дата начала
              <input v-model="availableFrom" type="date">
            </label>
            <label>
              Дата возврата
              <input v-model="availableTo" type="date">
            </label>
          </div>
        </div>
        <div class="filter-actions">
          <button type="button" class="btn primary" @click="submitSearch">Искать</button>
          <button type="button" class="btn secondary" @click="clearFilters">Очистить</button>
        </div>
      </div>

      <div class="results-card">
        <div class="results-header">
          <div>
            <div class="results-title">Результаты</div>
            <p class="muted small">Поиск по названию, описанию и ключевым словам.</p>
            <div v-if="!searchLoading && !searchError" class="results-count">Найдено: {{ totalResults }}</div>
          </div>
          <div v-if="favoritesLoading" class="muted small">Обновляем избранное...</div>
        </div>
        <p v-if="categoriesError" class="landing-note error">{{ categoriesError }}</p>
        <p v-else-if="searchLoading" class="landing-note">Обновляем результаты...</p>
        <p v-else-if="searchError" class="landing-note error">{{ searchError }}</p>
        <p v-else-if="!searchResults.length" class="landing-note">Пока ничего не найдено.</p>

        <div v-else-if="!mapView" class="results-grid">
          <article v-for="item in searchResults" :key="item.id" class="result-card">
            <button class="favorite-btn" type="button" :class="{ active: isFavorite(item.id) }" @click="toggleFavorite(item.id)">
              ♥
            </button>
            <div
              class="result-image"
              :style="{ backgroundImage: `url('${item.previewPhotoUrl || '/media/basket.svg'}')` }"
            ></div>
            <div class="result-body">
              <div class="result-title">{{ item.title }}</div>
              <div class="result-price">{{ formatPrice(item.pricePerHour) }}</div>
              <button type="button" class="link" @click="router.push(`/catalog/${item.id}`)">Открыть объявление</button>
            </div>
          </article>
        </div>

        <div v-else class="map-view">
          <div class="map-frame">
            <div ref="mapContainer" class="map-yandex"></div>
            <div v-if="mapLoading" class="map-empty">Загружаем карту...</div>
            <div v-else-if="mapError" class="map-empty error">{{ mapError }}</div>
          </div>
          <div v-if="mapFallbackList.length" class="map-fallback">
            <p class="muted small">Без координат:</p>
            <div class="map-fallback__list">
              <span v-for="item in mapFallbackList" :key="item.id || item.listingId">{{ item.title }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>
