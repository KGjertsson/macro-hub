import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import { AuthProvider } from './auth/AuthProvider';
import './index.css';

createRoot(document.getElementById('root')!).render(
  <AuthProvider>
    <App />
  </AuthProvider>,
);

if (import.meta.env.PROD) {
  import('./telemetry/browserTelemetry')
    .then(({ startBrowserTelemetry }) => startBrowserTelemetry())
    .catch(() => undefined);
}
