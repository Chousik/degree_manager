<script setup>
import { reactive } from 'vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const messageForm = reactive({
  rentalId: '',
  body: '',
});

const reviewForm = reactive({
  rentalId: '',
  rating: 5,
  text: '',
});

const apiResults = reactive({ messages: null, messageSent: null, review: null });
const toast = reactive({ message: '', type: 'success', visible: false });
const { userId } = useSession();
const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};
const toJson = (val) => (val ? JSON.stringify(val, null, 2) : '');

const getMessages = async () => {
  if (!messageForm.rentalId || !userId.value) {
    showToast('Укажите rentalId; в токене должен быть userId/sub', 'error');
    return;
  }
  apiResults.messages = await fetchJson(
    `${USER_API_BASE}/rentals/${messageForm.rentalId}/messages?userId=${userId.value}`,
  );
};

const sendMessage = async () => {
  if (!messageForm.rentalId || !messageForm.body || !userId.value) {
    showToast('Нужны rentalId, текст и userId/sub в токене', 'error');
    return;
  }
  const payload = { senderId: userId.value, body: messageForm.body };
  apiResults.messageSent = await fetchJson(`${USER_API_BASE}/rentals/${messageForm.rentalId}/messages`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  showToast('Сообщение отправлено');
};

const leaveReview = async () => {
  if (!reviewForm.rentalId || !userId.value) {
    showToast('Укажите rentalId; в токене должен быть userId/sub', 'error');
    return;
  }
  const payload = {
    authorId: userId.value,
    rating: Number(reviewForm.rating),
    text: reviewForm.text,
  };
  apiResults.review = await fetchJson(`${USER_API_BASE}/rentals/${reviewForm.rentalId}/reviews`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  showToast('Отзыв сохранён');
};
</script>

<template>
  <div class="api-playground">
    <div class="section">
      <h3>Сообщения по аренде</h3>
      <div class="grid">
        <div class="field"><label>RentalId</label><input v-model="messageForm.rentalId" placeholder="UUID"></div>
        <div class="field full"><label>Текст</label><textarea v-model="messageForm.body" rows="2"></textarea></div>
      </div>
      <button class="btn primary" @click="sendMessage">Отправить</button>
      <button class="btn" style="margin-top:8px" @click="getMessages">Загрузить переписку</button>
      <pre v-if="apiResults.messageSent">{{ toJson(apiResults.messageSent) }}</pre>
      <pre v-if="apiResults.messages">{{ toJson(apiResults.messages) }}</pre>
    </div>

    <div class="section">
      <h3>Оставить отзыв</h3>
      <div class="grid">
        <div class="field"><label>RentalId</label><input v-model="reviewForm.rentalId" placeholder="UUID"></div>
        <div class="field"><label>Оценка 1-5</label><input v-model="reviewForm.rating" type="number" min="1" max="5"></div>
        <div class="field full"><label>Текст</label><textarea v-model="reviewForm.text" rows="2"></textarea></div>
      </div>
      <button class="btn primary" @click="leaveReview">Отправить отзыв</button>
      <pre v-if="apiResults.review">{{ toJson(apiResults.review) }}</pre>
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
