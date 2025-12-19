<script setup>
import { computed, reactive } from 'vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const accountForm = reactive({
  profile: {
    name: '',
    surname: '',
    lastName: '',
    phone: '',
  },
  notifications: {
    systemNotifications: true,
    rentalNotifications: true,
    messageNotifications: true,
    paymentNotifications: true,
  },
});

const apiResults = reactive({
  dashboard: null,
  profile: null,
  notifications: null,
});
const toast = reactive({ message: '', type: 'success', visible: false });
const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};
const toJson = (val) => (val ? JSON.stringify(val, null, 2) : '');
const listingsActive = computed(() => apiResults.dashboard?.activeListings || []);
const listingsArchived = computed(() => apiResults.dashboard?.archivedListings || []);
const listingsFavorites = computed(() => apiResults.dashboard?.favorites || []);
const { userId } = useSession();

const loadDashboard = async () => {
  if (!userId.value) {
    showToast('В токене нет userId/sub — войдите заново', 'error');
    return;
  }
  const data = await fetchJson(`${USER_API_BASE}/account/${userId.value}`);
  apiResults.dashboard = data;
  accountForm.profile = {
    name: data?.profile?.name || '',
    surname: data?.profile?.surname || '',
    lastName: data?.profile?.lastName || '',
    phone: data?.profile?.phone || '',
  };
  accountForm.notifications = {
    systemNotifications: data?.notificationSettings?.systemNotifications ?? true,
    rentalNotifications: data?.notificationSettings?.rentalNotifications ?? true,
    messageNotifications: data?.notificationSettings?.messageNotifications ?? true,
    paymentNotifications: data?.notificationSettings?.paymentNotifications ?? true,
  };
};

const updateProfile = async () => {
  if (!userId.value) {
    showToast('В токене нет userId/sub — войдите заново', 'error');
    return;
  }
  const payload = { ...accountForm.profile };
  apiResults.profile = await fetchJson(`${USER_API_BASE}/account/${userId.value}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  showToast('Профиль обновлён');
};

const updateNotifications = async () => {
  if (!userId.value) {
    showToast('В токене нет userId/sub — войдите заново', 'error');
    return;
  }
  const payload = { ...accountForm.notifications };
  apiResults.notifications = await fetchJson(`${USER_API_BASE}/account/${userId.value}/notifications`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  });
  showToast('Настройки сохранены');
};
</script>

<template>
  <div class="api-playground">
    <div class="section">
      <h3>Профиль и дашборд</h3>
      <button class="btn primary" @click="loadDashboard">Загрузить дашборд</button>
      <div v-if="apiResults.dashboard" class="profile-card">
        <div class="profile-row">
          <div class="profile-name">{{ apiResults.dashboard.profile?.name }} {{ apiResults.dashboard.profile?.surname }}</div>
          <div class="chip ghost">ID: {{ userId }}</div>
        </div>
        <div class="profile-meta">
          <span>Телефон: {{ apiResults.dashboard.profile?.phone || '—' }}</span>
          <span>Уведомления: {{ apiResults.dashboard.notificationSettings?.systemNotifications ? 'Вкл' : 'Выкл' }}</span>
        </div>
      </div>
      <pre v-if="apiResults.dashboard">{{ toJson(apiResults.dashboard) }}</pre>
    </div>

    <div class="section">
      <h3>Обновить профиль</h3>
      <div class="grid">
        <div class="field"><label>Имя</label><input v-model="accountForm.profile.name"></div>
        <div class="field"><label>Фамилия</label><input v-model="accountForm.profile.surname"></div>
        <div class="field"><label>Отчество</label><input v-model="accountForm.profile.lastName"></div>
        <div class="field"><label>Телефон</label><input v-model="accountForm.profile.phone" placeholder="10-12 цифр"></div>
      </div>
      <button class="btn" @click="updateProfile">Сохранить профиль</button>
      <pre v-if="apiResults.profile">{{ toJson(apiResults.profile) }}</pre>
    </div>

    <div class="section">
      <h3>Настройки уведомлений</h3>
      <div class="grid">
        <div class="field checkbox">
          <label>Системные</label>
          <input type="checkbox" v-model="accountForm.notifications.systemNotifications">
        </div>
        <div class="field checkbox">
          <label>Аренда</label>
          <input type="checkbox" v-model="accountForm.notifications.rentalNotifications">
        </div>
        <div class="field checkbox">
          <label>Сообщения</label>
          <input type="checkbox" v-model="accountForm.notifications.messageNotifications">
        </div>
        <div class="field checkbox">
          <label>Платежи</label>
          <input type="checkbox" v-model="accountForm.notifications.paymentNotifications">
        </div>
      </div>
      <button class="btn" @click="updateNotifications">Сохранить уведомления</button>
      <pre v-if="apiResults.notifications">{{ toJson(apiResults.notifications) }}</pre>
    </div>

    <div class="section" v-if="listingsActive.length || listingsArchived.length || listingsFavorites.length">
      <h3>Мои объявления и избранное</h3>
      <div v-if="listingsActive.length">
        <h4 class="subheading">Активные</h4>
        <div class="cards">
          <div v-for="item in listingsActive" :key="item.id" class="card">
            <div class="card-header">
              <div class="card-title">{{ item.title }}</div>
              <span class="chip">{{ item.pricePerHour ?? 0 }} ₽/ч</span>
            </div>
            <p class="card-text">{{ item.description || 'Без описания' }}</p>
          </div>
        </div>
      </div>

      <div v-if="listingsArchived.length">
        <h4 class="subheading">Архив</h4>
        <div class="cards">
          <div v-for="item in listingsArchived" :key="item.id" class="card muted-card">
            <div class="card-header">
              <div class="card-title">{{ item.title }}</div>
              <span class="chip ghost">Архив</span>
            </div>
            <p class="card-text">{{ item.description || 'Без описания' }}</p>
          </div>
        </div>
      </div>

      <div v-if="listingsFavorites.length">
        <h4 class="subheading">Избранное</h4>
        <div class="cards">
          <div v-for="item in listingsFavorites" :key="item.id" class="card">
            <div class="card-header">
              <div class="card-title">{{ item.title }}</div>
              <span class="chip">{{ item.pricePerHour ?? 0 }} ₽/ч</span>
            </div>
            <p class="card-text">{{ item.description || 'Без описания' }}</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
