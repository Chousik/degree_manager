import { USER_API_BASE, fetchJson } from './client';

const notificationsApiBase = (import.meta.env.VITE_NOTIFICATIONS_API_BASE || USER_API_BASE).replace(/\/$/, '');
const rawEndpoint = (import.meta.env.VITE_NOTIFICATIONS_ENDPOINT || '/notifications').trim();

const normalizeEndpoint = () => {
  if (/^https?:\/\//i.test(rawEndpoint)) {
    return rawEndpoint;
  }
  const sanitized = rawEndpoint.replace(/^\/+/, '');
  return `${notificationsApiBase}/${sanitized}`;
};

const buildUrl = (userId) => {
  if (!userId) {
    throw new Error('userId is required to load notifications');
  }
  const template = normalizeEndpoint();
  if (template.includes('{userId}')) {
    return template.replace(/\{userId\}/g, encodeURIComponent(userId));
  }
  if (template.includes(':userId')) {
    return template.replace(/:userId/g, encodeURIComponent(userId));
  }
  const separator = template.includes('?')
    ? template.endsWith('?') || template.endsWith('&')
      ? ''
      : '&'
    : '?';
  return `${template}${separator}userId=${encodeURIComponent(userId)}`;
};

export async function getUserNotifications(userId) {
  if (!userId) {
    return [];
  }
  const url = buildUrl(userId);
  return fetchJson(url);
}
