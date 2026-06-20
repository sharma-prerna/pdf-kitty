import { AppBar, Box, Button, Container, Toolbar, Typography } from '@mui/material';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom';

export default function AppLayout() {
  const location = useLocation();
  const onHistory = location.pathname.startsWith('/history');

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="sticky" color="inherit" elevation={0} sx={{ borderBottom: '1px solid #e5e7eb' }}>
        <Toolbar>
          <PictureAsPdfIcon color="primary" sx={{ mr: 1 }} />
          <Typography
            variant="h6"
            component={RouterLink}
            to="/"
            sx={{ flexGrow: 1, color: 'text.primary', textDecoration: 'none' }}
          >
            PDF Toolkit
          </Typography>
          <Button component={RouterLink} to="/" color={onHistory ? 'inherit' : 'primary'}>
            Tools
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
}
