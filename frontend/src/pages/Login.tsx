import { useState } from "react";
import { useAuthActions } from "@/context/AuthContext.tsx";
import { Link, useNavigate } from "react-router-dom";
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from "@/components/ui/card.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Label } from "@/components/ui/label.tsx";
import { motion } from "motion/react";

export default function Login() {
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    // Use only actions to avoid unnecessary re-renders from auth state changes
    const { login } = useAuthActions();
    const navigate = useNavigate();

    const MotionButton = motion.create(Button);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (!username || !password) {
            setError("Please fill in all fields");
            return;
        }

        setError(null);
        setLoading(true);

        try {
            const result = await login(username, password);

            if (result.success) {
                navigate("/calculator");
            } else {
                setError(result.error ?? "Login Failed");
            }
        } catch (error) {
            setError("An error occurred while trying to log in");
            console.error("Login Error: ", error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
            <Card className="w-full max-w-md flex flex-col gap-y-8">
                <CardHeader className="text-center">
                    <CardTitle>Calculator Login</CardTitle>
                    <CardDescription>
                        Enter your credentials to access the calculator
                    </CardDescription>
                </CardHeader>

                <form onSubmit={handleSubmit} className="flex flex-col gap-y-10">
                    <CardContent className="flex flex-col gap-y-8">
                        {error && (
                            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                                {error}
                            </div>
                        )}

                        <div className="flex flex-col gap-y-2">
                            <Label htmlFor="username">Username</Label>
                            <Input
                                id="username"
                                type="text"
                                placeholder="Enter your username"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                disabled={loading}
                                required
                            />
                        </div>

                        <div className="flex flex-col gap-y-2">
                            <Label htmlFor="password">Password</Label>
                            <Input
                                id="password"
                                type="password"
                                placeholder="Enter your password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                disabled={loading}
                                required
                            />
                        </div>
                    </CardContent>

                    <CardFooter className="flex flex-col gap-y-2">
                        <MotionButton
                            type="submit"
                            className="w-full bg-blue-600 text-white hover:bg-blue-900"
                            variant="outline"
                            disabled={loading}
                            whileTap={{ scale: 0.80 }}
                            transition={{ type: "spring", stiffness: 400, damping: 17 }}
                        >
                            {loading ? 'Logging in...' : 'Login'}
                        </MotionButton>

                        <p className="text-sm text-center text-gray-600">
                            Don't have an account?{' '}
                            <Link to="/register" className="text-blue-600 hover:underline">
                                Register here
                            </Link>
                        </p>
                    </CardFooter>
                </form>
            </Card>
        </div>
    );
}