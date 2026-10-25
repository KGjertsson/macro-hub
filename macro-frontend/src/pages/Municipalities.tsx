import { Helmet } from 'react-helmet-async';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import LocalElectionView from '@/components/LocalElectionView';
import { Building2 } from 'lucide-react';

const Municipalities = () => {
  return (
    <>
      <Helmet>
        <title>Kommuner - Röstningsresultat per kommun | Macro Hub</title>
        <meta name="description" content="Utforska röstningsresultat för varje kommun i Sverige. Se hur olika kommuner röstade i valet med interaktiv karta." />
      </Helmet>
      
      <div className="min-h-screen bg-background">
        <Header />
        
        <main className="container mx-auto px-4 pt-24 pb-16">
          <div className="max-w-6xl mx-auto">
            <div className="text-center mb-12">
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4">
                <Building2 className="h-4 w-4" />
                Kommunala valresultat
              </div>
              <h1 className="text-4xl md:text-5xl font-bold text-foreground mb-4">
                Röstningsresultat per kommun
              </h1>
              <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                Resultat i kommunfullmäktigevalet för alla 290 kommuner. Kartan är färgad efter största parti.
              </p>
            </div>

            <LocalElectionView
              kind="municipalities"
              topoUrl="/geo/kommuner.topo.json"
              objectName="kommuner"
              icon={<Building2 className="h-12 w-12" />}
              labels={{
                areaSingular: 'kommun',
                searchPlaceholder: 'Sök kommun…',
                pickTitle: 'Välj en kommun',
                pickHint: 'Klicka på en kommun på kartan eller sök efter den för att se valresultatet i kommunfullmäktigevalet.',
                governanceTitle: name => `Kommunalt styre i ${name}`,
              }}
            />

            <div className="mt-8 grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">290</div>
                <div className="text-sm text-muted-foreground">Kommuner</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">8</div>
                <div className="text-sm text-muted-foreground">Riksdagspartier</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">84,2 %</div>
                <div className="text-sm text-muted-foreground">Valdeltagande</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold" style={{ color: '#E8112d' }}>S</div>
                <div className="text-sm text-muted-foreground">Största parti</div>
              </div>
            </div>
          </div>
        </main>

        <Footer />
      </div>
    </>
  );
};

export default Municipalities;
