import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import { CircularProgress, Box } from '@mui/material';

interface ProtectedRouteProps {
    children: ReactNode;
}

const ProtectedRoute = ({ children }: ProtectedRouteProps) => {
    const { keycloak, initialized } = useKeycloak();

    if (!initialized) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    if (!keycloak?.authenticated) {
        return <Navigate to="/login" replace />;
    }

    // Check if token is about to expire
    if (keycloak.isTokenExpired(30)) {
        keycloak.updateToken(70).catch(() => {
            keycloak.login();
        });
    }

    return <>{children}</>;
};

export default ProtectedRoute; 