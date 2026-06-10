import type { ApiErrorPayload } from '@/types';

export function getApiErrorMessage(error: unknown, fallback = '请求失败，请稍后重试'): string {
  if (error && typeof error === 'object' && 'message' in error) {
    const message = (error as ApiErrorPayload).message;
    if (typeof message === 'string' && message.trim()) {
      return message;
    }
  }

  return fallback;
}

