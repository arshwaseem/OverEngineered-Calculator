import axios from 'axios';
import type { AuthResponse, RegisterResponse, OperationResponse } from "../types";
import type { AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';

const api: AxiosInstance = axios.create({
    baseURL: "/api",
    withCredentials: true,
    headers: {
        'Content-Type': "application/json",
    },
});

api.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        return config;
    }, (error: AxiosError) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response: AxiosResponse) => {
        return response;
    }, async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        if(originalRequest.url?.includes('/auth/refresh')){
            window.location.href = "/login";
            return Promise.reject(error);
        }

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            try {
                await api.post("/auth/refresh");
                return api(originalRequest);
            } catch (refreshError) {
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

function normalizeOperationResponse(data: unknown): OperationResponse {
    if (typeof data === 'number') {
        return { result: data };
    }
    if (data && typeof data === 'object' && 'result' in (data as Record<string, unknown>)) {
        return (data as OperationResponse);
    }
    // Fallback: try to coerce common shapes
    const d = data as Record<string, unknown> | null;
    const maybe = d?.['value'] ?? d?.['data'] ?? d?.['res'];
    if (typeof maybe === 'number') {
        return { result: maybe };
    }
    throw new Error('Unexpected operation response shape');
}

const apiService = {
    register: async(username: string, password: string) : Promise<RegisterResponse> => {
        const {data} : {data : RegisterResponse}  = await api.post<RegisterResponse>("/auth/register", {
            username,
            password
        });
        return data;
    },

    login : async(username: string, password: string) : Promise<AuthResponse> => {
        const {data} : {data : AuthResponse} = await api.post<AuthResponse>("/auth/login", {
            username,
            password
        });
        return data;
    },

    logout : async() : Promise<void> => {
        await api.post<void>("/auth/logout", {});
    },

    add: async (numA: number, numB: number): Promise<OperationResponse> => {
        const { data } = await api.post("/op/add", {
            numA,
            numB
        });
        return normalizeOperationResponse(data);
    },

    subtract: async (numA: number, numB: number): Promise<OperationResponse> => {
        const { data } = await api.post("/op/subtract", {
            numA,
            numB
        });
        return normalizeOperationResponse(data);
    },

    multiply: async (numA: number, numB: number): Promise<OperationResponse> => {
        const { data } = await api.post("/op/multiply", {
            numA,
            numB
        });
        return normalizeOperationResponse(data);
    },

    divide: async (numA: number, numB: number): Promise<OperationResponse> => {
        const { data } = await api.post("/op/divide", {
            numA,
            numB
        });
        return normalizeOperationResponse(data);
    }
}

export default apiService;