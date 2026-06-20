import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import { useNavigate } from 'react-router-dom';
import { useJobHistory } from '../hooks/useJobs';
import { downloadUrl } from '../api/endpoints';
import { toMessage } from '../api/client';
import type { JobStatus } from '../types';

const STATUS_COLOR: Record<JobStatus, 'default' | 'info' | 'warning' | 'success' | 'error'> = {
  UPLOADED: 'default',
  QUEUED: 'info',
  PROCESSING: 'warning',
  COMPLETED: 'success',
  FAILED: 'error',
};

export default function HistoryPage() {
  const navigate = useNavigate();
  const { data: jobs, isLoading, isError, error } = useJobHistory();

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        History
      </Typography>

      {isLoading && (
        <Stack alignItems="center" sx={{ py: 6 }}>
          <CircularProgress />
        </Stack>
      )}
      {isError && <Alert severity="error">{toMessage(error)}</Alert>}

      {jobs && jobs.length === 0 && (
        <Typography color="text.secondary">No jobs yet. Run a tool to see it here.</Typography>
      )}

      {jobs && jobs.length > 0 && (
        <Paper variant="outlined">
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Operation</TableCell>
                <TableCell>Source</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Created</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {jobs.map((job) => (
                <TableRow key={job.id} hover>
                  <TableCell>{job.operationType.replace(/_/g, ' ')}</TableCell>
                  <TableCell sx={{ maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {job.originalFileName ?? '—'}
                  </TableCell>
                  <TableCell>
                    <Chip label={job.status} color={STATUS_COLOR[job.status]} size="small" />
                  </TableCell>
                  <TableCell>{new Date(job.createdAt).toLocaleString()}</TableCell>
                  <TableCell align="right">
                    <Stack direction="row" spacing={1} justifyContent="flex-end">
                      <Button size="small" onClick={() => navigate(`/jobs/${job.id}`)}>
                        Details
                      </Button>
                      {job.status === 'COMPLETED' && (
                        <Button
                          size="small"
                          variant="outlined"
                          startIcon={<DownloadIcon />}
                          href={downloadUrl(job.id)}
                          component="a"
                        >
                          Download
                        </Button>
                      )}
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      )}
    </Box>
  );
}
