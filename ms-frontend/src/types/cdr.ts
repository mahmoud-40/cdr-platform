export interface Cdr {
    id: number;
    source: string;
    destination: string;
    startTime: string;
    service: 'VOICE' | 'SMS' | 'DATA';
    cdrUsage: number;
}

export interface CdrReport {
    date: string;
    service: string;
    totalUsage: number;
} 