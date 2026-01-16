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

const formatPrice = (price) => (price ? `${price} ₽/день` : 'Договорная');
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
const goToAuth = (tab) => {
  if (tab === 'register') {
    router.push('/register');
  } else {
    router.push('/login');
  }
};
const logoutAndRedirect = () => { logout(); router.push('/login'); };

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
</template>
