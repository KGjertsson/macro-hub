import DynamicChartSettings from '@/components/dynamicchart/DynamicChartSettings';
import Header from '@/components/Header';
import Footer from '@/components/Footer';

const ChartPage = () => {
  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />
      <main className='flex-1 container mx-auto px-4 pt-24 pb-12'>
        <DynamicChartSettings />
      </main>
      <Footer />
    </div>
  );
};

export default ChartPage;
