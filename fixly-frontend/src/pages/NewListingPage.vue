<script setup>
import { computed, reactive, ref, watch } from 'vue';
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
  address: '',
  latitude: '',
  longitude: '',
  categoryIds: [],
});
const loading = ref(false);
const message = ref('');
const error = ref('');
const categories = ref([]);
const categoriesLoading = ref(false);
const photoFiles = ref([]);
const photoPreviews = ref([]);
const photoError = ref('');
const categoryQuery = ref('');
const categoryDropdownOpen = ref(false);
const addressQuery = ref('');
const addressSuggestions = ref([]);
const addressDropdownOpen = ref(false);
const addressLoading = ref(false);
const addressError = ref('');
const selectedAddress = ref(null);

const selectedCategories = computed(() =>
  categories.value.filter((cat) => form.categoryIds.includes(cat.id))
);

const filteredCategorySuggestions = computed(() => {
  const query = categoryQuery.value.trim().toLowerCase();
  return categories.value.filter((cat) => {
    if (form.categoryIds.includes(cat.id)) return false;
    if (!query) return true;
    return cat.name.toLowerCase().includes(query);
  });
});

const mediaBase = computed(() => {
  try {
    const url = new URL('/media', USER_API_BASE);
    return url.toString().replace(/\/$/, '');
  } catch (err) {
    return 'http://localhost:8075/media';
  }
});
const geocodeBase = (import.meta.env.VITE_GEOCODING_BASE || 'https://nominatim.openstreetmap.org').replace(/\/$/, '');

const clearPhotoPreviews = () => {
  photoPreviews.value.forEach((url) => URL.revokeObjectURL(url));
  photoPreviews.value = [];
};

const handlePhotoChange = (event) => {
  const files = Array.from(event.target.files || []);
  photoError.value = '';
  if (!files.length) {
    photoFiles.value = [];
    clearPhotoPreviews();
    return;
  }
  if (files.length > 5) {
    photoError.value = 'Можно загрузить не больше 5 фотографий';
  }
  const limited = files.slice(0, 5);
  photoFiles.value = limited;
  clearPhotoPreviews();
  photoPreviews.value = limited.map((file) => URL.createObjectURL(file));
};

const getUploadName = (file, index) => {
  const safeName = file.name.replace(/[^a-zA-Z0-9._-]/g, '_');
  return `${Date.now()}-${index + 1}-${safeName}`;
};

const uploadPhotos = async () => {
  if (!photoFiles.value.length) return [];
  const uploaded = [];
  for (let i = 0; i < photoFiles.value.length; i += 1) {
    const file = photoFiles.value[i];
    const uploadName = getUploadName(file, i);
    const uploadUrl = `${mediaBase.value}/${encodeURIComponent(uploadName)}`;
    const response = await fetch(uploadUrl, {
      method: 'PUT',
      headers: { 'Content-Type': file.type || 'application/octet-stream' },
      body: file,
    });
    if (!response.ok) {
      throw new Error('Не удалось загрузить фото. Проверьте доступ к хранилищу.');
    }
    uploaded.push({ url: uploadUrl, sortOrder: i + 1 });
  }
  return uploaded;
};

const addCategory = (category) => {
  if (!category || form.categoryIds.includes(category.id)) return;
  form.categoryIds.push(category.id);
  categoryQuery.value = '';
};

const removeCategory = (id) => {
  form.categoryIds = form.categoryIds.filter((catId) => catId !== id);
};

const handleCategoryKeydown = (event) => {
  if (event.key === 'Enter') {
    event.preventDefault();
    const firstSuggestion = filteredCategorySuggestions.value[0];
    if (firstSuggestion) {
      addCategory(firstSuggestion);
    }
  }
};

const openCategoryDropdown = () => {
  categoryDropdownOpen.value = true;
};

const closeCategoryDropdown = () => {
  setTimeout(() => {
    categoryDropdownOpen.value = false;
  }, 150);
};

const openAddressDropdown = () => {
  addressDropdownOpen.value = true;
};

const closeAddressDropdown = () => {
  setTimeout(() => {
    addressDropdownOpen.value = false;
  }, 150);
};

const selectAddress = (suggestion) => {
  if (!suggestion) return;
  selectedAddress.value = suggestion;
  addressQuery.value = suggestion.label;
  form.address = suggestion.label;
  form.latitude = suggestion.lat;
  form.longitude = suggestion.lon;
  addressSuggestions.value = [];
  addressDropdownOpen.value = false;
};

const handleAddressKeydown = (event) => {
  if (event.key === 'Enter') {
    event.preventDefault();
    if (addressSuggestions.value.length) {
      selectAddress(addressSuggestions.value[0]);
    }
  }
};

let addressTimer;
let addressRequestId = 0;

const fetchAddressSuggestions = async (query, requestId) => {
  addressLoading.value = true;
  addressError.value = '';
  try {
    const url = `${geocodeBase}/search?format=json&addressdetails=1&limit=6&accept-language=ru&q=${encodeURIComponent(query)}`;
    const response = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!response.ok) {
      throw new Error('Не удалось загрузить подсказки');
    }
    const data = await response.json();
    if (requestId !== addressRequestId) return;
    addressSuggestions.value = (Array.isArray(data) ? data : []).map((item) => ({
      label: item.display_name,
      lat: Number.isFinite(Number(item.lat)) ? Number(item.lat) : null,
      lon: Number.isFinite(Number(item.lon)) ? Number(item.lon) : null,
    }));
  } catch (err) {
    if (requestId === addressRequestId) {
      addressError.value = err.message || 'Не удалось загрузить подсказки';
    }
  } finally {
    if (requestId === addressRequestId) {
      addressLoading.value = false;
    }
  }
};

