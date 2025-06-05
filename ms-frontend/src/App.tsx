import { Button } from '@mui/material';
import {
    AppBar,
    Toolbar,
    Typography,
    Box,
    CssBaseline,
    Tab,
    Tabs,
} from '@mui/material';
import { useState, useEffect } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { CdrList } from './components/CdrList';
import { UsageReport } from './components/UsageReport';
import AnalyticsDashboard from './components/AnalyticsDashboard';
import { ErrorBoundary } from 'react-error-boundary';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';

// Error fallback component
const ErrorFallback = ({ error, resetErrorBoundary }: { error: Error; resetErrorBoundary: () => void }) => (
    <Box sx={{ p: 3, textAlign: 'center' }}>
        <Typography variant="h5" color="error" gutterBottom>
            Something went wrong
        </Typography>
        <Typography color="text.secondary" paragraph>
            {error.message}
        </Typography>
        <Button variant="contained" onClick={resetErrorBoundary}>
            Try again
        </Button>
    </Box>
);

function App() {
    const [value, setValue] = useState(0);
    const { keycloak } = useKeycloak();
    const navigate = useNavigate();

    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setValue(newValue);
        // Navigate to the corresponding route
        switch (newValue) {
            case 0:
                navigate('/');
                break;
            case 1:
                navigate('/usage');
                break;
            case 2:
                navigate('/analytics');
                break;
        }
    };

    // Update tab value based on current route
    useEffect(() => {
        const path = window.location.pathname;
        if (path === '/') setValue(0);
        else if (path === '/usage') setValue(1);
        else if (path === '/analytics') setValue(2);
    }, []);

    const handleLogout = () => {
        keycloak?.logout();
    };

    // Extract username from token if available
    const username = keycloak?.tokenParsed?.preferred_username || keycloak?.tokenParsed?.email || '';

    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', width: '100%' }}>
            <CssBaseline />
            <AppBar position="static">
                <Toolbar>
                    <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                        CDR Platform
                    </Typography>
                    {keycloak?.authenticated && username && (
                        <Typography variant="body2" sx={{ mr: 2 }}>
                            Signed in as <b>{username}</b>
                        </Typography>
                    )}
                    {keycloak?.authenticated && (
                        <Button color="inherit" onClick={handleLogout}>
                            Logout
                        </Button>
                    )}
                </Toolbar>
                {keycloak?.authenticated && (
                    <Tabs value={value} onChange={handleChange} aria-label="navigation tabs">
                        <Tab label="CDR Records" />
                        <Tab label="Usage Report" />
                        <Tab label="Analytics" />
                    </Tabs>
                )}
            </AppBar>
            <Box component="main" sx={{ flexGrow: 1, width: '100%' }}>
                <ErrorBoundary FallbackComponent={ErrorFallback}>
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route
                            path="/"
                            element={
                                <ProtectedRoute>
                                    <CdrList />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/usage"
                            element={
                                <ProtectedRoute>
                                    <UsageReport />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/analytics"
                            element={
                                <ProtectedRoute>
                                    <AnalyticsDashboard />
                                </ProtectedRoute>
                            }
                        />
                        <Route path="*" element={<Navigate to="/" replace />} />
                    </Routes>
                </ErrorBoundary>
            </Box>
        </Box>
    );
}

export default App;
