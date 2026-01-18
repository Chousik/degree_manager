import { useSession } from '../state/session';
import { refreshTokens } from './auth';

const API_BASE = (import.meta.env.VITE_API_BASE || 'https://fixly-meow.ru:8092/auth-service').replace(/\/$/, '');
const USER_API_BASE = (import.meta.env.VITE_USER_API_BASE || 'https://fixly-meow.ru:8092/user-service/api').replace(/\/$/, '');
const EMAIL_VERIFY_ENDPOINT = import.meta.env.VITE_EMAIL_VERIFY_ENDPOINT || '/api/users/verify-email';
const EMAIL_VERIFY_PARAM = import.meta.env.VITE_EMAIL_VERIFY_PARAM || 'token';
const OAUTH_BASE = (import.meta.env.VITE_OAUTH_BASE || API_BASE).replace(/\/$/, '');
const OAUTH_REDIRECT = import.meta.env.VITE_OAUTH_REDIRECT || 'http://localhost:5173/auth-callback';
const OAUTH_CLIENT_ID = import.meta.env.VITE_OAUTH_CLIENT_ID || 'client';
const OAUTH_CLIENT_SECRET = import.meta.env.VITE_OAUTH_CLIENT_SECRET || 'secret';
const OAUTH_SCOPE = import.meta.env.VITE_OAUTH_SCOPE || 'openid offline_access';

export {
  API_BASE,
  USER_API_BASE,
  EMAIL_VERIFY_ENDPOINT,
  EMAIL_VERIFY_PARAM,
  OAUTH_BASE,
  OAUTH_REDIRECT,
  OAUTH_CLIENT_ID,
  OAUTH_CLIENT_SECRET,
  OAUTH_SCOPE,
};

export async function fetchJson(url, options = {}) {
  const session = useSession();
  const { accessToken, refreshToken, hasValidAccessToken, logout, setTokens } = session;

  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const needsAuth = options.auth !== false;

  const ensureFreshAccessToken = async () => {
    if (!needsAuth || hasValidAccessToken.value) {
      return;
    }
    if (!refreshToken.value) {
      throw new Error('Нет refresh токена');
    }
    const refreshResponse = await refreshTokens(refreshToken.value);
    setTokens(refreshResponse.access_token, refreshResponse.refresh_token || refreshToken.value);
    if (!hasValidAccessToken.value) {
      throw new Error('Не удалось обновить токен');
    }
  };

  if (needsAuth) {
    try {
      await ensureFreshAccessToken();
    } catch (err) {
      logout();
      redirectToLogin();
      throw new Error('Требуется авторизация');
    }
    if (!hasValidAccessToken.value) {
      logout();
      redirectToLogin();
      throw new Error('Требуется авторизация');
    }
  }

  const attemptRequest = async (tokenOverride) => {
    const authHeaders = { ...headers };
    if (tokenOverride && needsAuth) {
      authHeaders.Authorization = `Bearer ${tokenOverride}`;
    }

    const res = await fetch(url, {
      ...options,
      headers: authHeaders,
    });
    return res;
  };

  let response = await attemptRequest(accessToken.value);

  if (needsAuth && response.status === 401 && refreshToken.value) {
    try {
      const refreshResponse = await refreshTokens(refreshToken.value);
      setTokens(refreshResponse.access_token, refreshResponse.refresh_token || refreshToken.value);
      response = await attemptRequest(refreshResponse.access_token);
    } catch (err) {
      logout();
      redirectToLogin();
      throw err;
    }
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;

  if (!response.ok) {
    if (needsAuth && response.status === 401) {
      logout();
      redirectToLogin();
    }
    throw new Error(data?.message || text || response.statusText);
  }

  return data;
}

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}
