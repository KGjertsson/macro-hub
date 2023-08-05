import dynamic from 'next/dynamic';

const DynamicMyChart = dynamic(() => import('@/components/mychart'), {
  ssr: false,
});

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <DynamicMyChart />
    </main>
  );
}
