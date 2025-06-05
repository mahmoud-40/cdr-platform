import React, { useEffect, useState } from 'react';
import {
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
    BarChart,
    Bar,
    PieChart,
    Pie,
    Cell,
} from 'recharts';
import { Box, Typography, Paper, CircularProgress, Alert } from '@mui/material';
import { cdrService } from '../services/api';
import type { CdrReport, ServiceType } from '../types/cdr';
import { format, parseISO } from 'date-fns';

const SERVICE_COLORS: Record<ServiceType, string> = {
    VOICE: '#8884d8',
    DATA: '#82ca9d',
    SMS: '#ffc658',
};

const AnalyticsDashboard: React.FC = () => {
    const [reports, setReports] = useState<CdrReport[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await cdrService.getUsageReport();
                setReports(data);
            } catch (err) {
                const errorMessage = err instanceof Error ? err.message : 'Failed to fetch analytics data';
                setError(errorMessage);
                console.error('Error fetching analytics:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const formatData = (reports: CdrReport[]) => {
        const groupedByDate = reports.reduce((acc, report) => {
            const date = format(parseISO(report.date), 'yyyy-MM-dd');
            if (!acc[date]) {
                acc[date] = { date, VOICE: 0, DATA: 0, SMS: 0 };
            }
            acc[date][report.service] = report.totalUsage;
            return acc;
        }, {} as Record<string, { date: string } & Record<ServiceType, number>>);

        return Object.values(groupedByDate).sort((a, b) => 
            parseISO(a.date).getTime() - parseISO(b.date).getTime()
        );
    };

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

    const data = formatData(reports);
    const serviceTotals = reports.reduce((acc, report) => {
        if (!acc[report.service]) {
            acc[report.service] = 0;
        }
        acc[report.service] += report.totalUsage;
        return acc;
    }, {} as Record<ServiceType, number>);

    return (
        <Box sx={{ p: 2 }}>
            <Typography variant="h5" gutterBottom>
                Analytics Dashboard
            </Typography>

            <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 2, mb: 3 }}>
                {Object.entries(serviceTotals).map(([service, total]) => (
                    <Paper key={service} sx={{ p: 2 }}>
                        <Typography variant="subtitle2" color="text.secondary">
                            {service} Usage
                        </Typography>
                        <Typography variant="h4">
                            {formatTooltipValue(total, service)[0]}
                        </Typography>
                    </Paper>
                ))}
            </Box>

            <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(500px, 1fr))', gap: 2 }}>
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
                        Service Distribution
                    </Typography>
                    <Box sx={{ height: 400 }}>
                        <ResponsiveContainer width="100%" height="100%">
                            <PieChart aria-label="Service distribution pie chart">
                                <Pie
                                    data={Object.entries(serviceTotals).map(([service, total]) => ({
                                        name: service,
                                        value: total,
                                    }))}
                                    cx="50%"
                                    cy="50%"
                                    labelLine={false}
                                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                                    outerRadius={80}
                                    fill="#8884d8"
                                    dataKey="value"
                                >
                                    {Object.entries(serviceTotals).map(([service]) => (
                                        <Cell key={service} fill={SERVICE_COLORS[service as ServiceType]} />
                                    ))}
                                </Pie>
                                <Tooltip formatter={formatTooltipValue} />
                                <Legend aria-label="Service legend" />
                            </PieChart>
                        </ResponsiveContainer>
                    </Box>
                </Paper>
            </Box>
        </Box>
    );
};

export default AnalyticsDashboard; 