<script setup>
import { reactive } from 'vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const rentalCreate = reactive({
  listingId: '',
  startAt: '',
  endAt: '',
  depositAmount: '',
});

const rentalAction = reactive({
  rentalId: '',
  action: 'confirm',
});

const apiResults = reactive({ rental: null, action: null });
const toast = reactive({ message: '', type: 'success', visible: false });
const { userId } = useSession();

const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};
const toJson = (val) => (val ? JSON.stringify(val, null, 2) : '');

const ensureUserId = () => {
  if (!userId.value) {
    showToast('В токене нет userId/sub — войдите заново', 'error');
    return false;
  }
  return true;
};

const createRental = async () => {
  if (!ensureUserId()) return;
  const payload = {
    listingId: rentalCreate.listingId || null,
    lesseeId: userId.value,
    startAt: rentalCreate.startAt,
    endAt: rentalCreate.endAt,
    depositAmount: rentalCreate.depositAmount ? Number(rentalCreate.depositAmount) : 0,
  };
  apiResults.rental = await fetchJson(`${USER_API_BASE}/rentals`, { method: 'POST', body: JSON.stringify(payload) });
  showToast('Аренда создана');
};

const performRentalAction = async () => {
  if (!ensureUserId()) return;
  if (!rentalAction.rentalId) {
    showToast('Укажите rentalId', 'error');
    return;
  }
  const url = `${USER_API_BASE}/rentals/${rentalAction.rentalId}/${rentalAction.action}`;
  apiResults.action = await fetchJson(url, {
    method: 'POST',
    body: JSON.stringify({ actorId: userId.value }),
  });
  showToast('Действие применено');
};
</script>

<template>
  <div class="api-playground">
    <div class="section">
      <h3>Создать аренду</h3>
      <div class="grid">
        <div class="field"><label>ListingId</label><input v-model="rentalCreate.listingId" placeholder="UUID"></div>
        <div class="field"><label>StartAt</label><input v-model="rentalCreate.startAt" type="datetime-local"></div>
        <div class="field"><label>EndAt</label><input v-model="rentalCreate.endAt" type="datetime-local"></div>
        <div class="field"><label>Депозит</label><input v-model="rentalCreate.depositAmount" type="number" step="0.01"></div>
      </div>
      <button class="btn primary" @click="createRental">Создать</button>
      <pre v-if="apiResults.rental">{{ toJson(apiResults.rental) }}</pre>
    </div>

    <div class="section">
      <h3>Действия по аренде</h3>
      <div class="grid">
        <div class="field"><label>RentalId</label><input v-model="rentalAction.rentalId" placeholder="UUID"></div>
        <div class="field">
          <label>Действие</label>
          <select v-model="rentalAction.action">
            <option value="confirm">confirm</option>
            <option value="cancel">cancel</option>
            <option value="complete">complete</option>
          </select>
        </div>
      </div>
      <button class="btn" @click="performRentalAction">Выполнить</button>
      <pre v-if="apiResults.action">{{ toJson(apiResults.action) }}</pre>
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
