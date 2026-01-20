const EMOJI_REGEX = /[\p{Extended_Pictographic}\u200d\uFE0F]/u;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const USERNAME_REGEX = /^[a-zA-Z0-9._-]{3,30}$/;
const NAME_REGEX = /^[A-Za-zА-Яа-яЁё' -]+$/;

export const hasEmoji = (value = '') => EMOJI_REGEX.test(value);

export const isValidEmail = (value = '') => EMAIL_REGEX.test(value.trim());

export const isValidUsername = (value = '') => USERNAME_REGEX.test(value.trim());

export const isValidName = (value = '') => NAME_REGEX.test(value.trim());

export const normalizePhone = (value = '') => {
  const trimmed = value.trim();
  if (!trimmed) return '';
  const digits = trimmed.replace(/\D/g, '');
  if (!digits) return '';
  if (digits.length === 10) {
    return `+7${digits}`;
  }
  if (digits.length === 11 && (digits.startsWith('7') || digits.startsWith('8'))) {
    return `+7${digits.slice(1)}`;
  }
  return trimmed.startsWith('+') ? `+${digits}` : digits;
};

export const isValidPhone = (value = '') => {
  if (!value) return true;
  return /^\+7\d{10}$/.test(value);
};

export const isValidOtp = (value = '') => /^\d{6}$/.test(value.trim());
