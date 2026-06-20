import { Routes, Route, Navigate } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import Dashboard from './pages/Dashboard';
import OperationPage from './pages/OperationPage';
import ProcessingPage from './pages/ProcessingPage';

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<Dashboard />} />
        <Route path="op/:opKey" element={<OperationPage />} />
        <Route path="jobs/:jobId" element={<ProcessingPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
