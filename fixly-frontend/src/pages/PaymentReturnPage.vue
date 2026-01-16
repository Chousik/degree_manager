<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import MainHeader from '../components/MainHeader.vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const error = ref('');
const payment = ref(null);
const { userId } = useSession();

const paymentId = computed(() => route.query.paymentId || '');

const statusLabel = computed(() => {
  if (!payment.value?.status) return '—';
  const status = payment.value.status.toLowerCase();
  if (status === 'succeeded') return 'Оплачено';
  if (status === 'pending') return 'Ожидает оплаты';
  if (status === 'canceled') return 'Отменено';
  return payment.value.status;
});

const loadPayment = async () => {
  if (!paymentId.value) {
    error.value = 'Не найден идентификатор платежа.';
    return;
  }
  loading.value = true;
  error.value = '';
  try {
    const data = await fetchJson(`${USER_API_BASE}/payments/${paymentId.value}`);
    payment.value = data || null;
    if (payment.value?.status?.toLowerCase() === 'succeeded' && payment.value?.purpose === 'RENTAL' && payment.value?.rentalId) {
      const depositResponse = await fetchJson(`${USER_API_BASE}/payments/rentals/${payment.value.rentalId}/initiate`, {
        method: 'POST',
        body: JSON.stringify({ actorId: userId.value, purpose: 'DEPOSIT' }),
      }).catch(() => null);
      if (depositResponse?.confirmationUrl) {
        window.location.href = depositResponse.confirmationUrl;
        return;
      }
    }
  } catch (err) {
    error.value = err.message || 'Не удалось проверить оплату.';
  } finally {
    loading.value = false;
  }
};

const goToRentals = () => {
  router.push({ path: '/account', query: { tab: 'active' } });
};

onMounted(() => {
  loadPayment();
});
</script>

<template>
  <div class="dashboard">
    <MainHeader />
    <section class="dashboard-section">
      <h2>Статус оплаты</h2>
      <p v-if="loading" class="dashboard-note">Проверяем оплату...</p>
      <p v-else-if="error" class="dashboard-note error">{{ error }}</p>
      <div v-else class="payment-card">
        <div class="payment-card__row">
          <span>Статус</span>
          <strong>{{ statusLabel }}</strong>
        </div>
        <div class="payment-card__row">
          <span>Сумма</span>
          <strong>{{ payment?.amount ? `${payment.amount} ₽` : '—' }}</strong>
        </div>
        <div class="payment-card__row">
          <span>Назначение</span>
          <strong>{{ payment?.purpose || '—' }}</strong>
        </div>
        <button class="btn primary" type="button" @click="goToRentals">К моим арендам</button>
      </div>
    </section>
  </div>
</template>
