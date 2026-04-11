import { BACKEND_ORIGIN } from '../services/api';

export const resolveBookImageUrl = (imageUrl) => {
  if (!imageUrl) return '';
  if (/^https?:\/\//i.test(imageUrl)) return imageUrl;
  if (imageUrl.startsWith('/')) return `${BACKEND_ORIGIN}${imageUrl}`;
  return `${BACKEND_ORIGIN}/${imageUrl}`;
};
