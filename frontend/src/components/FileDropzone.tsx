import { useDropzone } from 'react-dropzone';
import { Box, Typography } from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';

interface Props {
  accept: Record<string, string[]>;
  multiple: boolean;
  onFiles: (files: File[]) => void;
}

export default function FileDropzone({ accept, multiple, onFiles }: Props) {
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept,
    multiple,
    onDrop: (acceptedFiles) => {
      if (acceptedFiles.length > 0) {
        onFiles(acceptedFiles);
      }
    },
  });

  const extensions = Object.values(accept).flat().join(', ');

  return (
    <Box
      {...getRootProps()}
      role="button"
      aria-label="File upload dropzone"
      sx={{
        border: '2px dashed',
        borderColor: isDragActive ? 'primary.main' : '#cbd5e1',
        borderRadius: 2,
        p: 5,
        textAlign: 'center',
        cursor: 'pointer',
        bgcolor: isDragActive ? 'action.hover' : 'transparent',
        transition: 'border-color 0.2s, background-color 0.2s',
      }}
    >
      <input {...getInputProps()} />
      <CloudUploadIcon sx={{ fontSize: 48, color: 'primary.main', mb: 1 }} />
      <Typography variant="subtitle1">
        {isDragActive ? 'Drop files here…' : 'Drag & drop files here, or click to browse'}
      </Typography>
      <Typography variant="body2" color="text.secondary">
        Accepted: {extensions}
        {multiple ? ' · multiple files allowed' : ''}
      </Typography>
    </Box>
  );
}
