import dynamic from 'next/dynamic';
import ScrapeAllButton from '@/components/ScrapeAllButton';

const ChartSwedenPolicyRate = dynamic(
  () => import('@/components/ChartSwedenPolicyRate'),
  {
    ssr: false,
  },
);

const ChartUsdSekExchangeRate = dynamic(
  () => import('@/components/ChartUsdSekExchangeRate'),
  {
    ssr: false,
  },
);

const ChartGovernmentBillsSweden = dynamic(
  () => import('@/components/ChartGovernmentBillsSweden'),
  {
    ssr: false,
  },
);

const ChartGovernmentBondsSweden = dynamic(
  () => import('@/components/ChartGovernmentBondsSweden'),
  {
    ssr: false,
  },
);

const ChartEuroMarketRates3Month = dynamic(
  () => import('@/components/ChartEuroMarketRates3Month'),
  {
    ssr: false,
  },
);

const ChartEuroMarketRates6Month = dynamic(
  () => import('@/components/ChartEuroMarketRates6Month'),
  {
    ssr: false,
  },
);

const ChartInternationalGovBillRates5Year = dynamic(
  () => import('@/components/ChartInternationalGovBillRates5Year'),
  {
    ssr: false,
  },
);

const ChartInternationalGovBillRates10Year = dynamic(
  () => import('@/components/ChartInternationalGovBillRates10Year'),
  {
    ssr: false,
  },
);

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <ScrapeAllButton />
      <ChartSwedenPolicyRate />
      <ChartUsdSekExchangeRate />
      <ChartGovernmentBillsSweden />
      <ChartGovernmentBondsSweden />
      <ChartEuroMarketRates3Month />
      <ChartEuroMarketRates6Month />
      <ChartInternationalGovBillRates5Year />
      <ChartInternationalGovBillRates10Year />
    </main>
  );
}
