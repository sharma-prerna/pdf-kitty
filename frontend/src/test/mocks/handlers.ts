import { http, HttpResponse } from 'msw';
import type { Job } from '../../types';

export const completedJob: Job = {
  id: 'job-123',
  operationType: 'MERGE',
  status: 'COMPLETED',
  originalFileName: 'a.pdf (+1 more)',
  outputFileName: 'merged.pdf',
  errorMessage: null,
  createdAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T10:00:05Z',
  processingStartedAt: '2026-01-01T10:00:01Z',
  completedAt: '2026-01-01T10:00:05Z',
  outputs: [{ id: 'out-1', fileName: 'merged.pdf', fileSize: 2048, mimeType: 'application/pdf' }],
};

export const handlers = [
  http.get('/api/jobs/:id', () => HttpResponse.json(completedJob)),
  http.get('/api/jobs', () => HttpResponse.json([completedJob])),
  http.post('/api/pdf/merge', () =>
    HttpResponse.json({ ...completedJob, status: 'QUEUED' }, { status: 202 }),
  ),
];
