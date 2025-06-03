export interface Cdr {
    id: number;
    source: string;
    destination: string;
    startTime: number[];
    service: 'VOICE' | 'SMS' | 'DATA';
    cdr_usage: number;
}

export interface CdrReport {
    date: string;
    service: string;
    totalUsage: number;
} 