'use client';
import { QueryClient, QueryClientProvider } from 'react-query';
import DynamicChartSettings from '@/components/DynamicChartSettings';

const queryClient = new QueryClient();

const Home = () => {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <QueryClientProvider client={queryClient}>
        <DynamicChartSettings />
      </QueryClientProvider>
    </main>
  );
};

export default Home;
