'use client';
import dynamic from 'next/dynamic';
import { QueryClient, QueryClientProvider } from 'react-query';
import DynamicChartSettings from '@/components/DynamicChartSettings';

const ChartEuroMarketRates3Month = dynamic(
  () => import('@/components/ChartEuroMarketRates3Month'),
  {
    ssr: false,
  }
);

const ChartEuroMarketRates6Month = dynamic(
  () => import('@/components/ChartEuroMarketRates6Month'),
  {
    ssr: false,
  }
);

const ChartInternationalGovBillRates5Year = dynamic(
  () => import('@/components/ChartInternationalGovBillRates5Year'),
  {
    ssr: false,
  }
);

const ChartInternationalGovBillRates10Year = dynamic(
  () => import('@/components/ChartInternationalGovBillRates10Year'),
  {
    ssr: false,
  }
);

const queryClient = new QueryClient();

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <QueryClientProvider client={queryClient}>
        <DynamicChartSettings />
        {/*<ChartEuroMarketRates3Month />*/}
        {/*<ChartEuroMarketRates6Month />*/}
        {/*<ChartInternationalGovBillRates5Year />*/}
        {/*<ChartInternationalGovBillRates10Year />*/}
      </QueryClientProvider>
    </main>
  );
}
