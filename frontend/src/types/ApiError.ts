export type ApiError = {
    timestamp: string;
    status: number;
    error: string;
    message: string;
    path: string;
};

export type ValidationApiError = {
    timestamp: string;
    status: number;
    error: string;
    fieldErrors: Record<string, string>;
    path: string;
};