<script setup>
import { computed, reactive } from 'vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const { userId } = useSession();

const listingSearch = reactive({
  text: '',
  categoryId: '',
  minPrice: '',
  maxPrice: '',
});

const listingCreate = reactive({
  ownerId: '',
  title: '',
  description: '',
  pricePerHour: '',
  depositAmount: '',
  autoConfirmation: true,
  latitude: '',
  longitude: '',
});

const toast = reactive({ message: '', type: 'success', visible: false });
const apiResults = reactive({ listings: null, create: null, delete: null });

const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};

const toJson = (val) => (val ? JSON.stringify(val, null, 2) : '');
const listingCards = computed(() => {
  if (!apiResults.listings) return [];
  return Array.isArray(apiResults.listings.content)
    ? apiResults.listings.content
    : Array.isArray(apiResults.listings)
      ? apiResults.listings
      : [];
});

const searchListings = async () => {
  const params = new URLSearchParams();
  Object.entries(listingSearch).forEach(([k, v]) => v && params.append(k, v));
  apiResults.listings = await fetchJson(`${USER_API_BASE}/listings?${params.toString()}`);
};

const ensureUserId = () => {
  if (!userId.value) {
    showToast('В токене нет userId/sub — войдите заново', 'error');
    return false;
  }
  return true;
};

const createListing = async () => {
  if (!ensureUserId()) return;
  const payload = {
    ownerId: userId.value,
    title: listingCreate.title,
    description: listingCreate.description,
    pricePerHour: listingCreate.pricePerHour ? Number(listingCreate.pricePerHour) : null,
    depositAmount: listingCreate.depositAmount ? Number(listingCreate.depositAmount) : 0,
    autoConfirmation: listingCreate.autoConfirmation,
    latitude: listingCreate.latitude ? Number(listingCreate.latitude) : null,
    longitude: listingCreate.longitude ? Number(listingCreate.longitude) : null,
    availabilitySlots: [],
    photos: [],
    categoryIds: [],
  };
  apiResults.create = await fetchJson(`${USER_API_BASE}/listings`, { method: 'POST', body: JSON.stringify(payload) });
  showToast('Объявление создано');
};

const deleteListing = async () => {
  if (!ensureUserId()) return;
  const ownerId = userId.value;
  const listingId = apiResults.create?.id || '';
  if (!listingId) {
    showToast('Нужен listingId для удаления', 'error');
    return;
  }
  await fetchJson(`${USER_API_BASE}/listings/${listingId}?ownerId=${ownerId}`, { method: 'DELETE' });
  apiResults.delete = { message: 'Удалено', listingId };
  showToast('Удалено');
};
</script>

<template>
  <div class="api-playground">
    <div class="section">
      <h3>Поиск объявлений</h3>
      <div class="grid">
        <div class="field">
          <label>Текст</label>
          <input v-model="listingSearch.text" placeholder="дрель">
        </div>
        <div class="field">
          <label>Категория UUID</label>
          <input v-model="listingSearch.categoryId" placeholder="опционально">
        </div>
        <div class="field">
          <label>Мин. цена</label>
          <input v-model="listingSearch.minPrice" type="number" step="0.01">
        </div>
        <div class="field">
          <label>Макс. цена</label>
          <input v-model="listingSearch.maxPrice" type="number" step="0.01">
        </div>
      </div>
      <button class="btn primary" @click="searchListings">Искать</button>
      <div v-if="listingCards.length" class="cards">
        <div v-for="card in listingCards" :key="card.id" class="card">
          <div class="card-header">
            <div class="card-title">{{ card.title || 'Без названия' }}</div>
            <span class="chip">{{ card.pricePerHour ? card.pricePerHour + ' ₽/ч' : 'Договорная' }}</span>
          </div>
          <p class="card-text">{{ card.description || 'Нет описания' }}</p>
          <div class="card-meta">
            <span>Депозит: {{ card.depositAmount ?? 0 }} ₽</span>
            <span>Автоподтверждение: {{ card.autoConfirmation ? 'Да' : 'Нет' }}</span>
          </div>
        </div>
      </div>
      <pre v-if="apiResults.listings">{{ toJson(apiResults.listings) }}</pre>
    </div>

    <div class="section">
      <h3>Создать объявление</h3>
      <div class="grid">
        <div class="field"><label>Название</label><input v-model="listingCreate.title" placeholder="Перфоратор Bosch"></div>
        <div class="field"><label>Цена / час</label><input v-model="listingCreate.pricePerHour" type="number" step="0.01"></div>
        <div class="field"><label>Депозит</label><input v-model="listingCreate.depositAmount" type="number" step="0.01"></div>
        <div class="field checkbox">
          <label>Auto confirm</label>
          <input v-model="listingCreate.autoConfirmation" type="checkbox">
        </div>
        <div class="field"><label>Latitude</label><input v-model="listingCreate.latitude" type="number" step="0.000001"></div>
        <div class="field"><label>Longitude</label><input v-model="listingCreate.longitude" type="number" step="0.000001"></div>
        <div class="field full"><label>Описание</label><textarea v-model="listingCreate.description" rows="2"></textarea></div>
      </div>
      <button class="btn primary" @click="createListing">Создать</button>
      <button class="btn" style="margin-top:8px" @click="deleteListing">Удалить созданное</button>
      <pre v-if="apiResults.create">{{ toJson(apiResults.create) }}</pre>
      <pre v-if="apiResults.delete">{{ toJson(apiResults.delete) }}</pre>
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
