<script setup>
import MainHeader from '../components/MainHeader.vue';

const notifications = [
  { id: 1, title: 'Новый отклик на объявление', body: 'Пользователь Иван откликнулся на ваше объявление "Перфоратор"', time: '5 минут назад' },
  { id: 2, title: 'Платёж завершён', body: 'Аренда #12345 успешно оплачена.', time: 'Вчера' },
];
</script>

<template>
  <div class="notifications-page">
    <MainHeader />
    <section class="notifications-section">
      <div class="notifications-header">
        <div class="notifications-title">Уведомления</div>
      </div>

      <div v-if="notifications.length" class="notifications-list">
        <article v-for="item in notifications" :key="item.id" class="notification-card">
          <div class="notification-card__head">
            <h3>{{ item.title }}</h3>
            <span>{{ item.time }}</span>
          </div>
          <p>{{ item.body }}</p>
        </article>
      </div>
      <div v-else class="notifications-empty">
        Пока нет уведомлений
      </div>
    </section>
  </div>
</template>
