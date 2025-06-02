import { useEffect, useState } from 'react';
import { Box, Typography, Paper } from '@mui/material';
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
} from 'recharts';
import type { CdrReport } from '../types/cdr';
import { cdrService } from '../services/api';
import { format, parseISO } from 'date-fns';

export const UsageReport = () => {
    const [reports, setReports] = useState<CdrReport[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchReports = async () => {
            try {
                const data = await cdrService.getUsageReport();
                setReports(data);
                setError(null);
            } catch (err) {
                setError('Failed to fetch usage reports');
                console.error('Error fetching reports:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchReports();
    }, []);

    if (error) {
        return (
            <Box sx={{ p: 3 }}>
                <Typography color="error">{error}</Typography>
            </Box>
        );
    }

    if (loading) {
        return (
            <Box sx={{ p: 3 }}>
                <Typography>Loading reports...</Typography>
            </Box>
        );
    }

    const formatData = (reports: CdrReport[]) => {
        const groupedByDate = reports.reduce((acc, report) => {
            const date = format(parseISO(report.date), 'yyyy-MM-dd');
            if (!acc[date]) {
                acc[date] = { date };
            }
            acc[date][report.service] = report.totalUsage;
            return acc;
        }, {} as Record<string, any>);

        return Object.values(groupedByDate);
    };

    const data = formatData(reports);

    return (
        <Paper sx={{ p: 2 }}>
            <Typography variant="h5" gutterBottom>
                Usage Report
            </Typography>
            <Box sx={{ height: 400 }}>
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis
                            dataKey="date"
                            tickFormatter={(date) => format(parseISO(date), 'MMM dd')}
                        />
                        <YAxis />
                        <Tooltip
                            labelFormatter={(date) => format(parseISO(date), 'PP')}
                            formatter={(value: number, name: string) => [
                                `${value} ${name === 'VOICE' ? 'minutes' : name === 'DATA' ? 'MB' : 'messages'}`,
                                name,
                            ]}
                        />
                        <Legend />
                        <Bar dataKey="VOICE" name="Voice" fill="#8884d8" />
                        <Bar dataKey="DATA" name="Data" fill="#82ca9d" />
                        <Bar dataKey="SMS" name="SMS" fill="#ffc658" />
                    </BarChart>
                </ResponsiveContainer>
            </Box>
        </Paper>
    );
}; 