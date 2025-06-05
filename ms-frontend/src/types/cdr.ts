export type ServiceType = 'VOICE' | 'SMS' | 'DATA';

export interface Cdr {
    id: number;
    source: string;
    destination: string;
    startTime: string;  // LocalDateTime as ISO string (e.g., "2025-06-05T10:30:00")
    service: ServiceType;
    cdr_usage: number;
}

export interface CdrReport {
    date: string;
    service: ServiceType;
    totalUsage: number;
    recordCount: number;
}

export interface ApiError {
    message: string;
    status: number;
    details?: unknown;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
} 