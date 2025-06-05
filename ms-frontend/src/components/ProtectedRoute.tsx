import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import { CircularProgress, Box, Alert } from '@mui/material';
import { useState, useEffect } from 'react';

interface ProtectedRouteProps {
    children: ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
    const { keycloak, initialized } = useKeycloak();
    const [error, setError] = useState<string | null>(null);
    const [checking, setChecking] = useState(false);

    useEffect(() => {
        if (initialized && keycloak?.authenticated && keycloak.isTokenExpired(30)) {
            setChecking(true);
            keycloak.updateToken(70).catch(() => {
                setError('Session expired. Please log in again.');
                keycloak.login();
            }).finally(() => setChecking(false));
        }
    }, [initialized, keycloak]);

    if (!initialized || checking) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return (
            <Box sx={{ p: 3 }}>
                <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
            </Box>
        );
    }

    if (!keycloak?.authenticated) {
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
};

export default ProtectedRoute; 