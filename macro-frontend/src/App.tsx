import { Toaster } from '@/components/ui/toaster';
import { Toaster as Sonner } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { HelmetProvider } from 'react-helmet-async';
import Index from './pages/Index';
import Parliament from './pages/Parliament';
import MacroIndicator from '@/pages/MacroIndicator';
import PoliticianDetail from './pages/PoliticianDetail';
import Sources from './pages/Sources';
import Contact from './pages/Contact';
import Regions from './pages/Regions';
import Municipalities from './pages/Municipalities';
import Constituencies from './pages/Constituencies';
import NotFound from './pages/NotFound';

// MapLibre is about 1 MB, so the map page and its dependencies load only when visited
const RegionalStatistics = lazy(() => import('./pages/RegionalStatistics'));

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
            <Route path='/regions' element={<Regions />} />
            <Route path='/municipalities' element={<Municipalities />} />
            <Route path='/constituencies' element={<Constituencies />} />
            <Route path='/sources' element={<Sources />} />
            <Route path='/contact' element={<Contact />} />
            <Route path='/parliament/politician/:id' element={<PoliticianDetail />} />
            <Route
              path='/omraden'
              element={
                <Suspense fallback={<div className='min-h-screen bg-background' />}>
                  <RegionalStatistics />
                </Suspense>
              }
            />
            {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
            <Route path='*' element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </TooltipProvider>
    </QueryClientProvider>
  </HelmetProvider>
);

export default App;
