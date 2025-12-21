<script setup>
import MainHeader from '../components/MainHeader.vue';

const favorites = [
  { id: 1, title: 'Аренда перфоратора Bosch', description: '8.5 ₽/час · Без залога' },
  { id: 2, title: 'Компрессор 50 л', description: '7.5 ₽/час · Залог 50 ₽' },
  { id: 3, title: 'Шлифмашина эксцентриковая', description: '4 ₽/час · Самовывоз' },
];
</script>

<template>
  <div class="favorites-page">
    <MainHeader />
    <section class="favorites-section">
      <div class="favorites-header">
        <div class="favorites-title">Избранные</div>
      </div>

      <div v-if="favorites.length" class="favorites-list">
        <article v-for="item in favorites" :key="item.id" class="favorites-card">
          <div class="favorites-card__title">{{ item.title }}</div>
          <p>{{ item.description }}</p>
        </article>
      </div>
      <div v-else class="favorites-empty">
        У вас пока нет избранных объявлений
      </div>
    </section>
  </div>
</template>
