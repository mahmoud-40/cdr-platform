import { useEffect, useState } from 'react';
import { DataGrid } from '@mui/x-data-grid';
import type { GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { Box, Typography, Alert, CircularProgress, Button, Snackbar, IconButton, Tooltip, Dialog, DialogTitle, DialogActions } from '@mui/material';
import type { Cdr, ServiceType } from '../types/cdr';
import { cdrService } from '../services/api';
import CdrFormDialog from './CdrFormDialog';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

const formatStartTime = (startTime: string): string => {
    try {
        const date = new Date(startTime);
        if (isNaN(date.getTime())) {
            return 'Invalid Date';
        }
        return date.toLocaleString('en-US', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
    } catch (error) {
        console.error('Error formatting date:', error);
        return 'Invalid Date';
    }
};

const formatUsage = (value: number, service: ServiceType): string => {
    if (value === undefined || value === null || isNaN(value)) {
        return 'Invalid Value';
    }
    switch (service) {
        case 'VOICE':
            return `${value} minutes`;
        case 'DATA':
            return `${value} MB`;
        case 'SMS':
            return String(value);
        default:
            return String(value);
    }
};

export const CdrList = () => {
    const [cdrs, setCdrs] = useState<Cdr[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [formOpen, setFormOpen] = useState(false);
    const [editCdr, setEditCdr] = useState<Cdr | null>(null);
    const [deleteCdr, setDeleteCdr] = useState<Cdr | null>(null);
    const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({ open: false, message: '', severity: 'success' });

    const fetchCdrs = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await cdrService.getAllCdrs();
            setCdrs(data);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to fetch CDRs';
            setError(errorMessage);
            console.error('Error fetching CDRs:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCdrs();
    }, []);

    const handleAdd = () => {
        setEditCdr(null);
        setFormOpen(true);
    };
    const handleEdit = (cdr: Cdr) => {
        setEditCdr(cdr);
        setFormOpen(true);
    };
    const handleDelete = (cdr: Cdr) => {
        setDeleteCdr(cdr);
    };
    const handleFormClose = () => {
        setFormOpen(false);
        setEditCdr(null);
    };
    const handleFormSubmit = async (cdr: Omit<Cdr, 'id'>) => {
        try {
            if (editCdr) {
                await cdrService.updateCdr(editCdr.id, cdr);
                setSnackbar({ open: true, message: 'CDR updated successfully.', severity: 'success' });
            } else {
                await cdrService.createCdr(cdr);
                setSnackbar({ open: true, message: 'CDR added successfully.', severity: 'success' });
            }
            fetchCdrs();
        } catch (err: any) {
            setSnackbar({ open: true, message: err.message || 'Failed to save CDR.', severity: 'error' });
        }
    };
    const handleDeleteConfirm = async () => {
        if (!deleteCdr) return;
        try {
            await cdrService.deleteCdr(deleteCdr.id);
            setSnackbar({ open: true, message: 'CDR deleted successfully.', severity: 'success' });
            fetchCdrs();
        } catch (err: any) {
            setSnackbar({ open: true, message: err.message || 'Failed to delete CDR.', severity: 'error' });
        } finally {
            setDeleteCdr(null);
        }
    };
    const handleSnackbarClose = () => setSnackbar((s) => ({ ...s, open: false }));

    const columns: GridColDef[] = [
        { field: 'id', headerName: 'ID', minWidth: 120, flex: 0.3 },
        { field: 'source', headerName: 'Source', minWidth: 250, flex: 1.5 },
        { field: 'destination', headerName: 'Destination', minWidth: 250, flex: 1.5 },
        {
            field: 'startTime',
            headerName: 'Start Time',
            minWidth: 250,
            flex: 1.2,
            renderCell: (params: GridRenderCellParams) => (
                <Typography>{formatStartTime(params.row.startTime)}</Typography>
            ),
        },
        { field: 'service', headerName: 'Service', minWidth: 150, flex: 0.8 },
        {
            field: 'cdr_usage',
            headerName: 'Usage',
            minWidth: 150,
            flex: 0.8,
            renderCell: (params: GridRenderCellParams) => (
                <Typography>
                    {formatUsage(params.row.cdr_usage, params.row.service)}
                </Typography>
            ),
        },
        {
            field: 'actions',
            headerName: 'Actions',
            minWidth: 150,
            flex: 0.5,
            sortable: false,
            filterable: false,
            renderCell: (params: GridRenderCellParams) => (
                <Box>
                    <Tooltip title="Edit">
                        <IconButton onClick={() => handleEdit(params.row)} size="small">
                            <EditIcon fontSize="small" />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                        <IconButton onClick={() => handleDelete(params.row)} size="small" color="error">
                            <DeleteIcon fontSize="small" />
                        </IconButton>
                    </Tooltip>
                </Box>
            ),
        },
    ];

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return (
            <Box sx={{ p: 3 }}>
                <Alert severity="error" sx={{ mb: 2 }}>
                    {error}
                </Alert>
            </Box>
        );
    }

    return (
        <Box sx={{ height: '100vh', width: '100%', p: 0 }}>
            <Box sx={{ p: 2 }}>
                <Typography variant="h5" gutterBottom>
                    CDR Records
                </Typography>
                <Button variant="contained" color="primary" sx={{ mb: 2 }} onClick={handleAdd}>
                    Add CDR
                </Button>
            </Box>
            <Box sx={{ height: 'calc(100vh - 160px)', width: '100%' }}>
                <DataGrid
                    rows={cdrs}
                    columns={columns}
                    pageSizeOptions={[10, 25, 50]}
                    initialState={{
                        pagination: { paginationModel: { pageSize: 10 } },
                        sorting: {
                            sortModel: [{ field: 'id', sort: 'desc' }],
                        },
                    }}
                    loading={loading}
                    disableRowSelectionOnClick
                    sx={{
                        '& .MuiDataGrid-cell': {
                            borderColor: 'rgba(224, 224, 224, 1)',
                            padding: '0 16px',
                        },
                        '& .MuiDataGrid-columnHeaders': {
                            backgroundColor: '#f5f5f5',
                        },
                        '& .MuiDataGrid-columnHeader': {
                            padding: '0 16px',
                        }
                    }}
                />
            </Box>
            <CdrFormDialog
                open={formOpen}
                onClose={handleFormClose}
                onSubmit={handleFormSubmit}
                initialData={editCdr || undefined}
                isEdit={!!editCdr}
            />
            <Dialog open={!!deleteCdr} onClose={() => setDeleteCdr(null)}>
                <DialogTitle>Are you sure you want to delete this CDR?</DialogTitle>
                <DialogActions>
                    <Button onClick={() => setDeleteCdr(null)}>Cancel</Button>
                    <Button onClick={handleDeleteConfirm} color="error" variant="contained">Delete</Button>
                </DialogActions>
            </Dialog>
            <Snackbar
                open={snackbar.open}
                autoHideDuration={4000}
                onClose={handleSnackbarClose}
                message={snackbar.message}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
                ContentProps={{
                    style: { backgroundColor: snackbar.severity === 'success' ? '#43a047' : '#d32f2f' },
                }}
            />
        </Box>
    );
}; 