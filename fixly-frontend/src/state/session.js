import { computed, reactive } from 'vue';

const state = reactive({
  loggedIn: localStorage.getItem('fx_logged_in') === '1',
  pendingEmail: '',
  currentUserId: localStorage.getItem('fx_user_id') || '',
  accessToken: localStorage.getItem('fx_access') || '',
  refreshToken: localStorage.getItem('fx_refresh') || '',
});

function setLoggedIn(value) {
  state.loggedIn = value;
  localStorage.setItem('fx_logged_in', value ? '1' : '0');
}

function logout() {
  setLoggedIn(false);
  setUserId('');
  setTokens('', '');
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

export function useSession() {
  const isLoggedIn = computed(() => state.loggedIn);
  const userId = computed(() => state.currentUserId);
  const accessToken = computed(() => state.accessToken);
  const refreshToken = computed(() => state.refreshToken);
  return {
    state,
    isLoggedIn,
    userId,
    accessToken,
    refreshToken,
    setLoggedIn,
    logout,
    setPendingEmail,
    setUserId,
    setTokens,
    setUserIdFromToken,
  };
}
