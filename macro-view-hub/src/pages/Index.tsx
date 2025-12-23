import Header from '@/components/Header';
import IndicatorCard from '@/components/IndicatorCard';
import ChartSection from '@/components/ChartSection';
import InsightsSection from '@/components/InsightsSection';
import Footer from '@/components/Footer';
import { Activity } from 'lucide-react';

const indicators = [
  {
    title: 'US GDP Growth',
    value: '2.8',
    change: 0.3,
    unit: '%',
    period: 'Q3 2024'
  },
  {
    title: 'Inflation Rate (CPI)',
    value: '2.7',
    change: -0.3,
    unit: '%',
    period: 'Nov 2024'
  },
  {
    title: 'Unemployment Rate',
    value: '4.2',
    change: 0.1,
    unit: '%',
    period: 'Nov 2024'
  },
  {
    title: 'Fed Funds Rate',
    value: '4.50',
    change: 0,
    unit: '%',
    period: 'Dec 2024'
  }
];

const Index = () => {
  return (
    <div className='min-h-screen bg-background'>
      <Header />

      {/* Hero Section */}
      <section className='pt-32 pb-20 relative overflow-hidden'>
        {/* Background glow effect */}
        <div
          className='absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[600px] bg-gradient-glow opacity-50 blur-3xl pointer-events-none' />

        <div className='container mx-auto px-4 relative'>
          <div className='text-center max-w-3xl mx-auto mb-16'>
            <div
              className='inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 border border-primary/20 mb-6 opacity-0 animate-fade-in-up'>
              <Activity className='h-4 w-4 text-primary indicator-pulse' />
              <span className='text-sm font-medium text-primary'>Live Economic Data</span>
            </div>

            <h1 className='text-4xl md:text-6xl font-bold tracking-tight mb-6 opacity-0 animate-fade-in-up'
                style={{ animationDelay: '100ms' }}>
              Global Macro
              <br />
              <span className='text-gradient'>Economic Dashboard</span>
            </h1>

            <p className='text-lg text-muted-foreground opacity-0 animate-fade-in-up'
               style={{ animationDelay: '200ms' }}>
              Track key economic indicators, visualize trends, and stay informed
              with real-time macroeconomic data from around the world.
            </p>
          </div>

          {/* Indicator Cards */}
          <div id='indicators' className='grid sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6'>
            {indicators.map((indicator, index) => (
              <IndicatorCard
                key={indicator.title}
                {...indicator}
                delay={`${(index + 3) * 100}ms`}
              />
            ))}
          </div>
        </div>
      </section>

      <ChartSection />
      <InsightsSection />
      <Footer />
    </div>
  );
};

export default Index;
