import {BarChart3, BookOpen, Landmark, LineChart, LogIn, LogOut, MapPinned, Menu, UserRound} from 'lucide-react';
import {Button} from '@/components/ui/button';
import {useState} from 'react';
import {Link, useLocation} from 'react-router-dom';
import {useAuth} from '@/auth/authContext';

const Header = () => {
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const location = useLocation();
    const {user, logout} = useAuth();
    const isParliamentPage = location.pathname.startsWith('/parliament');
    const isMapPage = location.pathname === '/karta';

    return (
        <header className='fixed top-0 left-0 right-0 z-50 border-b border-border/50 bg-background/80 backdrop-blur-xl'>
            <div className='container mx-auto px-4'>
                <div className='flex h-16 items-center justify-between'>
                    <Link to='/' className='flex items-center gap-3'>
                        <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-primary'>
                            <BarChart3 className='h-5 w-5 text-primary-foreground'/>
                        </div>
                        <span className='text-xl font-semibold tracking-tight whitespace-nowrap'>Macro Hub</span>
                    </Link>

                    <nav className='hidden lg:flex items-center gap-4 xl:gap-8'>
                        <Link
                            to='/macroindicator'
                            className={`text-sm transition-colors flex items-center gap-1.5 ${
                                location.pathname === '/macroindicator' ? 'text-primary font-medium' : 'text-muted-foreground hover:text-foreground'
                            }`}
                        >
                            <LineChart className='h-4 w-4'/>
                            Makroindikatorer
                        </Link>
                        <Link
                            to='/parliament'
                            className={`text-sm transition-colors flex items-center gap-1.5 ${
                                isParliamentPage ? 'text-primary font-medium' : 'text-muted-foreground hover:text-foreground'
                            }`}
                        >
                            <Landmark className='h-4 w-4'/>
                            Riksdagen
                        </Link>
                        <Link
                            to='/karta'
                            className={`text-sm transition-colors flex items-center gap-1.5 ${
                                isMapPage ? 'text-primary font-medium' : 'text-muted-foreground hover:text-foreground'
                            }`}
                        >
                            <MapPinned className='h-4 w-4'/>
                            Karta
                        </Link>
                    </nav>

                    <div className='hidden lg:flex items-center gap-1 xl:gap-4 shrink-0'>
                        <Link to='/sources' aria-label='Källor'>
                            <Button variant='ghost' size='sm' className='text-muted-foreground hover:text-foreground'>
                                <BookOpen className='h-4 w-4 xl:mr-2'/>
                                <span className='hidden xl:inline'>Källor</span>
                            </Button>
                        </Link>
                        {user ? (
                            <>
                                <span className='hidden 2xl:flex text-sm text-muted-foreground items-center gap-1.5'>
                                    <UserRound className='h-4 w-4 shrink-0'/>
                                    <span className='max-w-[10rem] truncate'>{user.displayName}</span>
                                </span>
                                <Button
                                    variant='ghost'
                                    size='sm'
                                    onClick={() => void logout()}
                                    title={`Logga ut ${user.displayName}`}
                                    aria-label={`Logga ut ${user.displayName}`}
                                >
                                    <LogOut className='h-4 w-4 xl:mr-2'/>
                                    <span className='hidden xl:inline'>Logga ut</span>
                                </Button>
                            </>
                        ) : (
                            <Link to='/konto/logga-in' aria-label='Logga in'>
                                <Button variant='ghost' size='sm' className='text-muted-foreground hover:text-foreground'>
                                    <LogIn className='h-4 w-4 xl:mr-2'/>
                                    <span className='hidden xl:inline'>Logga in</span>
                                </Button>
                            </Link>
                        )}
                    </div>

                    <Button
                        variant='ghost'
                        size='icon'
                        className='lg:hidden'
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                        aria-label={mobileMenuOpen ? 'Stäng meny' : 'Öppna meny'}
                        aria-expanded={mobileMenuOpen}
                    >
                        <Menu className='h-5 w-5'/>
                    </Button>
                </div>

                {mobileMenuOpen && (
                    <div className='lg:hidden py-4 border-t border-border/50 animate-slide-up'>
                        <nav className='flex flex-col gap-4'>
                            <Link
                                to='/macroindicator'
                                className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                            >
                                <LineChart className='h-4 w-4'/>
                                Makroindikatorer
                            </Link>
                            <Link
                                to='/parliament'
                                className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                            >
                                <Landmark className='h-4 w-4'/>
                                Riksdagen
                            </Link>
                            <Link
                                to='/karta'
                                className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                            >
                                <MapPinned className='h-4 w-4'/>
                                Karta
                            </Link>
                            <Link
                                to='/sources'
                                className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                            >
                                <BookOpen className='h-4 w-4'/>
                                Källor
                            </Link>
                            {user ? (
                                <button
                                    type='button'
                                    onClick={() => void logout()}
                                    className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                                >
                                    <LogOut className='h-4 w-4'/>
                                    Logga ut ({user.displayName})
                                </button>
                            ) : (
                                <Link
                                    to='/konto/logga-in'
                                    className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                                >
                                    <LogIn className='h-4 w-4'/>
                                    Logga in
                                </Link>
                            )}
                        </nav>
                    </div>
                )}
            </div>
        </header>
    );
};

export default Header;
