import { Box, Card, CardActionArea, CardContent, Stack, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { OPERATIONS } from '../operations';

export default function Dashboard() {
  const navigate = useNavigate();

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        PDF Toolkit
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Upload, convert, and transform your documents. Choose a tool to get started.
      </Typography>

      <Box
        sx={{
          display: 'grid',
          gap: 3,
          gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: '1fr 1fr 1fr' },
        }}
      >
        {OPERATIONS.map((op) => {
          const Icon = op.Icon;
          return (
            <Card key={op.key}>
              <CardActionArea onClick={() => navigate(`/op/${op.key}`)} sx={{ height: '100%' }}>
                <CardContent>
                  <Stack spacing={1.5}>
                    <Box
                      sx={{
                        width: 48,
                        height: 48,
                        borderRadius: 2,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        bgcolor: `${op.color}1a`,
                      }}
                    >
                      <Icon sx={{ color: op.color }} />
                    </Box>
                    <Typography variant="h6">{op.title}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {op.description}
                    </Typography>
                  </Stack>
                </CardContent>
              </CardActionArea>
            </Card>
          );
        })}
      </Box>
    </Box>
  );
}
