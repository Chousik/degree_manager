import { useSession } from '../state/session';

const API_BASE = (import.meta.env.VITE_API_BASE || 'http://localhost:8071').replace(/\/$/, '');
const USER_API_BASE = (import.meta.env.VITE_USER_API_BASE || 'http://localhost:8085/api').replace(/\/$/, '');
const EMAIL_VERIFY_ENDPOINT = import.meta.env.VITE_EMAIL_VERIFY_ENDPOINT || '/api/users/verify-email';
const EMAIL_VERIFY_PARAM = import.meta.env.VITE_EMAIL_VERIFY_PARAM || 'token';
const OAUTH_BASE = (import.meta.env.VITE_OAUTH_BASE || API_BASE).replace(/\/$/, '');
const OAUTH_REDIRECT = import.meta.env.VITE_OAUTH_REDIRECT || 'http://localhost:5173/auth-callback';

export { API_BASE, USER_API_BASE, EMAIL_VERIFY_ENDPOINT, EMAIL_VERIFY_PARAM, OAUTH_BASE, OAUTH_REDIRECT };

export async function fetchJson(url, options = {}) {
  const { accessToken, isLoggedIn, logout } = useSession();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const needsAuth = options.auth !== false;

  if (needsAuth && !isLoggedIn.value) {
    redirectToLogin();
    throw new Error('Требуется авторизация');
  }

  if (accessToken.value && needsAuth) {
    headers.Authorization = `Bearer ${accessToken.value}`;
  }

  const res = await fetch(url, {
    ...options,
    headers,
  });
  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) {
    if (needsAuth && res.status === 401) {
      logout();
      redirectToLogin();
    }
    throw new Error(data?.message || text || res.statusText);
  }
  return data;
}

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}
