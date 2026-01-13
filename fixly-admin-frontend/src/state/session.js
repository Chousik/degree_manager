import { computed, reactive } from 'vue';

const TOKEN_LEEWAY_MS = 5000;
const STORAGE_PREFIX = 'fx_admin_';

const state = reactive({
  loggedIn: localStorage.getItem(`${STORAGE_PREFIX}logged_in`) === '1',
  currentUserId: localStorage.getItem(`${STORAGE_PREFIX}user_id`) || '',
  accessToken: localStorage.getItem(`${STORAGE_PREFIX}access`) || '',
  refreshToken: localStorage.getItem(`${STORAGE_PREFIX}refresh`) || '',
  roles: JSON.parse(localStorage.getItem(`${STORAGE_PREFIX}roles`) || '[]'),
});

function setLoggedIn(value) {
  state.loggedIn = value;
  localStorage.setItem(`${STORAGE_PREFIX}logged_in`, value ? '1' : '0');
}

function logout() {
  setLoggedIn(false);
  setUserId('');
  setTokens('', '');
  setRoles([]);
}

function setUserId(userId) {
  state.currentUserId = userId;
  if (userId) {
    localStorage.setItem(`${STORAGE_PREFIX}user_id`, userId);
  } else {
    localStorage.removeItem(`${STORAGE_PREFIX}user_id`);
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

function setRolesFromToken(token) {
  const payload = decodeJwtPayload(token);
  if (!payload) {
    setRoles([]);
    return;
  }
  const roles = payload.roles || [];
  setRoles(Array.isArray(roles) ? roles : []);
}

function setUserIdFromToken(token) {
  const payload = decodeJwtPayload(token);
  if (!payload) return;
  const claim = payload.user_id || payload.uid || payload.sub;
  if (claim) {
    setUserId(String(claim));
  }
}

function setRoles(roles) {
  state.roles = roles;
  localStorage.setItem(`${STORAGE_PREFIX}roles`, JSON.stringify(roles));
}

function setTokens(access, refresh) {
  const normalizedAccess = access && isTokenActive(access) ? access : '';
  state.accessToken = normalizedAccess;
  state.refreshToken = refresh || '';
  if (state.accessToken) {
    localStorage.setItem(`${STORAGE_PREFIX}access`, state.accessToken);
  } else {
    localStorage.removeItem(`${STORAGE_PREFIX}access`);
  }
  if (state.refreshToken) {
    localStorage.setItem(`${STORAGE_PREFIX}refresh`, state.refreshToken);
  } else {
    localStorage.removeItem(`${STORAGE_PREFIX}refresh`);
  }
  if (normalizedAccess) {
    setLoggedIn(true);
    setUserIdFromToken(normalizedAccess);
    setRolesFromToken(normalizedAccess);
  } else {
    setLoggedIn(false);
    setUserId('');
    setRoles([]);
  }
}

function syncSessionWithToken() {
  if (isTokenActive(state.accessToken)) {
    setLoggedIn(true);
    setUserIdFromToken(state.accessToken);
    setRolesFromToken(state.accessToken);
  } else {
    if (state.accessToken) {
      state.accessToken = '';
      localStorage.removeItem(`${STORAGE_PREFIX}access`);
    }
    setLoggedIn(false);
    if (state.currentUserId) {
      setUserId('');
    }
    setRoles([]);
  }
}

syncSessionWithToken();

export function useSession() {
  const isLoggedIn = computed(() => state.loggedIn);
  const userId = computed(() => state.currentUserId);
  const accessToken = computed(() => state.accessToken);
  const refreshToken = computed(() => state.refreshToken);
  const hasValidAccessToken = computed(() => isTokenActive(state.accessToken));
  const roles = computed(() => state.roles);
  const isAdmin = computed(() => state.roles.includes('ROLE_ADMIN'));
  return {
    state,
    isLoggedIn,
    userId,
    accessToken,
    refreshToken,
    roles,
    isAdmin,
    hasValidAccessToken,
    setLoggedIn,
    logout,
    setUserId,
    setTokens,
  };
}
