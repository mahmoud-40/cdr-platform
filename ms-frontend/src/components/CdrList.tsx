import { useEffect, useState } from 'react';
import { DataGrid } from '@mui/x-data-grid';
import type { GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { Box, Typography, Alert, CircularProgress } from '@mui/material';
import type { Cdr, ServiceType } from '../types/cdr';
import { cdrService } from '../services/api';

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

const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 90 },
    { field: 'source', headerName: 'Source', width: 150 },
    { field: 'destination', headerName: 'Destination', width: 150 },
    {
        field: 'startTime',
        headerName: 'Start Time',
        width: 200,
        renderCell: (params: GridRenderCellParams) => (
            <Typography>{formatStartTime(params.row.startTime)}</Typography>
        ),
    },
    { field: 'service', headerName: 'Service', width: 120 },
    {
        field: 'cdr_usage',
        headerName: 'Usage',
        width: 120,
        renderCell: (params: GridRenderCellParams) => (
            <Typography>
                {formatUsage(params.row.cdr_usage, params.row.service)}
            </Typography>
        ),
    },
];

export const CdrList = () => {
    const [cdrs, setCdrs] = useState<Cdr[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
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

        fetchCdrs();
    }, []);

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
        <Box sx={{ height: '100vh', width: '100%', p: 2 }}>
            <Typography variant="h5" gutterBottom>
                CDR Records
            </Typography>
            <Box sx={{ height: 'calc(100vh - 100px)', width: '100%' }}>
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
                        },
                        '& .MuiDataGrid-columnHeaders': {
                            backgroundColor: '#f5f5f5',
                        },
                    }}
                />
            </Box>
        </Box>
    );
}; 