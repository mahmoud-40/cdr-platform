import { useEffect, useState } from 'react';
import { Box, Typography, Paper, FormControl, InputLabel, Select, MenuItem } from '@mui/material';
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
    LineChart,
    Line,
} from 'recharts';
import type { CdrReport } from '../types/cdr';
import { cdrService } from '../services/api';
import { format, parseISO, subDays, startOfDay, endOfDay } from 'date-fns';

type TimeRange = '7d' | '30d' | '90d' | 'custom';

export const UsageReport = () => {
    const [reports, setReports] = useState<CdrReport[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [timeRange, setTimeRange] = useState<TimeRange>('7d');

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
    }, [timeRange]);

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

    const getServiceColor = (service: string) => {
        switch (service) {
            case 'VOICE':
                return '#8884d8';
            case 'DATA':
                return '#82ca9d';
            case 'SMS':
                return '#ffc658';
            default:
                return '#000000';
        }
    };

    return (
        <Box sx={{ p: 2 }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="h5">Usage Report</Typography>
                    <FormControl sx={{ minWidth: 120 }}>
                        <InputLabel>Time Range</InputLabel>
                        <Select
                            value={timeRange}
                            label="Time Range"
                            onChange={(e) => setTimeRange(e.target.value as TimeRange)}
                        >
                            <MenuItem value="7d">Last 7 Days</MenuItem>
                            <MenuItem value="30d">Last 30 Days</MenuItem>
                            <MenuItem value="90d">Last 90 Days</MenuItem>
                            <MenuItem value="custom">Custom Range</MenuItem>
                        </Select>
                    </FormControl>
                </Box>

                <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                        Daily Usage by Service
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
                                <Bar dataKey="VOICE" name="Voice" fill={getServiceColor('VOICE')} />
                                <Bar dataKey="DATA" name="Data" fill={getServiceColor('DATA')} />
                                <Bar dataKey="SMS" name="SMS" fill={getServiceColor('SMS')} />
                            </BarChart>
                        </ResponsiveContainer>
                    </Box>
                </Paper>

                <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                        Usage Trends
                    </Typography>
                    <Box sx={{ height: 400 }}>
                        <ResponsiveContainer width="100%" height="100%">
                            <LineChart data={data}>
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
                                <Line
                                    type="monotone"
                                    dataKey="VOICE"
                                    name="Voice"
                                    stroke={getServiceColor('VOICE')}
                                    strokeWidth={2}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="DATA"
                                    name="Data"
                                    stroke={getServiceColor('DATA')}
                                    strokeWidth={2}
                                />
                                <Line
                                    type="monotone"
                                    dataKey="SMS"
                                    name="SMS"
                                    stroke={getServiceColor('SMS')}
                                    strokeWidth={2}
                                />
                            </LineChart>
                        </ResponsiveContainer>
                    </Box>
                </Paper>
            </Box>
        </Box>
    );
}; 