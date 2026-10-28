import { Helmet } from 'react-helmet-async';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import LocalElectionView from '@/components/LocalElectionView';
import { Vote } from 'lucide-react';

const Constituencies = () => {
  return (
    <>
      <Helmet>
        <title>Valkretsar - Röstningsresultat per valkrets | Macro Hub</title>
        <meta name="description" content="Utforska resultatet i riksdagsvalet för Sveriges 29 valkretsar. Se hur varje valkrets röstade och vilka ledamöter som valdes med interaktiv karta." />
      </Helmet>

      <div className="min-h-screen bg-background">
        <Header />

        <main className="container mx-auto px-4 pt-24 pb-16">
          <div className="max-w-6xl mx-auto">
            <div className="text-center mb-12">
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4">
                <Vote className="h-4 w-4" />
                Riksdagsval
              </div>
              <h1 className="text-4xl md:text-5xl font-bold text-foreground mb-4">
                Röstningsresultat per valkrets
              </h1>
              <p className="text-lg text-muted-foreground max-w-3xl mx-auto">
                En valkrets är det område där rösterna räknas och riksdagens mandat fördelas. Sverige har 29 valkretsar.
                De flesta är ett län, men Stockholms, Skåne och Västra Götalands län är uppdelade, och Stockholms, Malmö
                och Göteborgs kommuner är egna valkretsar. 310 av riksdagens 349 mandat är fasta valkretsmandat som
                fördelas efter rösterna i varje valkrets. De 39 utjämningsmandaten fördelas så att partiernas mandat i
                hela landet speglar deras röstandel. Kartan är färgad efter största parti.
              </p>
            </div>

            <LocalElectionView
              kind="constituencies"
              topoUrl="/geo/valkretsar.topo.json"
              objectName="valkretsar"
              icon={<Vote className="h-12 w-12" />}
              labels={{
                areaSingular: 'valkrets',
                searchPlaceholder: 'Sök valkrets…',
                pickTitle: 'Välj en valkrets',
                pickHint: 'Klicka på en valkrets på kartan eller sök efter den för att se valresultatet i riksdagsvalet. Mandaten per valkrets räknar med de utjämningsmandat som valkretsen tilldelats.',
                governanceTitle: name => `Styre i ${name}`,
              }}
            />

            <div className="mt-8 grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">29</div>
                <div className="text-sm text-muted-foreground">Valkretsar</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">349</div>
                <div className="text-sm text-muted-foreground">Mandat</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">310</div>
                <div className="text-sm text-muted-foreground">Fasta valkretsmandat</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">39</div>
                <div className="text-sm text-muted-foreground">Utjämningsmandat</div>
              </div>
            </div>
          </div>
        </main>

        <Footer />
      </div>
    </>
  );
};

export default Constituencies;
