import axios from 'axios';
import type { Cdr, CdrReport } from '../types/cdr';

const API_BASE_URL = '/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const cdrService = {
    getAllCdrs: async (): Promise<Cdr[]> => {
        const response = await api.get('/cdrs');
        return response.data;
    },

    getCdrById: async (id: number): Promise<Cdr> => {
        const response = await api.get(`/cdrs/${id}`);
        return response.data;
    },

    createCdr: async (cdr: Omit<Cdr, 'id'>): Promise<Cdr> => {
        const response = await api.post('/cdrs', cdr);
        return response.data;
    },

    updateCdr: async (id: number, cdr: Partial<Cdr>): Promise<Cdr> => {
        const response = await api.put(`/cdrs/${id}`, cdr);
        return response.data;
    },

    deleteCdr: async (id: number): Promise<void> => {
        await api.delete(`/cdrs/${id}`);
    },

    getUsageReport: async (): Promise<CdrReport[]> => {
        const response = await api.get('/cdrs/report');
        return response.data;
    },
}; 