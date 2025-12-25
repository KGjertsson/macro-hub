import { Toaster } from '@/components/ui/toaster';
import { Toaster as Sonner } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Index from './pages/Index';
import Parliament from './pages/Parliament';
import MacroIndicator from '@/pages/MacroIndicator';
import PoliticianDetail from './pages/PoliticianDetail';
import Sources from './pages/Sources';
import NotFound from './pages/NotFound';


const queryClient = new QueryClient();

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <Routes>
          <Route path='/' element={<Index />} />
          <Route path='/parliament' element={<Parliament />} />
          <Route path='/macroindicator' element={<MacroIndicator />} />
          <Route path='/sources' element={<Sources />} />
          <Route path='/parliament/politician/:id' element={<PoliticianDetail />} />
          {/* ADD ALL CUSTOM ROUTES ABOVE THE CATCH-ALL "*" ROUTE */}
          <Route path='*' element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
