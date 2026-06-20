import { useMutation, useQuery } from '@tanstack/react-query';
import { fetchJob, fetchJobHistory, submitOperation } from '../api/endpoints';
import { isTerminal, type Job } from '../types';

/** Poll a single job until it reaches a terminal status. */
export function useJob(id: string | undefined) {
  return useQuery<Job>({
    queryKey: ['job', id],
    queryFn: () => fetchJob(id as string),
    enabled: Boolean(id),
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status && isTerminal(status) ? false : 1500;
    },
  });
}

export function useJobHistory() {
  return useQuery<Job[]>({
    queryKey: ['jobs'],
    queryFn: () => fetchJobHistory(),
    refetchInterval: 5000,
  });
}

/** Submit an operation and return the created job. */
export function useSubmitOperation(endpoint: string) {
  return useMutation<Job, unknown, { files: File[]; params: Record<string, string> }>({
    mutationFn: ({ files, params }) => submitOperation(endpoint, files, params),
  });
}
