import { computed, reactive } from 'vue';
import { getCity } from '../api/account';

const state = reactive({
  loggedIn: localStorage.getItem('fx_logged_in') === '1',
  pendingEmail: '',
  currentUserId: localStorage.getItem('fx_user_id') || '',
  accessToken: localStorage.getItem('fx_access') || '',
  refreshToken: localStorage.getItem('fx_refresh') || '',
  city: localStorage.getItem('fx_city') || 'Москва',
  cityLoaded: false,
});

function setLoggedIn(value) {
  state.loggedIn = value;
  localStorage.setItem('fx_logged_in', value ? '1' : '0');
}

function logout() {
  setLoggedIn(false);
  setUserId('');
  setTokens('', '');
  resetCity();
}

function setPendingEmail(email) {
  state.pendingEmail = email;
}

function setUserId(userId) {
  state.currentUserId = userId;
  if (userId) {
    localStorage.setItem('fx_user_id', userId);
  } else {
    localStorage.removeItem('fx_user_id');
  }
}

function decodeJwtPayload(token) {
  try {
    const payload = token.split('.')[1];
    const decoded = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decoded);
  } catch (e) {
    return null;
  }
}

function setUserIdFromToken(token) {
  const payload = decodeJwtPayload(token);
  if (!payload) return;
  const claim = payload.user_id || payload.uid || payload.sub;
  if (claim) {
    setUserId(String(claim));
  }
}

function setTokens(access, refresh) {
  state.accessToken = access || '';
  state.refreshToken = refresh || '';
  if (state.accessToken) {
    localStorage.setItem('fx_access', state.accessToken);
  } else {
    localStorage.removeItem('fx_access');
  }
  if (state.refreshToken) {
    localStorage.setItem('fx_refresh', state.refreshToken);
  } else {
    localStorage.removeItem('fx_refresh');
  }
  if (access) {
    setLoggedIn(true);
    setUserIdFromToken(access);
  }
}

function setCity(value) {
  const next = value || 'Москва';
  state.city = next;
  if (next) {
    localStorage.setItem('fx_city', next);
  } else {
    localStorage.removeItem('fx_city');
  }
}

function resetCity() {
  state.cityLoaded = false;
  setCity('Москва');
}

async function loadCityFromServer(force = false) {
  if (!state.currentUserId) {
    return;
  }
  if (state.cityLoaded && !force) {
    return;
  }
  try {
    const data = await getCity(state.currentUserId);
    if (data?.city) {
      setCity(data.city);
    }
  } catch (e) {
    // ignore, keep local city
  } finally {
    state.cityLoaded = true;
  }
}

export function useSession() {
  const isLoggedIn = computed(() => state.loggedIn);
  const userId = computed(() => state.currentUserId);
  const accessToken = computed(() => state.accessToken);
  const refreshToken = computed(() => state.refreshToken);
  const city = computed(() => state.city);
  return {
    state,
    isLoggedIn,
    userId,
    accessToken,
    refreshToken,
    city,
    setLoggedIn,
    logout,
    setPendingEmail,
    setUserId,
    setTokens,
    setUserIdFromToken,
    setCity,
    loadCityFromServer,
  };
}
