import { USER_API_BASE, fetchJson } from './client';

export function getCity() {
  return fetchJson(`${USER_API_BASE}/account/me/city`);
}

export function updateCity(city) {
  return fetchJson(`${USER_API_BASE}/account/me/city`, {
    method: 'PUT',
    body: JSON.stringify({ city }),
  });
}
