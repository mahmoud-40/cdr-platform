import { useEffect, useState } from 'react';
import { DataGrid } from '@mui/x-data-grid';
import type { GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { Box, Typography } from '@mui/material';
import type { Cdr } from '../types/cdr';
import { cdrService } from '../services/api';

const columns: GridColDef[] = [
    { field: 'id', headerName: 'ID', width: 90 },
    { field: 'source', headerName: 'Source', width: 150 },
    { field: 'destination', headerName: 'Destination', width: 150 },
    {
        field: 'startTime',
        headerName: 'Start Time',
        width: 200,
        renderCell: (params: GridRenderCellParams) => {
            const startTime = params.row.startTime;
            if (!Array.isArray(startTime)) return <Typography>N/A</Typography>;
            try {
                const [year, month, day, hour, minute] = startTime;
                const formattedDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
                return <Typography>{formattedDate}</Typography>;
            } catch (error) {
                console.error('Error formatting date:', error);
                return <Typography>Invalid Date</Typography>;
            }
        },
    },
    { field: 'service', headerName: 'Service', width: 120 },
    {
        field: 'cdr_usage',
        headerName: 'Usage',
        width: 120,
        renderCell: (params: GridRenderCellParams) => {
            const value = params.row.cdr_usage;
            if (value === undefined || value === null) return <Typography>N/A</Typography>;
            const cdr = params.row as Cdr;
            const numValue = Number(value);
            if (isNaN(numValue)) {
                console.error('Invalid usage value:', value);
                return <Typography>Invalid Value</Typography>;
            }
            let displayValue = '';
            switch (cdr.service) {
                case 'VOICE':
                    displayValue = `${numValue} minutes`;
                    break;
                case 'DATA':
                    displayValue = `${numValue} MB`;
                    break;
                case 'SMS':
                    displayValue = '1';
                    break;
                default:
                    displayValue = String(numValue);
            }
            return <Typography>{displayValue}</Typography>;
        },
    },
];

export const CdrList = () => {
    const [cdrs, setCdrs] = useState<Cdr[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchCdrs = async () => {
            try {
                const data = await cdrService.getAllCdrs();
                console.log('Fetched CDRs:', JSON.stringify(data, null, 2));
                setCdrs(data);
                setError(null);
            } catch (err) {
                setError('Failed to fetch CDRs');
                console.error('Error fetching CDRs:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchCdrs();
    }, []);

    if (error) {
        return (
            <Box sx={{ p: 3 }}>
                <Typography color="error">{error}</Typography>
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