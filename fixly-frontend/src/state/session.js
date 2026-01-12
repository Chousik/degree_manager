import { computed, reactive } from 'vue';
import { getCity } from '../api/account';

const TOKEN_LEEWAY_MS = 5000;

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

function isTokenActive(token) {
  if (!token) {
    return false;
  }
  const payload = decodeJwtPayload(token);
  if (!payload?.exp) {
    return false;
  }
  const expiresAtMs = payload.exp * 1000;
  return expiresAtMs - TOKEN_LEEWAY_MS > Date.now();
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
  const normalizedAccess = access && isTokenActive(access) ? access : '';
  state.accessToken = normalizedAccess;
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
  if (normalizedAccess) {
    setLoggedIn(true);
    setUserIdFromToken(normalizedAccess);
  } else {
    setLoggedIn(false);
    setUserId('');
  }
}

function syncSessionWithToken() {
  if (isTokenActive(state.accessToken)) {
    setLoggedIn(true);
    setUserIdFromToken(state.accessToken);
  } else {
    if (state.accessToken) {
      state.accessToken = '';
      localStorage.removeItem('fx_access');
    }
    setLoggedIn(false);
    if (state.currentUserId) {
      setUserId('');
    }
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

syncSessionWithToken();

export function useSession() {
  const isLoggedIn = computed(() => state.loggedIn);
  const userId = computed(() => state.currentUserId);
  const accessToken = computed(() => state.accessToken);
  const refreshToken = computed(() => state.refreshToken);
  const city = computed(() => state.city);
  const hasValidAccessToken = computed(() => isTokenActive(state.accessToken));
  return {
    state,
    isLoggedIn,
    userId,
    accessToken,
    refreshToken,
    hasValidAccessToken,
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
