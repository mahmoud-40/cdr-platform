import React from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { Box, Button, Typography, Paper } from '@mui/material';

const Login: React.FC = () => {
    const { keycloak } = useKeycloak();

    const handleLogin = () => {
        keycloak?.login();
    };

    return (
        <Box
            sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh',
                bgcolor: 'background.default',
            }}
        >
            <Paper
                elevation={3}
                sx={{
                    p: 4,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    maxWidth: 400,
                    width: '100%',
                }}
            >
                <Typography variant="h4" component="h1" gutterBottom>
                    Welcome to CDR Platform
                </Typography>
                <Typography variant="body1" color="text.secondary" align="center" sx={{ mb: 3 }}>
                    Please sign in to access the platform
                </Typography>
                <Button
                    variant="contained"
                    color="primary"
                    size="large"
                    onClick={handleLogin}
                    fullWidth
                >
                    Sign In
                </Button>
            </Paper>
        </Box>
    );
};

export default Login; 