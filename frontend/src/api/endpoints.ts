import { api } from './client';
import type { Job } from '../types';

/** Submit an operation: POST files + params as multipart/form-data, returns the queued job. */
export async function submitOperation(
  endpoint: string,
  files: File[],
  params: Record<string, string>,
): Promise<Job> {
  const form = new FormData();
  files.forEach((file) => form.append('files', file));
  Object.entries(params).forEach(([key, value]) => form.append(key, value));
  const { data } = await api.post<Job>(endpoint, form);
  return data;
}

export async function fetchJob(id: string): Promise<Job> {
  const { data } = await api.get<Job>(`/jobs/${id}`);
  return data;
}

export async function fetchJobHistory(page = 0, size = 50): Promise<Job[]> {
  const { data } = await api.get<Job[]>('/jobs', { params: { page, size } });
  return data;
}

/** Absolute download URL for a completed job (single file or ZIP). */
export const downloadUrl = (id: string): string => `${import.meta.env.VITE_API_BASE_URL}/api/download/${id}`;
