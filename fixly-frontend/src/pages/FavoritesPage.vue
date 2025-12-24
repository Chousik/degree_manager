<script setup>
import { computed, reactive } from 'vue';
import { addFavorite as addFavoriteApi, getFavorites, removeFavorite as removeFavoriteApi } from '../api/favorites';
import { useSession } from '../state/session';

const favoriteForm = reactive({ listingId: '' });
const apiResults = reactive({ favoriteAction: null, favorites: null });
const toast = reactive({ message: '', type: 'success', visible: false });
const favoriteCards = computed(() => (apiResults.favorites && Array.isArray(apiResults.favorites) ? apiResults.favorites : []));
const { userId } = useSession();

const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};
const toJson = (val) => (val ? JSON.stringify(val, null, 2) : '');

const addFavorite = async () => {
  if (!userId.value) {
    showToast('В токене нет userId/sub — войдите заново', 'error');
    return;
  }
  await addFavoriteApi(userId.value, favoriteForm.listingId);
  apiResults.favoriteAction = { message: 'Добавлено в избранное' };
  showToast('Добавлено');
};

const removeFavorite = async () => {
  if (!userId.value) {
    showToast('В токене нет userId/sub — войдите заново', 'error');
    return;
  }
  await removeFavoriteApi(userId.value, favoriteForm.listingId);
  apiResults.favoriteAction = { message: 'Удалено из избранного' };
  showToast('Удалено');
};

const loadFavorites = async () => {
  if (!userId.value) {
    showToast('В токене нет userId/sub — войдите заново', 'error');
    return;
  }
  apiResults.favorites = await getFavorites(userId.value);
};
</script>

<template>
  <div class="api-playground">
    <div class="section">
      <h3>Избранное</h3>
      <div class="grid">
        <div class="field"><label>ListingId</label><input v-model="favoriteForm.listingId" placeholder="UUID"></div>
      </div>
      <button class="btn primary" @click="addFavorite">Добавить</button>
      <button class="btn" style="margin-top:8px" @click="removeFavorite">Удалить</button>
      <pre v-if="apiResults.favoriteAction">{{ toJson(apiResults.favoriteAction) }}</pre>
    </div>

    <div class="section">
      <h3>Список избранного</h3>
      <button class="btn" @click="loadFavorites">Загрузить избранное</button>
      <div v-if="favoriteCards.length" class="cards">
        <div v-for="item in favoriteCards" :key="item.id" class="card">
          <div class="card-header">
            <div class="card-title">{{ item.title }}</div>
            <span class="chip">{{ item.pricePerHour ?? 0 }} ₽/ч</span>
          </div>
          <p class="card-text">{{ item.description || 'Без описания' }}</p>
        </div>
      </div>
      <pre v-if="apiResults.favorites">{{ toJson(apiResults.favorites) }}</pre>
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
