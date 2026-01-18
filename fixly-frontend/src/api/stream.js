import { USER_API_BASE } from './client';

const streamBase = (import.meta.env.VITE_WS_BASE || USER_API_BASE.replace(/\/api\/?$/, '')).replace(/\/$/, '');
const rawStreamEndpoint = (import.meta.env.VITE_STREAM_WS_ENDPOINT || '/ws/stream').trim();

const normalizeEndpoint = () => {
  if (/^wss?:\/\//i.test(rawStreamEndpoint) || /^https?:\/\//i.test(rawStreamEndpoint)) {
    return rawStreamEndpoint;
  }
  const sanitized = rawStreamEndpoint.replace(/^\/+/, '');
  return `${streamBase}/${sanitized}`;
};

const toWebSocketUrl = (url) => {
  if (/^wss?:\/\//i.test(url)) {
    return url;
  }
  if (/^https:\/\//i.test(url)) {
    return `wss://${url.slice('https://'.length)}`;
  }
  if (/^http:\/\//i.test(url)) {
    return `ws://${url.slice('http://'.length)}`;
  }
  return url;
};

const buildWsUrl = (userId) => {
  if (!userId) {
    throw new Error('userId is required to subscribe realtime stream');
  }
  const template = toWebSocketUrl(normalizeEndpoint());
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

export function openRealtimeStream(userId, handlers = {}) {
  if (!userId) {
    return () => {};
  }
  const url = buildWsUrl(userId);
  const socket = new WebSocket(url);
  const { onMessage, onOpen, onClose, onError } = handlers;

  socket.addEventListener('open', () => {
    if (typeof onOpen === 'function') {
      onOpen();
    }
  });

  socket.addEventListener('message', (event) => {
    if (typeof onMessage !== 'function') {
      return;
    }
    try {
      const data = JSON.parse(event.data);
      onMessage(data);
    } catch (err) {
      // ignore malformed payloads
    }
  });

  socket.addEventListener('close', () => {
    if (typeof onClose === 'function') {
      onClose();
    }
  });

  socket.addEventListener('error', () => {
    if (typeof onError === 'function') {
      onError();
    }
  });

  return () => {
    socket.close();
  };
}
