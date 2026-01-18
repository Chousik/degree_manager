import { computed, reactive } from 'vue';

const MAX_EVENTS = 100;

const state = reactive({
  messageEvents: [],
  rentalEvents: [],
});

function pushEvent(list, payload) {
  if (!payload) {
    return;
  }
  list.unshift(payload);
  if (list.length > MAX_EVENTS) {
    list.length = MAX_EVENTS;
  }
}

function addMessageEvent(payload) {
  pushEvent(state.messageEvents, payload);
}

function addRentalEvent(payload) {
  pushEvent(state.rentalEvents, payload);
}

function clearEvents() {
  state.messageEvents = [];
  state.rentalEvents = [];
}

export function useRealtime() {
  return {
    messageEvents: computed(() => state.messageEvents),
    rentalEvents: computed(() => state.rentalEvents),
    addMessageEvent,
    addRentalEvent,
    clearEvents,
  };
}
