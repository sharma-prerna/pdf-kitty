export type JobStatus = 'UPLOADED' | 'QUEUED' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export type OperationType =
  | 'COMPRESS'
  | 'MERGE'
  | 'SPLIT'
  | 'PDF_TO_WORD'
  | 'IMAGE_TO_PDF'
  | 'PDF_TO_IMAGE';

export interface OutputFile {
  id: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
}

export interface Job {
  id: string;
  operationType: OperationType;
  status: JobStatus;
  originalFileName: string | null;
  outputFileName: string | null;
  errorMessage: string | null;
  createdAt: string;
  updatedAt: string;
  processingStartedAt: string | null;
  completedAt: string | null;
  outputs: OutputFile[];
}

export interface ApiError {
  timestamp: string;
  status: number;
  code: string;
  message: string;
  path: string;
  details: string[];
}

export const TERMINAL_STATUSES: JobStatus[] = ['COMPLETED', 'FAILED'];

export const isTerminal = (status: JobStatus): boolean => TERMINAL_STATUSES.includes(status);
