import { useParams, useNavigate } from 'react-router-dom';
import { Alert, Box, Button, CircularProgress, Stack } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useJob } from '../hooks/useJobs';
import JobStatusCard from '../components/JobStatusCard';
import { toMessage } from '../api/client';

export default function ProcessingPage() {
  const { jobId } = useParams();
  const navigate = useNavigate();
  const { data: job, isLoading, isError, error } = useJob(jobId);

  return (
    <Box>
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/')} sx={{ mb: 2 }}>
        All tools
      </Button>

      {isLoading && (
        <Stack alignItems="center" sx={{ py: 6 }}>
          <CircularProgress />
        </Stack>
      )}

      {isError && <Alert severity="error">{toMessage(error)}</Alert>}

      {job && (
        <Stack spacing={2}>
          <JobStatusCard job={job} />
          <Box>
            <Button onClick={() => navigate('/history')}>View all jobs</Button>
          </Box>
        </Stack>
      )}
    </Box>
  );
}
