import { createTheme } from '@mui/material/styles';

/**
 * SaaS-style theme: indigo primary, soft surfaces, rounded cards.
 */
export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#4f46e5' },
    secondary: { main: '#0ea5e9' },
    background: { default: '#f4f6fb', paper: '#ffffff' },
    success: { main: '#16a34a' },
    error: { main: '#dc2626' },
    warning: { main: '#d97706' },
  },
  shape: { borderRadius: 12 },
  typography: {
    fontFamily: '"Inter", "Segoe UI", system-ui, sans-serif',
    h4: { fontWeight: 700 },
    h6: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 1px 3px rgba(15, 23, 42, 0.08), 0 1px 2px rgba(15, 23, 42, 0.04)',
        },
      },
    },
  },
});
