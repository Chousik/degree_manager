<script setup>
import { reactive } from 'vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const moderationForms = reactive({
  listingFlag: { listingId: '', reason: '' },
  reviewFlag: { reviewId: '', reason: '' },
  listingResolve: { listingId: '', action: 'approve', comment: '' },
  reviewResolve: { reviewId: '', action: 'approve', comment: '' },
});

const apiResults = reactive({
  moderationAction: null,
  flaggedListings: null,
  flaggedReviews: null,
});

const toast = reactive({ message: '', type: 'success', visible: false });
const { userId } = useSession();
const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};
const toJson = (val) => (val ? JSON.stringify(val, null, 2) : '');

const flagListing = async () => {
  const { listingId, reason } = moderationForms.listingFlag;
  if (!listingId || !reason || !userId.value) {
    showToast('Укажите listingId, причину; в токене должен быть userId/sub', 'error');
    return;
  }
  await fetchJson(`${USER_API_BASE}/moderation/listings/${listingId}/flag`, {
    method: 'POST',
    body: JSON.stringify({ reporterId: userId.value, reason }),
  });
  apiResults.moderationAction = { message: 'Объявление отправлено на модерацию' };
  showToast('Флаг выставлен');
};

const flagReview = async () => {
  const { reviewId, reason } = moderationForms.reviewFlag;
  if (!reviewId || !reason || !userId.value) {
    showToast('Укажите reviewId, причину; в токене должен быть userId/sub', 'error');
    return;
  }
  await fetchJson(`${USER_API_BASE}/moderation/reviews/${reviewId}/flag`, {
    method: 'POST',
    body: JSON.stringify({ reporterId: userId.value, reason }),
  });
  apiResults.moderationAction = { message: 'Отзыв отправлен на модерацию' };
  showToast('Флаг выставлен');
};

const resolveListing = async () => {
  const { listingId, action, comment } = moderationForms.listingResolve;
  if (!listingId || !action || !userId.value) {
    showToast('Укажите listingId, действие; в токене должен быть userId/sub', 'error');
    return;
  }
  await fetchJson(`${USER_API_BASE}/moderation/listings/${listingId}/resolve`, {
    method: 'POST',
    body: JSON.stringify({ adminId: userId.value, action, comment }),
  });
  apiResults.moderationAction = { message: 'Объявление обработано' };
};

const resolveReview = async () => {
  const { reviewId, action, comment } = moderationForms.reviewResolve;
  if (!reviewId || !action || !userId.value) {
    showToast('Укажите reviewId, действие; в токене должен быть userId/sub', 'error');
    return;
  }
  await fetchJson(`${USER_API_BASE}/moderation/reviews/${reviewId}/resolve`, {
    method: 'POST',
    body: JSON.stringify({ adminId: userId.value, action, comment }),
  });
  apiResults.moderationAction = { message: 'Отзыв обработан' };
};

const loadFlagged = async () => {
  const [listings, reviews] = await Promise.all([
    fetchJson(`${USER_API_BASE}/moderation/listings`),
    fetchJson(`${USER_API_BASE}/moderation/reviews`),
  ]);
  apiResults.flaggedListings = listings;
  apiResults.flaggedReviews = reviews;
};
</script>

<template>
  <div class="api-playground">
    <div class="section">
      <h3>Флаги</h3>
      <div class="grid">
        <div class="field"><label>ListingId</label><input v-model="moderationForms.listingFlag.listingId" placeholder="UUID"></div>
        <div class="field full"><label>Причина</label><input v-model="moderationForms.listingFlag.reason"></div>
      </div>
      <button class="btn primary" @click="flagListing">Пожаловаться на объявление</button>

      <div class="grid" style="margin-top:12px">
        <div class="field"><label>ReviewId</label><input v-model="moderationForms.reviewFlag.reviewId" placeholder="UUID"></div>
        <div class="field full"><label>Причина</label><input v-model="moderationForms.reviewFlag.reason"></div>
      </div>
      <button class="btn primary" @click="flagReview">Пожаловаться на отзыв</button>
    </div>

    <div class="section">
      <h3>Решения модерации</h3>
      <div class="grid">
        <div class="field"><label>ListingId</label><input v-model="moderationForms.listingResolve.listingId" placeholder="UUID"></div>
        <div class="field"><label>Действие</label><input v-model="moderationForms.listingResolve.action" placeholder="approve/reject"></div>
        <div class="field full"><label>Комментарий</label><input v-model="moderationForms.listingResolve.comment"></div>
      </div>
      <button class="btn primary" @click="resolveListing">Обработать объявление</button>

      <div class="grid" style="margin-top:12px">
        <div class="field"><label>ReviewId</label><input v-model="moderationForms.reviewResolve.reviewId" placeholder="UUID"></div>
        <div class="field"><label>Действие</label><input v-model="moderationForms.reviewResolve.action" placeholder="approve/reject"></div>
        <div class="field full"><label>Комментарий</label><input v-model="moderationForms.reviewResolve.comment"></div>
      </div>
      <button class="btn primary" @click="resolveReview">Обработать отзыв</button>
    </div>

    <div class="section">
      <h3>Флаги в очереди</h3>
      <button class="btn" @click="loadFlagged">Загрузить</button>
      <pre v-if="apiResults.flaggedListings">{{ toJson(apiResults.flaggedListings) }}</pre>
      <pre v-if="apiResults.flaggedReviews">{{ toJson(apiResults.flaggedReviews) }}</pre>
      <pre v-if="apiResults.moderationAction">{{ toJson(apiResults.moderationAction) }}</pre>
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
