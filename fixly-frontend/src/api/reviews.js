import { USER_API_BASE, fetchJson } from './client';

export const getLessorReviews = (lessorId) =>
  fetchJson(`${USER_API_BASE}/reviews/lessor?lessorId=${lessorId}`, { auth: false });
