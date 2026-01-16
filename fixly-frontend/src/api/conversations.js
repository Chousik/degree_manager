import { USER_API_BASE, fetchJson } from './client';

export const startListingConversation = (listingId, payload) =>
  fetchJson(`${USER_API_BASE}/conversations/listings/${listingId}`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });

export const getConversations = (userId) =>
  fetchJson(`${USER_API_BASE}/conversations?userId=${userId}`);

export const getConversationMessages = (conversationId, userId) =>
  fetchJson(`${USER_API_BASE}/conversations/${conversationId}/messages?userId=${userId}`);

export const sendConversationMessage = (conversationId, payload) =>
  fetchJson(`${USER_API_BASE}/conversations/${conversationId}/messages`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
