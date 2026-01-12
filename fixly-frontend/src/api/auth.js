import { OAUTH_BASE, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET } from './client';

export async function refreshTokens(refreshToken) {
  const response = await fetch(`${OAUTH_BASE}/oauth2/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      Authorization: 'Basic ' + btoa(`${OAUTH_CLIENT_ID}:${OAUTH_CLIENT_SECRET}`),
    },
    body: new URLSearchParams({
      grant_type: 'refresh_token',
      refresh_token: refreshToken,
    }),
  });

  if (!response.ok) {
    throw new Error('Не удалось обновить токен');
  }

  return response.json();
}
