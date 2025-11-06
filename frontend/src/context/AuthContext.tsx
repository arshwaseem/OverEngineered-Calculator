import { createContext, useContext, useEffect, useState, useCallback, useMemo } from "react";
import type { ReactNode } from "react";
import apiService from "../services/api.ts";
import type { User } from "../types";

// Split context into state and actions for better performance
interface AuthState {
    user: User | null;
    loading: boolean;
    isAuthenticated: boolean;
}

interface AuthActions {
    login: (username: string, password: string) => Promise<{ success: true } | { success: false; error: string }>;
    register: (username: string, password: string) => Promise<{ success: true } | { success: false; error: string }>;
    logout: () => Promise<void>;
}

const AuthStateContext = createContext<AuthState | null>(null);
const AuthActionsContext = createContext<AuthActions | null>(null);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        checkAuthStatus();
    }, []);

    const checkAuthStatus = async (): Promise<void> => {
        try {
            setLoading(false);
        } catch (error) {
            console.error('Auth check failed:', error);
            setUser(null);
            setLoading(false);
        }
    };

    const login = useCallback(async (username: string, password: string) => {
        try {
            const userData = await apiService.login(username, password);
            setUser({
                id: userData.userId,
                username: userData.username,
            });
            return { success: true } as const;
        } catch (err: unknown) {
            let message = "Login Failed";
            if (typeof err === 'object' && err !== null && 'response' in err) {
                const anyErr = err as { response?: { data?: { message?: string } } };
                message = anyErr.response?.data?.message ?? message;
            }
            return { success: false, error: message } as const;
        }
    }, []);

    const register = useCallback(async (username: string, password: string) => {
        try {
            await apiService.register(username, password);
            return { success: true } as const;
        } catch (err: unknown) {
            let message = "Register Failed";
            if (typeof err === 'object' && err !== null && 'response' in err) {
                const anyErr = err as { response?: { data?: { message?: string } } };
                message = anyErr.response?.data?.message ?? message;
            }
            return { success: false, error: message } as const;
        }
    }, []);

    const logout = useCallback(async () => {
        try {
            await apiService.logout();
            setUser(null);
        } catch (error) {
            console.error('Logout Failed: ', error);
            setUser(null);
        }
    }, []);

    // Memoize state object to prevent unnecessary re-renders
    const state = useMemo<AuthState>(() => ({
        user,
        loading,
        isAuthenticated: !!user
    }), [user, loading]);

    // Memoize actions object to prevent re-creating functions
    const actions = useMemo<AuthActions>(() => ({
        login,
        register,
        logout
    }), [login, register, logout]);

    return (
        <AuthStateContext.Provider value={state}>
            <AuthActionsContext.Provider value={actions}>
                {children}
            </AuthActionsContext.Provider>
        </AuthStateContext.Provider>
    );
}

// Custom hooks to consume split contexts
export function useAuthState(): AuthState {
    const context = useContext(AuthStateContext);
    if (context === null) {
        throw new Error("useAuthState must be used within AuthProvider");
    }
    return context;
}

export function useAuthActions(): AuthActions {
    const context = useContext(AuthActionsContext);
    if (context === null) {
        throw new Error("useAuthActions must be used within AuthProvider");
    }
    return context;
}

// Convenience hook that combines both (use sparingly to avoid unnecessary re-renders)
export function useAuth() {
    return {
        ...useAuthState(),
        ...useAuthActions()
    };
}