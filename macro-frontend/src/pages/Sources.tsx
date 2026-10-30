import Header from '@/components/Header';
import Footer from '@/components/Footer';
import { Globe, ExternalLink } from 'lucide-react';

type Source = {
  name: string;
  description: string;
  url: string;
};

type SourceGroup = {
  title: string;
  sources: Source[];
};

const Sources = () => {
  const groups: SourceGroup[] = [
    {
      title: 'Data',
      sources: [
        {
          name: 'Sveriges riksbank (SWEA API)',
          description:
            'Styrräntan, statsobligationer (2, 5, 7 och 10 år), statsskuldväxlar (1, 3, 6 och 12 månader), euromarknadsräntor (3 och 6 månader för Danmark, euroområdet, Storbritannien, Japan, Norge och USA), utländska statsobligationsräntor (10 år, och 5 år för alla utom Danmark, Finland och Norge) och växelkursen SEK/USD.',
          url: 'https://developer.api.riksbank.se',
        },
        {
          name: 'FRED, Federal Reserve Bank of St. Louis',
          description:
            'Nyckeltal för USA: effektiv federal funds rate, arbetslöshet, BNP, statsskuld (nominell och i % av BNP), budgetsaldo, inflation (median-KPI), aktieindexen S&P 500, Dow Jones och Nasdaq-100 samt volatilitetsindexet VIX.',
          url: 'https://fred.stlouisfed.org',
        },
        {
          name: 'Eurostat',
          description:
            'Offentlig bruttoskuld (gov_10dd_edpt1) i miljoner euro och i % av BNP för EU-länderna, euroområdet och EU27. Får återanvändas med angivande av källa.',
          url: 'https://ec.europa.eu/eurostat',
        },
        {
          name: 'Statistiska centralbyrån (SCB)',
          description:
            'Områdesstatistik per DeSO och RegSO från Statistikdatabasen (PxWeb API): befolkning, utländsk bakgrund, inkomster, ekonomisk standard, utbildning, boende, ohälsotal, försörjningsstöd, flyttnetto, valdeltagande med mera. Gränserna för kommuner, län, valkretsar, DeSO och RegSO kommer från SCB:s geodata. Licens CC0.',
          url: 'https://www.scb.se',
        },
        {
          name: 'Riksdagens öppna data',
          description:
            'Riksdagsledamöter och deras uppdrag, voteringar, anföranden och motioner från data.riksdagen.se. Får användas fritt med Sveriges riksdag angiven som källa.',
          url: 'https://data.riksdagen.se',
        },
        {
          name: 'Valmyndigheten',
          description:
            'Slutliga valresultat från resultat.val.se: röster, mandatfördelning, valdeltagande och valda ledamöter i kommun-, region- och riksdagsvalen (per valkrets), samt mandaten i tidigare riksdagsval.',
          url: 'https://www.val.se',
        },
      ],
    },
    {
      title: 'Karttjänster',
      sources: [
        {
          name: 'OpenFreeMap',
          description: 'Bakgrundskartan på Områdesstatistik (kartstilen Positron), utan API-nyckel.',
          url: 'https://openfreemap.org',
        },
        {
          name: 'OpenMapTiles',
          description: 'Kartplattorna och schemat som OpenFreeMaps bakgrundskarta bygger på.',
          url: 'https://openmaptiles.org',
        },
        {
          name: 'OpenStreetMap',
          description:
            'Kartdata i bakgrundskartan och i adressökningen. © OpenStreetMap-bidragsgivare, licens ODbL.',
          url: 'https://www.openstreetmap.org/copyright',
        },
        {
          name: 'Photon (komoot)',
          description: 'Adressökningen på Områdesstatistik, som hittar det område en adress ligger i.',
          url: 'https://photon.komoot.io',
        },
      ],
    },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />
      <main className="flex-1 container mx-auto px-4 pt-24 pb-12">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center gap-3 mb-8">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-primary">
              <Globe className="h-6 w-6 text-primary-foreground" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-foreground">Källor</h1>
              <p className="text-muted-foreground">Datakällor som används i Macro Hub</p>
            </div>
          </div>

          <div className="space-y-10">
            {groups.map((group) => (
              <section key={group.title}>
                <h2 className="text-xl font-semibold text-foreground mb-4">{group.title}</h2>
                <div className="grid gap-4">
                  {group.sources.map((source) => (
                    <a
                      key={source.name}
                      href={source.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="group block p-6 rounded-lg border border-border bg-card hover:border-primary/50 hover:shadow-lg transition-all"
                    >
                      <div className="flex items-start justify-between">
                        <div>
                          <h3 className="text-lg font-semibold text-foreground group-hover:text-primary transition-colors">
                            {source.name}
                          </h3>
                          <p className="text-muted-foreground mt-1">{source.description}</p>
                          <p className="text-sm text-primary mt-2">{source.url}</p>
                        </div>
                        <ExternalLink className="h-5 w-5 text-muted-foreground group-hover:text-primary transition-colors flex-shrink-0" />
                      </div>
                    </a>
                  ))}
                </div>
              </section>
            ))}
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default Sources;
