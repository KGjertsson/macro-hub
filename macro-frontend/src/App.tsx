import { Toaster } from '@/components/ui/toaster';
import { Toaster as Sonner } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { HelmetProvider } from 'react-helmet-async';
import Index from './pages/Index';
import Parliament from './pages/Parliament';
import MacroIndicator from '@/pages/MacroIndicator';
import PoliticianDetail from './pages/PoliticianDetail';
import Sources from './pages/Sources';
import Contact from './pages/Contact';
import MapPage from './pages/MapPage';
import RedirectToMap from './pages/RedirectToMap';
import NotFound from './pages/NotFound';
import Login from './pages/account/Login';
import Register from './pages/account/Register';
import ResetPassword from './pages/account/ResetPassword';

const queryClient = new QueryClient();

const App = () => (
  <HelmetProvider>
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <Toaster />
        <Sonner />
        <BrowserRouter>
          <Routes>
            <Route path='/' element={<Index />} />
            <Route path='/parliament' element={<Parliament />} />
            <Route path='/macroindicator' element={<MacroIndicator />} />
            <Route path='/karta' element={<MapPage />} />
            <Route path='/omraden' element={<RedirectToMap category='omraden' />} />
            <Route path='/municipalities' element={<RedirectToMap category='kommuner' />} />
            <Route path='/regions' element={<RedirectToMap category='regioner' />} />
            <Route path='/constituencies' element={<RedirectToMap category='valkretsar' />} />
            <Route path='/sources' element={<Sources />} />
            <Route path='/contact' element={<Contact />} />
            <Route path='/parliament/politician/:id' element={<PoliticianDetail />} />
            <Route path='/konto/logga-in' element={<Login />} />
            <Route path='/konto/registrera' element={<Register />} />
            <Route path='/konto/aterstall-losenord' element={<ResetPassword />} />
            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path='*' element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </QueryClientProvider>
  </HelmetProvider>
);

export default App;
