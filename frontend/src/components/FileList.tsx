import {
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Tooltip,
} from '@mui/material';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import DeleteIcon from '@mui/icons-material/Delete';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

interface Props {
  files: File[];
  reorder: boolean;
  onRemove: (index: number) => void;
  onMove: (index: number, direction: -1 | 1) => void;
}

const formatSize = (bytes: number): string => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

export default function FileList({ files, reorder, onRemove, onMove }: Props) {
  if (files.length === 0) return null;

  return (
    <List dense>
      {files.map((file, index) => (
        <ListItem
          key={`${file.name}-${index}`}
          secondaryAction={
            <>
              {reorder && (
                <>
                  <Tooltip title="Move up">
                    <span>
                      <IconButton
                        edge="end"
                        aria-label={`move-up-${index}`}
                        disabled={index === 0}
                        onClick={() => onMove(index, -1)}
                      >
                        <ArrowUpwardIcon fontSize="small" />
                      </IconButton>
                    </span>
                  </Tooltip>
                  <Tooltip title="Move down">
                    <span>
                      <IconButton
                        edge="end"
                        aria-label={`move-down-${index}`}
                        disabled={index === files.length - 1}
                        onClick={() => onMove(index, 1)}
                      >
                        <ArrowDownwardIcon fontSize="small" />
                      </IconButton>
                    </span>
                  </Tooltip>
                </>
              )}
              <Tooltip title="Remove">
                <IconButton edge="end" aria-label={`remove-${index}`} onClick={() => onRemove(index)}>
                  <DeleteIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </>
          }
        >
          <ListItemIcon>
            <InsertDriveFileIcon color="primary" />
          </ListItemIcon>
          <ListItemText primary={file.name} secondary={formatSize(file.size)} />
        </ListItem>
      ))}
    </List>
  );
}
