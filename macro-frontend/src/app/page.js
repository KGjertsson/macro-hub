import dynamic from 'next/dynamic';

const ChartSwedenPolicyRate = dynamic(
  () => import('@/components/ChartSwedenPolicyRate'),
  {
    ssr: false,
  }
);

const ChartUsdSekExchangeRate = dynamic(
  () => import('@/components/ChartUsdSekExchangeRate'),
  {
    ssr: false,
  }
);

const ChartGovernmentBillsSweden = dynamic(
  () => import('@/components/ChartGovernmentBillsSweden'),
  {
    ssr: false,
  }
);

import ScrapeAllButton from '@/components/ScrapeAllButton';

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <ScrapeAllButton />
      <ChartSwedenPolicyRate />
      <ChartUsdSekExchangeRate />
      <ChartGovernmentBillsSweden />
    </main>
  );
}
