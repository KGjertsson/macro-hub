import Header from '@/components/Header';
import IndicatorCard from '@/components/IndicatorCard';
import ChartSection from '@/components/ChartSection';
import InsightsSection from '@/components/InsightsSection';
import Footer from '@/components/Footer';
import {Skeleton} from '@/components/ui/skeleton';
import {Frequency, START_PAGE_INDICATORS, useStartPageIndicators} from '@/api/indicators';
import {useSeriesConfigs} from '@/api/chartData';

// Latest observation's date: the day for daily series, the month for monthly ones ("29 okt 2026", "sep 2026")
const formatPeriod = (date: Date, frequency: Frequency) =>
    new Intl.DateTimeFormat('sv-SE', {
        day: frequency === 'day' ? 'numeric' : undefined,
        month: 'short',
        year: 'numeric',
        timeZone: 'UTC'
    }).format(date).replace('.', '');

const changeNote = (frequency: Frequency, changedAt: Date | null) => {
    if (frequency === 'month') {
        return 'Förändring mot föregående månad';
    }
    return changedAt
        ? `Förändring sedan senaste ändringen, ${formatPeriod(changedAt, frequency)}`
        : 'Oförändrad det senaste året';
};

const IndicatorCards = () => {
    const {isPending, error, data} = useStartPageIndicators();
    // Only for the units; until or without them the cards fall back to %
    const {data: configs} = useSeriesConfigs();

    if (isPending) {
        return (
            <>
                {START_PAGE_INDICATORS.map(indicator => (
                    <Skeleton key={indicator.name} className='h-[134px] rounded-xl'/>
                ))}
            </>
        );
    }

    return (
        <>
            {START_PAGE_INDICATORS.map((indicator, index) => {
                const config = configs?.find(c => c.name === indicator.name);
                const observation = data?.[indicator.name] ?? null;
                return (
                    <IndicatorCard
                        key={indicator.name}
                        title={indicator.title}
                        value={observation?.value ?? null}
                        previous={observation?.previous ?? null}
                        unit={config?.unit ?? '%'}
                        period={observation ? formatPeriod(observation.date, indicator.frequency) : undefined}
                        changeNote={changeNote(indicator.frequency, observation?.changedAt ?? null)}
                        message={error ? 'Kunde inte hämta data' : 'Ingen data ännu'}
                        delay={`${(index + 3) * 100}ms`}
                    />
                );
            })}
        </>
    );
};

const Index = () => {
    return (
        <div className='min-h-screen bg-background'>
            <Header/>

            {/* Hero Section */}
            <section className='pt-32 pb-20 relative overflow-hidden'>
                {/* Background glow effect */}
                <div
                    className='absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[600px] bg-gradient-glow opacity-50 blur-3xl pointer-events-none'/>

                <div className='container mx-auto px-4 relative'>
                    <div className='text-center max-w-3xl mx-auto mb-16'>

                        <h1 className='text-4xl md:text-6xl font-bold tracking-tight mb-6 opacity-0 animate-fade-in-up'
                            style={{animationDelay: '100ms'}}>
                            Global makroekonomisk
                            <br/>
                            <span className='text-gradient'>översikt</span>
                        </h1>

                        <p className='text-lg text-muted-foreground opacity-0 animate-fade-in-up'
                           style={{animationDelay: '200ms'}}>
                            Följ viktiga ekonomiska indikatorer, visualisera trender och håll dig uppdaterad
                            med makroekonomisk data från hela världen.
                        </p>
                    </div>

                    {/* Indicator Cards */}
                    <div id='indicators' className='grid sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6'>
                        <IndicatorCards/>
                    </div>
                </div>
            </section>

            <ChartSection/>
            <InsightsSection/>
            <Footer/>
        </div>
    );
};

export default Index;
