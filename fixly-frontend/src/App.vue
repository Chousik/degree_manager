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

const handleLogout = () => {
  logout();
  router.push('/auth');
};
</script>

<template>
  <div class="page">
    <div class="bg-shape bg-shape-1"></div>
    <div class="bg-shape bg-shape-2"></div>

    <header class="topbar">
      <div class="brand">
        <div class="brand-mark">Fx</div>
        <div>
          <div class="brand-name">Fixly</div>
          <div class="brand-tagline">Песочница объявлений в стиле маркетплейса</div>
        </div>
      </div>
      <div class="top-info">
        <span class="badge">Beta</span>
        <span class="muted">{{ showNav ? 'API доступно после входа' : 'Войдите, чтобы тестировать API' }}</span>
      </div>
    </header>

    <nav class="nav">
      <template v-for="link in navLinks" :key="link.to">
        <RouterLink
          v-if="isLoggedIn || link.to === '/catalog'"
          :to="link.to"
          class="nav-link"
          :class="{ active: route.path === link.to, locked: link.requiresAuth && !isLoggedIn }"
        >
          {{ link.label }}
        </RouterLink>
      </template>
      <template v-if="isLoggedIn">
        <button class="nav-link logout" type="button" @click="handleLogout">Выйти</button>
      </template>
      <template v-else>
        <RouterLink class="nav-link" to="/auth">Log in</RouterLink>
        <RouterLink class="nav-link" :to="{ path: '/auth', query: { tab: 'register' } }">Sign up</RouterLink>
      </template>
    </nav>

    <main v-if="isCatalog" class="full-catalog">
      <RouterView />
    </main>
    <main v-else class="layout">
      <section class="hero">
        <div class="hero-chip">Fixly Playground</div>
        <h1>Маркетплейс-интерфейс для всех эндпоинтов</h1>
        <p class="hero-text">
          Интерфейс похож на витрину объявлений: карточки, аккуратные формы и навигация как у Avito/Юлы.
          Сначала войдите через /auth, потом тестируйте API по разделам.
        </p>
        <ul class="hero-list">
          <li><span class="dot"></span>Регистрация/вход + подтверждение email</li>
          <li><span class="dot"></span>Объявления, аренды, избранное, профиль, чат и отзывы</li>
          <li><span class="dot"></span>Модерация и поддержка в отдельных разделах</li>
        </ul>
      </section>

      <section class="auth-card">
        <RouterView />
      </section>
    </main>
  </div>
</template>