watch(addressQuery, (value) => {
  if (addressTimer) clearTimeout(addressTimer);
  if (!value || value.trim().length < 3) {
    addressSuggestions.value = [];
    addressLoading.value = false;
    addressError.value = '';
    selectedAddress.value = null;
    form.address = value?.trim() || '';
    form.latitude = '';
    form.longitude = '';
    return;
  }
  selectedAddress.value = null;
  addressDropdownOpen.value = true;
  addressTimer = setTimeout(() => {
    addressRequestId += 1;
    fetchAddressSuggestions(value.trim(), addressRequestId);
  }, 300);
});

async function createListing() {
  if (!isLoggedIn.value || !userId.value) {
    error.value = 'Сначала войдите в аккаунт';
    return;
  }
  if (!form.categoryIds.length) {
    error.value = 'Выберите хотя бы одну категорию';
    return;
  }
  if (photoFiles.value.length > 5) {
    error.value = 'Можно загрузить не больше 5 фотографий';
    return;
  }
  loading.value = true;
  message.value = '';
  error.value = '';
  try {
    form.address = addressQuery.value.trim();
    const photos = await uploadPhotos();
    const payload = {
      ownerId: userId.value,
      title: form.title,
      description: form.description,
      pricePerHour: form.pricePerDay ? Number(form.pricePerDay) : null,
      depositAmount: form.depositAmount ? Number(form.depositAmount) : 0,
      autoConfirmation: form.autoConfirmation,
      latitude: selectedAddress.value?.lat ?? null,
      longitude: selectedAddress.value?.lon ?? null,
      address: form.address || null,
      availabilitySlots: [],
      photos,
      categoryIds: form.categoryIds,
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
      address: '',
      latitude: '',
      longitude: '',
      categoryIds: [],
    });
    addressQuery.value = '';
    addressSuggestions.value = [];
    selectedAddress.value = null;
    photoFiles.value = [];
    clearPhotoPreviews();
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
        <label>
          Адрес
          <div class="address-picker">
            <input
              v-model="addressQuery"
              type="text"
              placeholder="Начните вводить адрес"
              @focus="openAddressDropdown"
              @blur="closeAddressDropdown"
              @keydown="handleAddressKeydown"
            />
            <div v-if="addressDropdownOpen && addressSuggestions.length" class="address-suggestions">
              <button
                v-for="suggestion in addressSuggestions"
                :key="suggestion.label"
                type="button"
                class="address-suggestion"
                @click="selectAddress(suggestion)"
              >
                {{ suggestion.label }}
              </button>
            </div>
          </div>
        </label>
        <p v-if="addressLoading" class="form-hint">Ищем адрес...</p>
        <p v-else-if="addressError" class="form-hint error">{{ addressError }}</p>
        <p v-else class="form-hint">Выберите адрес из подсказок, чтобы закрепить локацию.</p>
        <div class="toggle-row">
          <span>Автоматическое подтверждение</span>
          <label class="toggle">
            <input v-model="form.autoConfirmation" type="checkbox" />
            <span class="toggle-track"></span>
          </label>
        </div>
        <div class="category-field">
          <p class="label">Категории</p>
          <div v-if="categoriesLoading" class="form-hint">Загружаем категории...</div>
          <div v-else class="category-picker">
            <div class="category-chips" v-if="selectedCategories.length">
              <button
                v-for="cat in selectedCategories"
                :key="cat.id"
                type="button"
                class="category-chip"
                @click="removeCategory(cat.id)"
              >
                {{ cat.name }} ×
              </button>
            </div>
            <input
              v-model="categoryQuery"
              type="text"
              placeholder="Начните вводить категорию"
              @focus="openCategoryDropdown"
              @blur="closeCategoryDropdown"
              @keydown="handleCategoryKeydown"
            />
            <div v-if="categoryDropdownOpen && filteredCategorySuggestions.length" class="category-suggestions">
              <button
                v-for="cat in filteredCategorySuggestions"
                :key="cat.id"
                type="button"
                class="category-suggestion"
                @click="addCategory(cat)"
              >
                {{ cat.name }}
              </button>
            </div>
            <p class="form-hint">Можно выбрать несколько категорий.</p>
          </div>
        </div>
        <label>
          Фотографии (до 5)
          <input type="file" accept="image/*" multiple @change="handlePhotoChange" />
        </label>
        <p v-if="photoError" class="form-hint error">{{ photoError }}</p>
        <div v-if="photoPreviews.length" class="photo-grid">
          <img v-for="(url, index) in photoPreviews" :key="`${url}-${index}`" :src="url" alt="Фото объявления" />
        </div>
        <button class="btn primary" type="submit" :disabled="loading">
          {{ loading ? 'Сохраняем...' : 'Создать объявление' }}
        </button>
        <p v-if="message" class="dashboard-note">{{ message }}</p>
        <p v-if="error" class="dashboard-note error">{{ error }}</p>
      </form>
    </section>
  </div>
</template>
