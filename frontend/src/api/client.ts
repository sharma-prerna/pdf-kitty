import axios, { AxiosError } from 'axios';
import type { ApiError } from '../types';

/**
 * Shared Axios instance. In dev the Vite proxy forwards /api to the backend; in production
 * Nginx proxies /api to the backend container.
 */
export const api = axios.create({
  baseURL: '/api',
});

/** Normalise an Axios error into a readable message from the backend ErrorResponse. */
export function toMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>;
    const data = axiosError.response?.data;
    if (data?.message) {
      return data.details?.length ? `${data.message}: ${data.details.join(', ')}` : data.message;
    }
    return axiosError.message;
  }
  return error instanceof Error ? error.message : 'Unexpected error';
}
