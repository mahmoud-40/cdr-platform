import { Button } from '@mui/material';
import {
    AppBar,
    Toolbar,
    Typography,
    Box,
    CssBaseline,
    ThemeProvider,
    createTheme,
    Tab,
    Tabs,
} from '@mui/material';
import { useState } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { CdrList } from './components/CdrList';
import { UsageReport } from './components/UsageReport';
import AnalyticsDashboard from './components/AnalyticsDashboard';
import { ErrorBoundary } from 'react-error-boundary';
import Login from './pages/Login';
import ProtectedRoute from './components/ProtectedRoute';

// Create a theme instance
const theme = createTheme({
    palette: {
        primary: {
            main: '#1976d2',
        },
        secondary: {
            main: '#dc004e',
        },
    },
});

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

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function TabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;

    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`simple-tabpanel-${index}`}
            aria-labelledby={`simple-tab-${index}`}
            {...other}
        >
            {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
        </div>
    );
}

function App() {
    const [value, setValue] = useState(0);
    const { keycloak } = useKeycloak();

    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setValue(newValue);
    };

    const handleLogout = () => {
        keycloak?.logout();
    };

    // Extract username from token if available
    const username = keycloak?.tokenParsed?.preferred_username || keycloak?.tokenParsed?.email || '';

    return (
        <Router>
            <ThemeProvider theme={theme}>
                <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
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
                    <Box component="main" sx={{ flexGrow: 1 }}>
                        <ErrorBoundary FallbackComponent={ErrorFallback}>
                            <Routes>
                                <Route path="/login" element={<Login />} />
                                <Route
                                    path="/"
                                    element={
                                        <ProtectedRoute>
                                            <TabPanel value={value} index={0}>
                                                <CdrList />
                                            </TabPanel>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route
                                    path="/usage"
                                    element={
                                        <ProtectedRoute>
                                            <TabPanel value={value} index={1}>
                                                <UsageReport />
                                            </TabPanel>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route
                                    path="/analytics"
                                    element={
                                        <ProtectedRoute>
                                            <TabPanel value={value} index={2}>
                                                <AnalyticsDashboard />
                                            </TabPanel>
                                        </ProtectedRoute>
                                    }
                                />
                                <Route path="*" element={<Navigate to="/" replace />} />
                            </Routes>
                        </ErrorBoundary>
                    </Box>
                </Box>
            </ThemeProvider>
        </Router>
    );
}

export default App;
