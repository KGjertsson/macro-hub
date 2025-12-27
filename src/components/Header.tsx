import {BarChart3, LineChart, Landmark, BookOpen, Menu, MapPin, Building2} from 'lucide-react';
import {Button} from '@/components/ui/button';
import {useState} from 'react';
import {Link, useLocation} from 'react-router-dom';

const Header = () => {
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const location = useLocation();
    const isParliamentPage = location.pathname.startsWith('/parliament');
    const isRegionsPage = location.pathname === '/regions';
    const isMunicipalitiesPage = location.pathname === '/municipalities';

    return (
        <header className='fixed top-0 left-0 right-0 z-50 border-b border-border/50 bg-background/80 backdrop-blur-xl'>
            <div className='container mx-auto px-4'>
                <div className='flex h-16 items-center justify-between'>
                    <Link to='/' className='flex items-center gap-3'>
                        <div className='flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-primary'>
                            <BarChart3 className='h-5 w-5 text-primary-foreground'/>
                        </div>
                        <span className='text-xl font-semibold tracking-tight'>Macro Hub</span>
                    </Link>

                    <nav className='hidden md:flex items-center gap-8'>
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
                            to='/regions'
                            className={`text-sm transition-colors flex items-center gap-1.5 ${
                                isRegionsPage ? 'text-primary font-medium' : 'text-muted-foreground hover:text-foreground'
                            }`}
                        >
                            <MapPin className='h-4 w-4'/>
                            Regioner
                        </Link>
                        <Link
                            to='/municipalities'
                            className={`text-sm transition-colors flex items-center gap-1.5 ${
                                isMunicipalitiesPage ? 'text-primary font-medium' : 'text-muted-foreground hover:text-foreground'
                            }`}
                        >
                            <Building2 className='h-4 w-4'/>
                            Kommuner
                        </Link>
                    </nav>

                    <div className='hidden md:flex items-center gap-4'>
                        <Link to='/sources'>
                            <Button variant='ghost' size='sm' className='text-muted-foreground hover:text-foreground'>
                                <BookOpen className='h-4 w-4 mr-2'/>
                                Källor
                            </Button>
                        </Link>
                    </div>

                    <Button
                        variant='ghost'
                        size='icon'
                        className='md:hidden'
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                    >
                        <Menu className='h-5 w-5'/>
                    </Button>
                </div>

                {mobileMenuOpen && (
                    <div className='md:hidden py-4 border-t border-border/50 animate-slide-up'>
                        <nav className='flex flex-col gap-4'>
                            <a href='/#indicators'
                               className='text-sm text-muted-foreground hover:text-foreground transition-colors'>
                                Indicators
                            </a>
                            <a href='/#charts'
                               className='text-sm text-muted-foreground hover:text-foreground transition-colors'>
                                Charts
                            </a>
                            <a href='/#insights'
                               className='text-sm text-muted-foreground hover:text-foreground transition-colors'>
                                Insights
                            </a>
                            <Link
                                to='/parliament'
                                className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                            >
                                <Landmark className='h-4 w-4'/>
                                Riksdagen
                            </Link>
                            <Link
                                to='/regions'
                                className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                            >
                                <MapPin className='h-4 w-4'/>
                                Regioner
                            </Link>
                            <Link
                                to='/municipalities'
                                className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                            >
                                <Building2 className='h-4 w-4'/>
                                Kommuner
                            </Link>
                            <Link
                                to='/sources'
                                className='text-sm transition-colors flex items-center gap-1.5 text-muted-foreground hover:text-foreground'
                            >
                                <BookOpen className='h-4 w-4'/>
                                Källor
                            </Link>
                        </nav>
                    </div>
                )}
            </div>
        </header>
    );
};

export default Header;
