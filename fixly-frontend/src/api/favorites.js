import { USER_API_BASE, fetchJson } from './client';

const FAVORITES_BASE = `${USER_API_BASE}/favorites`;

export function getFavorites(userId) {
  if (!userId) {
    return Promise.reject(new Error('userId is required to load favorites'));
  }
  return fetchJson(`${FAVORITES_BASE}?userId=${encodeURIComponent(userId)}`);
}

export function addFavorite(userId, listingId) {
  if (!userId || !listingId) {
    return Promise.reject(new Error('userId and listingId are required to add favorites'));
  }
  return fetchJson(`${FAVORITES_BASE}`, {
    method: 'POST',
    body: JSON.stringify({ userId, listingId }),
  });
}

export function removeFavorite(userId, listingId) {
  if (!userId || !listingId) {
    return Promise.reject(new Error('userId and listingId are required to remove favorites'));
  }
  return fetchJson(`${FAVORITES_BASE}/${encodeURIComponent(listingId)}?userId=${encodeURIComponent(userId)}`, {
    method: 'DELETE',
  });
}
