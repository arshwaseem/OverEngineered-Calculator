import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import Calculator from '@/components/Calculator';
import { Button } from '@/components/ui/button';

export default function CalculatorPage() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            await logout();
            navigate('/login');
        } catch (error) {
            console.error('Logout failed:', error);
            navigate('/login');
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex flex-col">
            <header className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">
                            Calculator App
                        </h1>
                        <p className="text-sm text-gray-600">
                            Welcome, {user?.username ?? 'Guest'}!
                        </p>
                    </div>

                    <Button
                        onClick={handleLogout}
                        variant="outline"
                    >
                        Logout
                    </Button>
                </div>
            </header>

            <main className="flex items-center flex-1 py-12 px-4">
                <Calculator />
            </main>

            <footer className="fixed bottom-0 w-full bg-white border-t p-4 text-center text-sm text-gray-600">
                <p>Built with React + Spring Boot Microservices</p>
            </footer>
        </div>
    );
}