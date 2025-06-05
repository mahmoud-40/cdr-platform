import React from 'react';
import ReactDOM from 'react-dom/client';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ErrorBoundary } from 'react-error-boundary';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import { BrowserRouter } from 'react-router-dom';
import { getKeycloakInstance } from './config/keycloak';
import './index.css';
import App from './App';

// Create theme instance
const theme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: '#1976d2',
        },
        secondary: {
            main: '#dc004e',
        },
        background: {
            default: '#f5f5f5',
            paper: '#ffffff',
        },
    },
    typography: {
        fontFamily: [
            '-apple-system',
            'BlinkMacSystemFont',
            '"Segoe UI"',
            'Roboto',
            '"Helvetica Neue"',
            'Arial',
            'sans-serif',
        ].join(','),
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    textTransform: 'none',
                },
            },
        },
    },
});

// Error fallback component
const ErrorFallback = ({ error, resetErrorBoundary }: { error: Error; resetErrorBoundary: () => void }) => (
    <div style={{ padding: '20px', textAlign: 'center' }}>
        <h1>Something went wrong</h1>
        <pre style={{ color: 'red' }}>{error.message}</pre>
        <button onClick={resetErrorBoundary}>Try again</button>
    </div>
);

// Root element
const rootElement = document.getElementById('root');
if (!rootElement) {
    throw new Error('Failed to find the root element');
}

// Create root
const root = ReactDOM.createRoot(rootElement);

// Keycloak init options
const keycloakInitOptions = {
    onLoad: 'login-required' as const,
    checkLoginIframe: false,
    silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    pkceMethod: 'S256' as const,
    enableLogging: true
};

// Get the singleton Keycloak instance
const keycloak = getKeycloakInstance();

// Render app
root.render(
    <React.StrictMode>
        <ErrorBoundary FallbackComponent={ErrorFallback}>
            <ReactKeycloakProvider 
                authClient={keycloak}
                initOptions={keycloakInitOptions}
                onEvent={(eventType) => {
                    console.log('Keycloak event:', eventType);
                }}
                LoadingComponent={
                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                        <div>Loading...</div>
                    </div>
                }
            >
                <BrowserRouter>
                    <ThemeProvider theme={theme}>
                        <LocalizationProvider dateAdapter={AdapterDateFns}>
                            <CssBaseline />
                            <App />
                        </LocalizationProvider>
                    </ThemeProvider>
                </BrowserRouter>
            </ReactKeycloakProvider>
        </ErrorBoundary>
    </React.StrictMode>
);
