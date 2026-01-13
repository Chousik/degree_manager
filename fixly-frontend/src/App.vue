<script setup>
import { computed } from 'vue';
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router';
import { useSession } from './state/session';

const { isLoggedIn, logout } = useSession();
const route = useRoute();
const router = useRouter();

const navLinks = [
  { to: '/catalog', label: 'Каталог' },
  { to: '/listings', label: 'Объявления' },
  { to: '/rentals', label: 'Аренды' },
  { to: '/favorites', label: 'Избранное' },
  { to: '/account', label: 'Аккаунт' },
  { to: '/communication', label: 'Сообщения/Отзывы' },
  { to: '/moderation', label: 'Модерация' },
  { to: '/support', label: 'Поддержка' },
];

const showNav = computed(() => isLoggedIn.value);
const isCatalog = computed(() => route.path === '/catalog');
const isStandalone = computed(() => Boolean(route.meta?.authPage || route.meta?.landingPage || route.meta?.standalone));

const handleLogout = () => {
  logout();
  router.push('/login');
};
</script>

<template>
  <RouterView v-if="isStandalone" />
  <div v-else class="page">
    <div class="bg-shape bg-shape-1"></div>
    <div class="bg-shape bg-shape-2"></div>

    <main v-if="isCatalog" class="full-catalog">
      <RouterView />
    </main>
    <main v-else class="layout">
      <section class="auth-card">
        <RouterView />
      </section>
    </main>
  </div>
</template>
