import React, { useState, useEffect } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    TextField,
    MenuItem,
    Box,
    Alert
} from '@mui/material';
import type { Cdr, ServiceType } from '../types/cdr';

const SERVICE_OPTIONS: ServiceType[] = ['VOICE', 'DATA', 'SMS'];

interface CdrFormDialogProps {
    open: boolean;
    onClose: () => void;
    onSubmit: (cdr: Omit<Cdr, 'id'>) => Promise<void>;
    initialData?: Partial<Cdr>;
    isEdit?: boolean;
}

const defaultForm: Omit<Cdr, 'id'> = {
    source: '',
    destination: '',
    startTime: '',
    service: 'VOICE',
    cdr_usage: 1
};

const CdrFormDialog: React.FC<CdrFormDialogProps> = ({ open, onClose, onSubmit, initialData, isEdit }) => {
    const [form, setForm] = useState<Omit<Cdr, 'id'>>(defaultForm);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (open) {
            setForm({ ...defaultForm, ...initialData });
            setError(null);
        }
    }, [open, initialData]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async () => {
        // Basic validation
        if (!form.source || !form.destination || !form.startTime || !form.service || !form.cdr_usage) {
            setError('All fields are required.');
            return;
        }
        if (isNaN(Number(form.cdr_usage)) || Number(form.cdr_usage) <= 0) {
            setError('Usage must be a positive number.');
            return;
        }
        setError(null);
        setLoading(true);
        try {
            await onSubmit({
                ...form,
                cdr_usage: Number(form.cdr_usage)
            });
            onClose();
        } catch (err: any) {
            setError(err.message || 'Failed to save CDR.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>{isEdit ? 'Edit CDR' : 'Add CDR'}</DialogTitle>
            <DialogContent>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                    {error && <Alert severity="error">{error}</Alert>}
                    <TextField
                        label="Source"
                        name="source"
                        value={form.source}
                        onChange={handleChange}
                        fullWidth
                        required
                    />
                    <TextField
                        label="Destination"
                        name="destination"
                        value={form.destination}
                        onChange={handleChange}
                        fullWidth
                        required
                    />
                    <TextField
                        label="Start Time"
                        name="startTime"
                        type="datetime-local"
                        value={form.startTime ? form.startTime.slice(0, 16) : ''}
                        onChange={handleChange}
                        fullWidth
                        required
                        InputLabelProps={{ shrink: true }}
                    />
                    <TextField
                        select
                        label="Service"
                        name="service"
                        value={form.service}
                        onChange={handleChange}
                        fullWidth
                        required
                    >
                        {SERVICE_OPTIONS.map((option) => (
                            <MenuItem key={option} value={option}>
                                {option}
                            </MenuItem>
                        ))}
                    </TextField>
                    <TextField
                        label="Usage"
                        name="cdr_usage"
                        type="number"
                        value={form.cdr_usage}
                        onChange={handleChange}
                        fullWidth
                        required
                        inputProps={{ min: 1 }}
                    />
                </Box>
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose} disabled={loading}>Cancel</Button>
                <Button onClick={handleSubmit} variant="contained" disabled={loading}>
                    {isEdit ? 'Save' : 'Add'}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default CdrFormDialog; 