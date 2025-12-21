<template>
  <div>
    <header class="main-header">
      <div class="main-header__location">
        <button class="main-location" type="button" @click="openCityModal">
          <img src="/media/map.svg" alt="Город" class="main-location-icon">
          <span>г. {{ cityLabel }}</span>
        </button>
      </div>
      <div class="main-logo">
        <button type="button" class="main-logo-button" @click="navigateToHome">
          <img src="/media/logo_big.png" alt="Fixly" class="main-logo-img">
        </button>
      </div>
      <div class="main-header__icons">
        <button
          v-for="icon in icons"
          :key="icon.alt"
          class="main-icon"
        type="button"
        :aria-label="icon.alt"
        @click="navigate(icon.route)"
      >
        <img :src="icon.src" :alt="icon.alt">
      </button>
      </div>
    </header>

    <div v-if="showCityModal" class="city-modal">
      <div class="city-modal__overlay" @click="closeCityModal"></div>
      <div class="city-modal__panel">
        <h3>Выберите город</h3>
        <p>Это поможет показывать актуальные объявления</p>
        <div class="city-modal__list">
          <button
            v-for="option in cityOptions"
            :key="option"
            type="button"
            class="city-option"
            :class="{ active: option === selectedCity }"
            @click="selectedCity = option"
          >
            {{ option }}
          </button>
        </div>
        <p v-if="cityError" class="city-modal__error">{{ cityError }}</p>
        <div class="city-modal__actions">
          <button type="button" class="landing-btn ghost" @click="closeCityModal">Отмена</button>
          <button type="button" class="landing-btn primary" :disabled="savingCity" @click="saveCity">
            {{ savingCity ? 'Сохраняем...' : 'Сохранить' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useSession } from '../state/session';
import { updateCity } from '../api/account';

const router = useRouter();
const { city, isLoggedIn, userId, setCity, loadCityFromServer } = useSession();

const showCityModal = ref(false);
const selectedCity = ref('Москва');
const savingCity = ref(false);
const cityError = ref('');
const cityOptions = ['Москва', 'Санкт-Петербург', 'Казань', 'Екатеринбург', 'Нижний Новгород', 'Новосибирск'];

const cityLabel = computed(() => city.value || 'Москва');

const icons = [
  { src: '/media/notification.svg', alt: 'Уведомления', route: '/notifications' },
  { src: '/media/favorite.svg', alt: 'Избранное', route: '/favorites' },
  { src: '/media/user.svg', alt: 'Профиль', route: '/account' },
];

function navigate(path) {
  router.push(path);
}

function navigateToHome() {
  router.push('/');
}

function openCityModal() {
  selectedCity.value = cityLabel.value;
  cityError.value = '';
  showCityModal.value = true;
}

function closeCityModal() {
  showCityModal.value = false;
  cityError.value = '';
}

async function saveCity() {
  const nextCity = selectedCity.value || 'Москва';
  if (nextCity === cityLabel.value) {
    closeCityModal();
    return;
  }
  savingCity.value = true;
  cityError.value = '';
  try {
    if (isLoggedIn.value && userId.value) {
      await updateCity(userId.value, nextCity);
      await loadCityFromServer(true);
    }
    setCity(nextCity);
    closeCityModal();
  } catch (err) {
    cityError.value = err.message || 'Не удалось сохранить город';
  } finally {
    savingCity.value = false;
  }
}

onMounted(() => {
  if (isLoggedIn.value) {
    loadCityFromServer();
  }
});

watch(() => isLoggedIn.value, (loggedIn) => {
  if (loggedIn) {
    loadCityFromServer(true);
  }
});

watch(() => userId.value, (value) => {
  if (isLoggedIn.value && value) {
    loadCityFromServer(true);
  }
});
</script>
