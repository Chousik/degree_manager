import { USER_API_BASE, fetchJson } from './client';

export function getCity(userId) {
  return fetchJson(`${USER_API_BASE}/account/${userId}/city`);
}

export function updateCity(userId, city) {
  return fetchJson(`${USER_API_BASE}/account/${userId}/city`, {
    method: 'PUT',
    body: JSON.stringify({ city }),
  });
}
