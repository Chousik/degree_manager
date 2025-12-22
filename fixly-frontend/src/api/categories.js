import { USER_API_BASE, fetchJson } from './client';

export function getCategories() {
  return fetchJson(`${USER_API_BASE}/categories`);
}
