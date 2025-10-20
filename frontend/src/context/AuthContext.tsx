import {createContext, useContext, useEffect, useState, useCallback} from "react";
import type {ReactNode} from "react";
import apiService from "../services/api.ts";
import type {User, AuthContextType} from "../types";

const AuthContext = createContext<AuthContextType|null>(null);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {

    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        checkAuthStatus();
    },[]);

    const checkAuthStatus = async (): Promise<void> => {
        try{
            setLoading(false);
        } catch (error) {
            console.error('Auth check failed: ' ,error);
            setUser(null);
            setLoading(false);
        }
    };

    const login = useCallback(async (username : string, password: string) => {
        try{
            const userData = await apiService.login(username, password);
            setUser({
                id : userData.userId,
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

    const register = useCallback(async (username : string, password: string) => {
        try{
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
        try{
            await apiService.logout();
            setUser(null);
        } catch (error) {
            console.error('Logout Failed: ', error);
            setUser(null);
        }
    }, []);

    const value : AuthContextType = {
        user,
        login,
        register,
        logout,
        loading,
        isAuthenticated : !!user
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>

}

export function  useAuth() : AuthContextType {
    const context = useContext(AuthContext);

    if(context === null) {
        throw new Error("useAuth must be used within Auth provider");
    }

    return context;
}