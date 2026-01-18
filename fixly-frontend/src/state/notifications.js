import { computed, reactive } from 'vue';

const state = reactive({
  items: [],
});

function setNotifications(list) {
  state.items = Array.isArray(list) ? list : [];
}

function upsertNotification(item) {
  if (!item || !item.id) {
    return;
  }
  const index = state.items.findIndex((entry) => entry.id === item.id);
  if (index >= 0) {
    state.items[index] = item;
  } else {
    state.items = [item, ...state.items];
  }
}

function clearNotifications() {
  state.items = [];
}

export function useNotifications() {
  return {
    notifications: computed(() => state.items),
    setNotifications,
    upsertNotification,
    clearNotifications,
  };
}
