import axios from 'axios';
import type { InternalAxiosRequestConfig, AxiosError } from 'axios';
import type { Cdr, CdrReport, ApiError } from '../types/cdr';
import keycloak from '../config/keycloak';

const API_BASE_URL = '/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    },
    timeout: 10000, // 10 seconds
    withCredentials: true,
});

// Request interceptor
api.interceptors.request.use(
    async (config) => {
        try {
            console.log('Keycloak state:', {
                authenticated: keycloak.authenticated,
                token: keycloak.token ? 'present' : 'missing',
                tokenParsed: keycloak.tokenParsed,
                refreshToken: keycloak.refreshToken ? 'present' : 'missing'
            });

            if (!keycloak.authenticated) {
                console.log('Not authenticated, attempting to login...');
                await keycloak.login();
                return Promise.reject('Authentication required');
            }

            if (keycloak.token) {
                // Ensure token is fresh
                const isTokenExpiring = keycloak.isTokenExpired(30);
                if (isTokenExpiring) {
                    console.log('Token expired, refreshing...');
                    const refreshed = await keycloak.updateToken(70);
                    if (!refreshed) {
                        console.log('Token refresh failed, redirecting to login...');
                        await keycloak.login();
                        return Promise.reject('Token refresh failed');
                    }
                }
                console.log('Adding token to request:', {
                    token: keycloak.token.substring(0, 20) + '...',
                    tokenParsed: keycloak.tokenParsed,
                    headers: config.headers
                });
                config.headers.Authorization = `Bearer ${keycloak.token}`;
            } else {
                console.log('No token available, redirecting to login...');
                await keycloak.login();
                return Promise.reject('No token available');
            }
            return config;
        } catch (error) {
            console.error('Error in request interceptor:', error);
            return Promise.reject(error);
        }
    },
    (error) => {
        console.error('Request error:', error);
        return Promise.reject(error);
    }
);

// Response interceptor
api.interceptors.response.use(
    (response) => {
        console.log('Response received:', {
            url: response.config.url,
            status: response.status,
            headers: response.headers
        });
        return response;
    },
    async (error: AxiosError<{ message?: string }>) => {
        console.log('Response error:', {
            status: error.response?.status,
            data: error.response?.data,
            headers: error.response?.headers,
            config: {
                url: error.config?.url,
                headers: error.config?.headers
            }
        });
        
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                console.log('Received 401, attempting to refresh token...');
                const refreshed = await keycloak.updateToken(70);
                if (refreshed) {
                    console.log('Token refreshed successfully, retrying request...');
                    originalRequest.headers.Authorization = `Bearer ${keycloak.token}`;
                    return api(originalRequest);
                } else {
                    console.log('Token refresh failed, redirecting to login...');
                    await keycloak.login();
                }
            } catch (refreshError) {
                console.error('Token refresh failed:', refreshError);
                await keycloak.login();
            }
        }

        if (error.code === 'ECONNABORTED') {
            console.error('Request timeout');
            return Promise.reject({
                message: 'Request timed out. Please try again.',
                status: 408,
            } as ApiError);
        }

        if (!error.response) {
            console.error('Network error:', error.message);
            return Promise.reject({
                message: 'Network error. Please check your connection.',
                status: 0,
            } as ApiError);
        }

        const apiError: ApiError = {
            message: error.response.data?.message || error.message || 'An unknown error occurred',
            status: error.response.status,
            details: error.response.data,
        };
        console.error('API Error:', apiError);
        return Promise.reject(apiError);
    }
);

export const cdrService = {
    getAllCdrs: async (): Promise<Cdr[]> => {
        try {
            console.log('Fetching CDRs...');
            const response = await api.get<Cdr[]>('/cdrs');
            console.log('CDRs fetched successfully:', response.data);
            
            // Check if response is an array
            if (!Array.isArray(response.data)) {
                throw new Error('Invalid response format: Expected an array of CDR records');
            }

            // Validate each CDR record
            const cdrs = response.data.map((cdr, index) => {
                if (!cdr || typeof cdr !== 'object') {
                    throw new Error(`Invalid CDR record at index ${index}: Expected an object`);
                }

                // Ensure required fields are present
                const requiredFields = ['id', 'source', 'destination', 'startTime', 'service', 'cdr_usage'];
                for (const field of requiredFields) {
                    if (!(field in cdr)) {
                        throw new Error(`Invalid CDR record at index ${index}: Missing required field '${field}'`);
                    }
                }

                // Validate startTime is a valid date string
                if (typeof cdr.startTime !== 'string' || isNaN(Date.parse(cdr.startTime))) {
                    throw new Error(`Invalid CDR record at index ${index}: startTime must be a valid ISO date string`);
                }

                return cdr as Cdr;
            });

            return cdrs;
        } catch (error) {
            console.error('Error fetching CDRs:', error);
            if (error instanceof Error) {
                throw new Error(`Failed to fetch CDRs: ${error.message}`);
            }
            throw error;
        }
    },

    getCdrById: async (id: number): Promise<Cdr> => {
        try {
            const response = await api.get<Cdr>(`/cdrs/${id}`);
            return response.data;
        } catch (error) {
            console.error(`Error fetching CDR ${id}:`, error);
            if (error instanceof Error) {
                throw new Error(`Failed to fetch CDR ${id}: ${error.message}`);
            }
            throw error;
        }
    },

    createCdr: async (cdr: Omit<Cdr, 'id'>): Promise<Cdr> => {
        try {
            const response = await api.post<Cdr>('/cdrs', cdr);
            return response.data;
        } catch (error) {
            console.error('Error creating CDR:', error);
            if (error instanceof Error) {
                throw new Error(`Failed to create CDR: ${error.message}`);
            }
            throw error;
        }
    },

    updateCdr: async (id: number, cdr: Partial<Cdr>): Promise<Cdr> => {
        try {
            const response = await api.put<Cdr>(`/cdrs/${id}`, cdr);
            return response.data;
        } catch (error) {
            console.error(`Error updating CDR ${id}:`, error);
            if (error instanceof Error) {
                throw new Error(`Failed to update CDR ${id}: ${error.message}`);
            }
            throw error;
        }
    },

    deleteCdr: async (id: number): Promise<void> => {
        try {
            await api.delete(`/cdrs/${id}`);
        } catch (error) {
            console.error(`Error deleting CDR ${id}:`, error);
            if (error instanceof Error) {
                throw new Error(`Failed to delete CDR ${id}: ${error.message}`);
            }
            throw error;
        }
    },

    getUsageReport: async (): Promise<CdrReport[]> => {
        try {
            const response = await api.get<CdrReport[]>('/cdrs/report');
            return response.data;
        } catch (error) {
            console.error('Error fetching usage report:', error);
            if (error instanceof Error) {
                throw new Error(`Failed to fetch usage report: ${error.message}`);
            }
            throw error;
        }
    },
}; 