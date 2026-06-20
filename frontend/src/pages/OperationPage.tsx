import { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { operationByKey, type ControlDef } from '../operations';
import FileDropzone from '../components/FileDropzone';
import FileList from '../components/FileList';
import { useSubmitOperation } from '../hooks/useJobs';
import { toMessage } from '../api/client';

export default function OperationPage() {
  const { opKey } = useParams();
  const navigate = useNavigate();
  const op = operationByKey(opKey ?? '');

  const [files, setFiles] = useState<File[]>([]);
  const [params, setParams] = useState<Record<string, string>>(
    () => (op ? Object.fromEntries(op.controls.map((c) => [c.name, c.default])) : {}),
  );

  const mutation = useSubmitOperation(op?.endpoint ?? '');

  const visibleControls = useMemo(
    () => (op ? op.controls.filter((c) => !c.showIf || c.showIf(params)) : []),
    [op, params],
  );

  if (!op) {
    return <Alert severity="error">Unknown tool. <Button onClick={() => navigate('/')}>Go back</Button></Alert>;
  }

  const addFiles = (incoming: File[]) => {
    setFiles((current) => (op.multiple ? [...current, ...incoming] : incoming.slice(0, 1)));
  };
  const removeFile = (index: number) => setFiles((c) => c.filter((_, i) => i !== index));
  const moveFile = (index: number, direction: -1 | 1) => {
    setFiles((c) => {
      const next = [...c];
      const target = index + direction;
      if (target < 0 || target >= next.length) return c;
      [next[index], next[target]] = [next[target], next[index]];
      return next;
    });
  };
  const setParam = (name: string, value: string) => setParams((p) => ({ ...p, [name]: value }));

  const specRequired = visibleControls.some((c) => c.name === 'spec');
  const specMissing = specRequired && !params.spec?.trim();
  const canSubmit = files.length >= op.minFiles && !specMissing && !mutation.isPending;

  const submit = () => {
    mutation.mutate(
      { files, params },
      { onSuccess: (job) => navigate(`/jobs/${job.id}`) },
    );
  };

  return (
    <Box>
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/')} sx={{ mb: 2 }}>
        All tools
      </Button>
      <Typography variant="h4" gutterBottom>
        {op.title}
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        {op.description}
      </Typography>

      <Card>
        <CardContent>
          <FileDropzone accept={op.accept} multiple={op.multiple} onFiles={addFiles} />
          <FileList files={files} reorder={op.reorder} onRemove={removeFile} onMove={moveFile} />

          {visibleControls.length > 0 && (
            <Stack spacing={2} sx={{ mt: 2 }}>
              {visibleControls.map((control) => (
                <ControlField
                  key={control.name}
                  control={control}
                  value={params[control.name] ?? control.default}
                  onChange={(value) => setParam(control.name, value)}
                />
              ))}
            </Stack>
          )}

          {mutation.isError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {toMessage(mutation.error)}
            </Alert>
          )}

          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
            <Button variant="contained" size="large" disabled={!canSubmit} onClick={submit}>
              {mutation.isPending ? 'Submitting…' : `Run ${op.title}`}
            </Button>
          </Box>
          {op.minFiles > 1 && files.length < op.minFiles && (
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1, textAlign: 'right' }}>
              Add at least {op.minFiles} files.
            </Typography>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}

function ControlField({
  control,
  value,
  onChange,
}: {
  control: ControlDef;
  value: string;
  onChange: (value: string) => void;
}) {
  if (control.type === 'select') {
    return (
      <TextField
        select
        label={control.label}
        value={value}
        helperText={control.helperText}
        onChange={(e) => onChange(e.target.value)}
        fullWidth
      >
        {control.options?.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </TextField>
    );
  }
  return (
    <TextField
      type={control.type === 'number' ? 'number' : 'text'}
      label={control.label}
      value={value}
      helperText={control.helperText}
      onChange={(e) => onChange(e.target.value)}
      fullWidth
    />
  );
}
