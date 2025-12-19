<script setup>
import { reactive } from 'vue';
import { USER_API_BASE, fetchJson } from '../api/client';
import { useSession } from '../state/session';

const supportForms = reactive({
  ticket: { rentalId: '', subject: '', message: '' },
  resolve: { ticketId: '', resolutionNotes: '' },
});

const apiResults = reactive({
  ticket: null,
  open: null,
  resolved: null,
});
const toast = reactive({ message: '', type: 'success', visible: false });
const { userId } = useSession();
const showToast = (message, type = 'success') => {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
};
const toJson = (val) => (val ? JSON.stringify(val, null, 2) : '');

const createTicket = async () => {
  if (!userId.value) {
    showToast('В токене должен быть userId/sub — войдите заново', 'error');
    return;
  }
  const payload = { requesterId: userId.value, ...supportForms.ticket };
  apiResults.ticket = await fetchJson(`${USER_API_BASE}/support/tickets`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
  showToast('Тикет создан');
};

const loadOpenTickets = async () => {
  apiResults.open = await fetchJson(`${USER_API_BASE}/support/tickets/open`);
};

const resolveTicket = async () => {
  const { ticketId, resolutionNotes } = supportForms.resolve;
  if (!ticketId || !resolutionNotes || !userId.value) {
    showToast('Укажите ticketId, решение; в токене должен быть userId/sub', 'error');
    return;
  }
  apiResults.resolved = await fetchJson(`${USER_API_BASE}/support/tickets/${ticketId}/resolve`, {
    method: 'POST',
    body: JSON.stringify({ adminId: userId.value, resolutionNotes }),
  });
  showToast('Тикет обработан');
};
</script>

<template>
  <div class="api-playground">
    <div class="section">
      <h3>Создать тикет</h3>
      <div class="grid">
        <div class="field"><label>RentalId (опц.)</label><input v-model="supportForms.ticket.rentalId" placeholder="UUID или пусто"></div>
        <div class="field"><label>Тема</label><input v-model="supportForms.ticket.subject"></div>
        <div class="field full"><label>Сообщение</label><textarea v-model="supportForms.ticket.message" rows="2"></textarea></div>
      </div>
      <button class="btn primary" @click="createTicket">Отправить тикет</button>
      <pre v-if="apiResults.ticket">{{ toJson(apiResults.ticket) }}</pre>
    </div>

    <div class="section">
      <h3>Открытые тикеты</h3>
      <button class="btn" @click="loadOpenTickets">Загрузить открытые</button>
      <pre v-if="apiResults.open">{{ toJson(apiResults.open) }}</pre>
    </div>

    <div class="section">
      <h3>Решение тикета</h3>
      <div class="grid">
        <div class="field"><label>TicketId</label><input v-model="supportForms.resolve.ticketId" placeholder="UUID"></div>
        <div class="field full"><label>Решение</label><textarea v-model="supportForms.resolve.resolutionNotes" rows="2"></textarea></div>
      </div>
      <button class="btn primary" @click="resolveTicket">Закрыть тикет</button>
      <pre v-if="apiResults.resolved">{{ toJson(apiResults.resolved) }}</pre>
    </div>

    <div v-if="toast.visible" class="toast" :class="toast.type">
      {{ toast.message }}
    </div>
  </div>
</template>
