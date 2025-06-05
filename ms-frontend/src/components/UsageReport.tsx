import { useEffect, useState, useCallback } from 'react';
import {
    Box,
    Typography,
    Paper,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    CircularProgress,
    Alert,
    Stack,
} from '@mui/material';
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
import type { CdrReport, ServiceType } from '../types/cdr';
import { cdrService } from '../services/api';
import { format, parseISO } from 'date-fns';

type TimeRange = '7d' | '30d' | '90d' | 'custom';

interface ChartData {
    date: string;
    VOICE?: number;
    DATA?: number;
    SMS?: number;
}

const SERVICE_COLORS: Record<ServiceType, string> = {
    VOICE: '#8884d8',
    DATA: '#82ca9d',
    SMS: '#ffc658',
};

const TIME_RANGE_OPTIONS = [
    { value: '7d', label: 'Last 7 Days' },
    { value: '30d', label: 'Last 30 Days' },
    { value: '90d', label: 'Last 90 Days' },
    { value: 'custom', label: 'Custom Range' },
] as const;

export const UsageReport = () => {
    const [reports, setReports] = useState<CdrReport[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [timeRange, setTimeRange] = useState<TimeRange>('7d');

    const fetchReports = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await cdrService.getUsageReport();
            setReports(data);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to fetch usage reports';
            setError(errorMessage);
            console.error('Error fetching reports:', err);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchReports();
    }, [fetchReports]);

    const formatData = useCallback((reports: CdrReport[]): ChartData[] => {
        const groupedByDate = reports.reduce((acc, report) => {
            const date = format(parseISO(report.date), 'yyyy-MM-dd');
            if (!acc[date]) {
                acc[date] = { date };
            }
            acc[date][report.service] = report.totalUsage;
            return acc;
        }, {} as Record<string, ChartData>);

        return Object.values(groupedByDate).sort((a, b) => 
            parseISO(a.date).getTime() - parseISO(b.date).getTime()
        );
    }, []);

    const data = formatData(reports);

    const formatTooltipValue = (value: number, name: string) => {
        const unit = name === 'VOICE' ? 'minutes' : name === 'DATA' ? 'MB' : 'messages';
        return [`${value} ${unit}`, name];
    };

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
                <Alert severity="error" onClose={() => setError(null)}>
                    {error}
                </Alert>
            </Box>
        );
    }

    return (
        <Box sx={{ p: 2 }}>
            <Stack spacing={3}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="h5" component="h1">Usage Report</Typography>
                    <FormControl sx={{ minWidth: 120 }}>
                        <InputLabel id="time-range-label">Time Range</InputLabel>
                        <Select
                            labelId="time-range-label"
                            id="time-range-select"
                            value={timeRange}
                            label="Time Range"
                            onChange={(e) => setTimeRange(e.target.value as TimeRange)}
                        >
                            {TIME_RANGE_OPTIONS.map(({ value, label }) => (
                                <MenuItem key={value} value={value}>
                                    {label}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Box>

                <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                        Daily Usage by Service
                    </Typography>
                    <Box sx={{ height: 400 }}>
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={data} aria-label="Daily usage by service bar chart">
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis
                                    dataKey="date"
                                    tickFormatter={(date) => format(parseISO(date), 'MMM dd')}
                                />
                                <YAxis />
                                <Tooltip
                                    labelFormatter={(date) => format(parseISO(date), 'PP')}
                                    formatter={formatTooltipValue}
                                />
                                <Legend aria-label="Service legend" />
                                {Object.entries(SERVICE_COLORS).map(([service, color]) => (
                                    <Bar
                                        key={service}
                                        dataKey={service}
                                        name={service}
                                        fill={color}
                                    />
                                ))}
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
                            <LineChart data={data} aria-label="Usage trends line chart">
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis
                                    dataKey="date"
                                    tickFormatter={(date) => format(parseISO(date), 'MMM dd')}
                                />
                                <YAxis />
                                <Tooltip
                                    labelFormatter={(date) => format(parseISO(date), 'PP')}
                                    formatter={formatTooltipValue}
                                />
                                <Legend aria-label="Service legend" />
                                {Object.entries(SERVICE_COLORS).map(([service, color]) => (
                                    <Line
                                        key={service}
                                        type="monotone"
                                        dataKey={service}
                                        name={service}
                                        stroke={color}
                                        strokeWidth={2}
                                    />
                                ))}
                            </LineChart>
                        </ResponsiveContainer>
                    </Box>
                </Paper>
            </Stack>
        </Box>
    );
}; 