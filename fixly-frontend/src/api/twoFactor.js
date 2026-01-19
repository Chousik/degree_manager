import { API_BASE, fetchJson } from './client';

export function getTwoFactorStatus() {
  return fetchJson(`${API_BASE}/users/2fa`);
}

export function startTwoFactorSetup() {
  return fetchJson(`${API_BASE}/users/2fa/setup`, { method: 'POST' });
}

export function confirmTwoFactor(code) {
  return fetchJson(`${API_BASE}/users/2fa/enable`, {
    method: 'POST',
    body: JSON.stringify({ code }),
  });
}

export function disableTwoFactor(code) {
  return fetchJson(`${API_BASE}/users/2fa/disable`, {
    method: 'POST',
    body: JSON.stringify({ code }),
  });
}
