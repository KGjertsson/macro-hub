import { Helmet } from 'react-helmet-async';
import { MapPinned } from 'lucide-react';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import RegionalStatisticsView from '@/components/regionalmap/RegionalStatisticsView';

const RegionalStatistics = () => {
  return (
    <>
      <Helmet>
        <title>Områden - Statistik per DeSO och RegSO | Macro Hub</title>
        <meta name="description" content="Utforska SCB:s statistik per DeSO- och RegSO-område på en interaktiv karta. Sök på en adress och se hur området placerar sig i kommunen och i Sverige." />
      </Helmet>

      <div className="min-h-screen bg-background">
        <Header />

        <main className="container mx-auto px-4 pt-24 pb-16">
          <div className="max-w-7xl mx-auto">
            <div className="text-center mb-8">
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4">
                <MapPinned className="h-4 w-4" />
                Områdesstatistik
              </div>
              <h1 className="text-4xl md:text-5xl font-bold text-foreground mb-4">
                Statistik per område
              </h1>
              <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                SCB:s statistik för Sveriges cirka 6 000 DeSO- och 3 000 RegSO-områden. Välj statistik och år, och
                sök på en adress för att se området du bor i.
              </p>
            </div>

            <RegionalStatisticsView />
          </div>
        </main>

        <Footer />
      </div>
    </>
  );
};

export default RegionalStatistics;
