import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  LinearProgress,
  Stack,
  Typography,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import { downloadUrl } from '../api/endpoints';
import type { Job, JobStatus } from '../types';

const STATUS_COLOR: Record<JobStatus, 'default' | 'info' | 'warning' | 'success' | 'error'> = {
  UPLOADED: 'default',
  QUEUED: 'info',
  PROCESSING: 'warning',
  COMPLETED: 'success',
  FAILED: 'error',
};

const formatTime = (iso: string | null): string => (iso ? new Date(iso).toLocaleString() : '—');

export default function JobStatusCard({ job }: { job: Job }) {
  const inProgress = job.status === 'QUEUED' || job.status === 'PROCESSING';
  const completed = job.status === 'COMPLETED';

  return (
    <Card>
      <CardContent>
        <Stack direction="row" alignItems="center" spacing={2} sx={{ mb: 2 }}>
          <Typography variant="h6">{job.operationType.replace(/_/g, ' ')}</Typography>
          <Chip label={job.status} color={STATUS_COLOR[job.status]} size="small" />
        </Stack>

        {inProgress && <LinearProgress sx={{ mb: 2 }} />}

        {job.status === 'FAILED' && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {job.errorMessage ?? 'Processing failed'}
          </Alert>
        )}

        <Stack spacing={0.5} sx={{ mb: 2 }}>
          <Detail label="Source" value={job.originalFileName ?? '—'} />
          <Detail label="Created" value={formatTime(job.createdAt)} />
          <Detail label="Started" value={formatTime(job.processingStartedAt)} />
          <Detail label="Completed" value={formatTime(job.completedAt)} />
        </Stack>

        {completed && (
          <>
            <Divider sx={{ my: 2 }} />
            <Stack direction="row" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography variant="subtitle2">Output</Typography>
                <Typography variant="body2" color="text.secondary">
                  {job.outputFileName ?? 'results'} · {job.outputs.length} file
                  {job.outputs.length === 1 ? '' : 's'}
                </Typography>
              </Box>
              <Button
                variant="contained"
                startIcon={<DownloadIcon />}
                href={downloadUrl(job.id)}
                component="a"
              >
                Download
              </Button>
            </Stack>
          </>
        )}
      </CardContent>
    </Card>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" spacing={1}>
      <Typography variant="body2" color="text.secondary" sx={{ minWidth: 90 }}>
        {label}
      </Typography>
      <Typography variant="body2" sx={{ wordBreak: 'break-all' }}>
        {value}
      </Typography>
    </Stack>
  );
}
