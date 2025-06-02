import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Container, Box, Button } from '@mui/material';
import { CdrList } from './components/CdrList';
import { UsageReport } from './components/UsageReport';

function App() {
  return (
        <Router>
            <Box sx={{ flexGrow: 1 }}>
                <AppBar position="static">
                    <Toolbar>
                        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                            CDR Platform
                        </Typography>
                        <Button color="inherit" component={Link} to="/">
                            CDR List
                        </Button>
                        <Button color="inherit" component={Link} to="/report">
                            Usage Report
                        </Button>
                    </Toolbar>
                </AppBar>

                <Container maxWidth="lg" sx={{ mt: 4 }}>
                    <Routes>
                        <Route path="/" element={<CdrList />} />
                        <Route path="/report" element={<UsageReport />} />
                    </Routes>
                </Container>
            </Box>
        </Router>
    );
}

export default App;
