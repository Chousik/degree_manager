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
          type="button"
          class="main-icon main-icon--label"
          @click="navigate('/listings')"
        >
          <span>Мои объявления</span>
        </button>
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

        <div class="city-modal__popular">
          <h4>Популярные города</h4>
          <div class="city-modal__list">
            <button
              v-for="cityName in popularCities"
              :key="cityName"
              type="button"
              class="city-option"
              :class="{ active: cityName === selectedCity }"
              @click="selectCity(cityName)"
            >
              {{ cityName }}
            </button>
          </div>
        </div>

        <div class="city-modal__search">
          <label class="city-modal__search-label" for="city-search">Найти другой город</label>
          <div class="city-modal__search-field">
            <span class="city-modal__search-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="7" />
                <line x1="16.65" y1="16.65" x2="21" y2="21" />
              </svg>
            </span>
            <input
              id="city-search"
              v-model="citySearch"
              type="text"
              class="city-modal__search-input"
              placeholder="Введите название города"
            >
          </div>
        </div>

        <div class="city-modal__all">
          <div v-if="filteredCities.length" class="city-modal__list scrollable">
            <button
              v-for="option in filteredCities"
              :key="option"
              type="button"
              class="city-option"
              :class="{ active: option === selectedCity }"
              @click="selectCity(option)"
            >
              {{ option }}
            </button>
          </div>
          <p v-else class="city-modal__empty">Ничего не найдено</p>
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
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useSession } from '../state/session';
import { useNotifications } from '../state/notifications';
import { useRealtime } from '../state/realtime';
import { openRealtimeStream } from '../api/stream';
import { updateCity } from '../api/account';

const router = useRouter();
const { city, isLoggedIn, userId, setCity, loadCityFromServer } = useSession();
const { upsertNotification, clearNotifications } = useNotifications();
const { addMessageEvent, addRentalEvent, clearEvents } = useRealtime();
let streamCleanup = null;

const showCityModal = ref(false);
const selectedCity = ref('Москва');
const citySearch = ref('');
const savingCity = ref(false);
const cityError = ref('');
const allCities = [
  'Москва',
  'Санкт-Петербург',
  'Новосибирск',
  'Екатеринбург',
  'Казань',
  'Нижний Новгород',
  'Челябинск',
  'Самара',
  'Омск',
  'Ростов-на-Дону',
  'Уфа',
  'Красноярск',
  'Пермь',
  'Воронеж',
  'Волгоград',
  'Краснодар',
  'Саратов',
  'Тюмень',
  'Тольятти',
  'Ижевск',
  'Барнаул',
  'Иркутск',
  'Ульяновск',
  'Хабаровск',
  'Ярославль',
  'Владивосток',
  'Махачкала',
  'Томск',
  'Оренбург',
  'Кемерово',
  'Новокузнецк',
  'Рязань',
  'Астрахань',
  'Пенза',
  'Набережные Челны',
  'Липецк',
  'Тула',
  'Киров',
  'Чебоксары',
  'Калининград',
  'Брянск',
  'Курск',
  'Иваново',
  'Магнитогорск',
  'Тверь',
  'Ставрополь',
  'Белгород',
  'Сочи',
  'Калуга',
  'Сургут',
  'Владимир',
  'Чита',
  'Архангельск',
  'Симферополь',
  'Севастополь',
  'Грозный',
  'Петрозаводск',
  'Кострома',
  'Йошкар-Ола',
  'Нижний Тагил',
  'Комсомольск-на-Амуре',
  'Сыктывкар',
  'Новороссийск',
  'Якутск',
  'Тамбов',
  'Мытищи',
  'Подольск',
  'Королёв',
  'Химки',
  'Балашиха',
  'Раменское',
  'Домодедово',
  'Жуковский',
  'Березники',
  'Энгельс',
  'Старый Оскол',
  'Орёл',
  'Нальчик',
  'Шахты',
  'Благовещенск',
  'Псков',
  'Абакан',
  'Каменск-Уральский',
  'Уссурийск',
  'Армавир',
  'Великий Новгород',
  'Салават',
  'Мурманск',
  'Люберцы',
  'Электросталь',
  'Ангарск',
  'Серпухов',
  'Вологда',
  'Кызыл',
  'Орск',
  'Бийск',
  'Прокопьевск',
  'Рубцовск',
  'Находка',
  'Бердск',
  'Дзержинск',
  'Обнинск',
  'Новоуральск'
];
const popularCityPreset = ['Москва', 'Санкт-Петербург', 'Казань'];
const popularCities = popularCityPreset.filter((cityName) => allCities.includes(cityName));
const filteredCities = computed(() => {
  const lookup = citySearch.value.trim().toLowerCase();
  const remaining = allCities.filter((cityName) => !popularCities.includes(cityName));
  if (!lookup) {
    return remaining;
  }
  return remaining.filter((cityName) => cityName.toLowerCase().includes(lookup));
});

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
  citySearch.value = '';
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
    if (isLoggedIn.value) {
      await updateCity(nextCity);
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

function selectCity(cityName) {
  selectedCity.value = cityName;
}

onMounted(() => {
  if (isLoggedIn.value) {
    loadCityFromServer();
  }
  startStream();
});

watch(() => isLoggedIn.value, (loggedIn) => {
  if (loggedIn) {
    loadCityFromServer(true);
    startStream();
  } else {
    clearNotifications();
    clearEvents();
    stopStream();
  }
});

watch(() => userId.value, (value) => {
  if (isLoggedIn.value && value) {
    loadCityFromServer(true);
    startStream();
  }
});

onUnmounted(() => {
  stopStream();
});

function handleStreamMessage(envelope) {
  if (!envelope || !envelope.type) {
    return;
  }
  if (envelope.type === 'notification') {
    upsertNotification(envelope.payload);
    return;
  }
  if (envelope.type === 'message') {
    addMessageEvent(envelope.payload);
    return;
  }
  if (envelope.type === 'rental') {
    addRentalEvent(envelope.payload);
  }
}

function startStream() {
  stopStream();
  if (!isLoggedIn.value || !userId.value) {
    return;
  }
  streamCleanup = openRealtimeStream(userId.value, {
    onMessage: handleStreamMessage,
  });
}

function stopStream() {
  if (streamCleanup) {
    streamCleanup();
    streamCleanup = null;
  }
}
</script>
