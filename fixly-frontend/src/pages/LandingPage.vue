<script setup>
import { useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';

const router = useRouter();

const categories = Array.from({ length: 6 }).map((_, idx) => ({
  title: 'Название категории',
  id: idx,
}));

const products = Array.from({ length: 8 }).map((_, idx) => ({
  title: 'Название товара',
  price: '000 ₽',
  id: idx,
}));

const goToLogin = () => router.push('/login');
const goToCatalog = () => router.push('/catalog');
</script>

<template>
  <div class="landing">
    <MainHeader />

    <div class="landing-actions">
      <div class="landing-buttons">
        <button type="button" class="landing-btn primary">Все категории</button>
      </div>
      <div class="landing-search">
        <input type="text" placeholder="Найдите инструмент, услугу или продавца">
        <button type="button">Найти</button>
      </div>
    </div>

    <section class="landing-section">
      <div class="landing-section__title">Категории</div>
      <div class="landing-grid categories">
        <article v-for="category in categories" :key="category.id" class="landing-card category">
          <div class="landing-card__image"></div>
          <div class="landing-card__title">{{ category.title }}</div>
        </article>
      </div>
    </section>

    <section class="landing-section">
      <div class="landing-section__header">
        <div class="landing-section__title">Для вас</div>
        <button class="landing-link" type="button" @click="goToCatalog">Перейти в каталог →</button>
      </div>
      <div class="landing-grid products">
        <article v-for="item in products" :key="item.id" class="landing-card product">
          <div class="landing-card__image large"></div>
          <div class="landing-card__text">
            <div class="landing-card__title">{{ item.title }}</div>
            <div class="landing-card__price">{{ item.price }}</div>
          </div>
        </article>
      </div>
    </section>

    <div class="landing-cta">
      <div>
        <h3>Готовы тестировать Fixly?</h3>
        <p>Войдите в песочницу и управляйте объявлениями, арендой и поддержкой.</p>
      </div>
      <div class="landing-cta__buttons">
        <button class="landing-btn primary" type="button" @click="goToLogin">Войти</button>
        <button class="landing-btn ghost" type="button" @click="router.push('/register')">Зарегистрироваться</button>
      </div>
    </div>
  </div>
</template>
