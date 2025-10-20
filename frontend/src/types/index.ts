export interface User {
    id: number;
    username: string;
}

export interface LoginRequest {
    username: string;
    password: string;
}

export interface RegisterRequest {
    username: string;
    password: string;
}

export interface AuthResponse {
    accessToken?: string;
    refreshToken?: string;
    tokenType: string;
    expiresIn: number;
    username: string;
    userId: number;
}

export interface RegisterResponse {
    message: string;
    username: string;
    userId: number;
}

export interface OperationRequest {
    numA : number;
    numB : number;
}

export interface OperationResponse {
    result : number;
}

// Discriminated union for auth actions to enforce proper error handling at call sites
export type AuthActionResult = { success: true } | { success: false; error: string };

export interface AuthContextType {
    user: User | null;
    login: (username: string, password: string) => Promise<AuthActionResult>;
    register: (username: string, password: string) => Promise<AuthActionResult>;
    logout: () => Promise<void>;
    loading: boolean;
    isAuthenticated: boolean;
}