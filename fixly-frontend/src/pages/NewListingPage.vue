<script setup>
import { reactive, ref } from 'vue';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const { isLoggedIn, userId } = useSession();
const form = reactive({
  title: '',
  description: '',
  pricePerDay: '',
  depositAmount: '',
  autoConfirmation: true,
  latitude: '',
  longitude: '',
  categoryId: '',
});
const loading = ref(false);
const message = ref('');
const error = ref('');
const categories = ref([]);
const categoriesLoading = ref(false);

async function createListing() {
  if (!isLoggedIn.value || !userId.value) {
    error.value = 'Сначала войдите в аккаунт';
    return;
  }
  if (!form.categoryId) {
    error.value = 'Выберите категорию';
    return;
  }
  loading.value = true;
  message.value = '';
  error.value = '';
  try {
    const payload = {
      ownerId: userId.value,
      title: form.title,
      description: form.description,
      pricePerHour: form.pricePerDay ? Number(form.pricePerDay) : null,
      depositAmount: form.depositAmount ? Number(form.depositAmount) : 0,
      autoConfirmation: form.autoConfirmation,
      latitude: form.latitude ? Number(form.latitude) : null,
      longitude: form.longitude ? Number(form.longitude) : null,
      availabilitySlots: [],
      photos: [],
      categoryIds: form.categoryId ? [form.categoryId] : [],
    };
    await fetchJson(`${USER_API_BASE}/listings`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    message.value = 'Объявление создано';
    Object.assign(form, {
      title: '',
      description: '',
      pricePerDay: '',
      depositAmount: '',
      autoConfirmation: true,
      latitude: '',
      longitude: '',
      categoryId: '',
    });
  } catch (err) {
    error.value = err.message || 'Ошибка создания объявления';
  } finally {
    loading.value = false;
  }
}

async function loadCategories() {
  if (!isLoggedIn.value) {
    categories.value = [];
    return;
  }
  categoriesLoading.value = true;
  try {
    const data = await fetchJson(`${USER_API_BASE}/categories`);
    categories.value = Array.isArray(data) ? data : [];
  } catch (err) {
    categories.value = [];
  } finally {
    categoriesLoading.value = false;
  }
}

loadCategories();
</script>

<template>
  <div class="dashboard">
    <MainHeader />
    <section class="dashboard-section">
      <h2>Новое объявление</h2>
      <p v-if="!isLoggedIn" class="dashboard-note">Войдите, чтобы создавать объявления.</p>
      <form v-else @submit.prevent="createListing" class="listing-form">
        <label>
          Название
          <input v-model="form.title" required />
        </label>
        <label>
          Описание
          <textarea v-model="form.description" rows="3"></textarea>
        </label>
        <div class="form-row">
          <label>
            Цена / день
            <input v-model="form.pricePerDay" type="number" step="0.01" required />
          </label>
          <label>
            Депозит
            <input v-model="form.depositAmount" type="number" step="0.01" />
          </label>
        </div>
        <div class="form-row">
          <label>
            Широта
            <input v-model="form.latitude" type="number" step="0.000001" />
          </label>
          <label>
            Долгота
            <input v-model="form.longitude" type="number" step="0.000001" />
          </label>
        </div>
        <label class="checkbox">
          <input v-model="form.autoConfirmation" type="checkbox" /> Автоматическое подтверждение
        </label>
        <label>
          Категория
          <select v-model="form.categoryId">
            <option value="" disabled selected>Выберите категорию</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
        </label>
        <button class="btn primary" type="submit" :disabled="loading">
          {{ loading ? 'Сохраняем...' : 'Создать объявление' }}
        </button>
        <p v-if="message" class="dashboard-note">{{ message }}</p>
        <p v-if="error" class="dashboard-note error">{{ error }}</p>
      </form>
    </section>
  </div>
</template>
