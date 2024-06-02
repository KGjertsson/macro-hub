'use client';

import ClientLayout from '@/app/ClientLayout';
import DynamicChartSettings from '@/components/dynamicchart/DynamicChartSettings';

const ChartPage = () => {
  return (
    <ClientLayout>
      <main className="flex min-h-screen flex-col items-center justify-between p-24">
        <DynamicChartSettings />
      </main>
    </ClientLayout>
  );
};

export default ChartPage;
