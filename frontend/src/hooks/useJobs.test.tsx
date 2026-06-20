import { ReactNode } from 'react';
import { describe, expect, it } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useJob, useJobHistory } from './useJobs';

function wrapper() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
}

describe('job hooks', () => {
  it('useJob fetches a job by id', async () => {
    const { result } = renderHook(() => useJob('job-123'), { wrapper: wrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data?.status).toBe('COMPLETED');
    expect(result.current.data?.outputs).toHaveLength(1);
  });

  it('useJobHistory returns the list of jobs', async () => {
    const { result } = renderHook(() => useJobHistory(), { wrapper: wrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toHaveLength(1);
  });
});
