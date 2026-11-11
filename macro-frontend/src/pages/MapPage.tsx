import { lazy, ReactNode, Suspense, useEffect } from 'react';
import { Helmet } from 'react-helmet-async';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Building2, MapPin, MapPinned, Vote } from 'lucide-react';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group';
import { MAP_CATEGORIES, MapCategory, parseMapCategory } from './mapCategories';

// MapLibre is about 1 MB, so every map loads only when the page is visited
const RegionalStatisticsView = lazy(() => import('@/components/regionalmap/RegionalStatisticsView'));
const LocalElectionView = lazy(() => import('@/components/LocalElectionView'));

const loadingMap = <div className='min-h-[600px]' />;

type CategoryContent = {
  label: string;
  icon: (className: string) => ReactNode;
  title: string;
  metaDescription: string;
  badge: string;
  heading: string;
  intro: ReactNode;
  view: ReactNode;
};

const CONTENT: Record<MapCategory, CategoryContent> = {
  omraden: {
    label: 'Områdesstatistik',
    icon: className => <MapPinned className={className} />,
    title: 'Karta - Statistik per DeSO och RegSO | Macro Hub',
    metaDescription: 'Utforska SCB:s statistik per DeSO- och RegSO-område på en interaktiv karta. Sök på en adress och se hur området placerar sig i kommunen och i Sverige.',
    badge: 'Områdesstatistik',
    heading: 'Statistik per område',
    intro: (
      <>
        SCB:s statistik för Sveriges cirka 6 000 DeSO- och 3 000 RegSO-områden. Välj statistik och år, och sök på en
        adress för att se området du bor i.
      </>
    ),
    view: (
      <Suspense fallback={loadingMap}>
        <RegionalStatisticsView />
      </Suspense>
    ),
  },
  kommuner: {
    label: 'Kommunval',
    icon: className => <Building2 className={className} />,
    title: 'Karta - Röstningsresultat per kommun | Macro Hub',
    metaDescription: 'Utforska röstningsresultat för varje kommun i Sverige. Se hur olika kommuner röstade i valet med interaktiv karta.',
    badge: 'Kommunala valresultat',
    heading: 'Röstningsresultat per kommun',
    intro: 'Resultat i kommunfullmäktigevalet för alla 290 kommuner. Kartan är färgad efter största parti.',
    view: (
      <Suspense fallback={loadingMap}>
        <LocalElectionView
          kind='municipalities'
          topoUrl='/geo/kommuner.topo.json'
          objectName='kommuner'
          icon={<Building2 className='h-12 w-12' />}
          labels={{
            areaSingular: 'kommun',
            searchPlaceholder: 'Sök kommun…',
            pickTitle: 'Välj en kommun',
            pickHint: 'Klicka på en kommun på kartan eller sök efter den för att se valresultatet i kommunfullmäktigevalet.',
            governanceTitle: name => `Kommunalt styre i ${name}`,
          }}
        />
      </Suspense>
    ),
  },
  regioner: {
    label: 'Regionval',
    icon: className => <MapPin className={className} />,
    title: 'Karta - Röstningsresultat per region | Macro Hub',
    metaDescription: 'Utforska röstningsresultat för varje region i Sverige. Se hur olika län röstade i valet med interaktiv karta.',
    badge: 'Regionala valresultat',
    heading: 'Röstningsresultat per region',
    intro: 'Resultat i regionfullmäktigevalet för Sveriges 20 regioner. Kartan är färgad efter största parti.',
    view: (
      <Suspense fallback={loadingMap}>
        <LocalElectionView
          kind='regions'
          topoUrl='/geo/lan.topo.json'
          objectName='lan'
          icon={<MapPin className='h-12 w-12' />}
          labels={{
            areaSingular: 'region',
            searchPlaceholder: 'Sök region…',
            pickTitle: 'Välj en region',
            pickHint: 'Klicka på en region på kartan eller sök efter den för att se valresultatet i regionfullmäktigevalet. Gotland har inget regionfullmäktige; kommunen sköter regionens uppgifter.',
            governanceTitle: name => `Regionalt styre i ${name}`,
          }}
        />
      </Suspense>
    ),
  },
  valkretsar: {
    label: 'Riksdagsval per valkrets',
    icon: className => <Vote className={className} />,
    title: 'Karta - Röstningsresultat per valkrets | Macro Hub',
    metaDescription: 'Utforska resultatet i riksdagsvalet för Sveriges 29 valkretsar. Se hur varje valkrets röstade och vilka ledamöter som valdes med interaktiv karta.',
    badge: 'Riksdagsval',
    heading: 'Röstningsresultat per valkrets',
    intro: (
      <>
        En valkrets är det område där rösterna räknas och riksdagens mandat fördelas. Sverige har 29 valkretsar.
        De flesta är ett län, men Stockholms, Skåne och Västra Götalands län är uppdelade, och Stockholms, Malmö
        och Göteborgs kommuner är egna valkretsar. 310 av riksdagens 349 mandat är fasta valkretsmandat som
        fördelas efter rösterna i varje valkrets. De 39 utjämningsmandaten fördelas så att partiernas mandat i
        hela landet speglar deras röstandel. Kartan är färgad efter största parti.
      </>
    ),
    view: (
      <Suspense fallback={loadingMap}>
        <LocalElectionView
          kind='constituencies'
          topoUrl='/geo/valkretsar.topo.json'
          objectName='valkretsar'
          icon={<Vote className='h-12 w-12' />}
          labels={{
            areaSingular: 'valkrets',
            searchPlaceholder: 'Sök valkrets…',
            pickTitle: 'Välj en valkrets',
            pickHint: 'Klicka på en valkrets på kartan eller sök efter den för att se valresultatet i riksdagsvalet. Mandaten per valkrets räknar med de utjämningsmandat som valkretsen tilldelats.',
            governanceTitle: name => `Styre i ${name}`,
          }}
        />
      </Suspense>
    ),
  },
};

const MapPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const rawCategory = searchParams.get('k');
  const category = parseMapCategory(rawCategory);
  const content = CONTENT[category];

  useEffect(() => {
    if (rawCategory !== null && rawCategory !== category) {
      const params = new URLSearchParams(searchParams);
      params.delete('k');
      navigate({ search: params.toString() }, { replace: true });
    }
  }, [rawCategory, category, searchParams, navigate]);

  // Every other query parameter belongs to the category it was set in, so a switch starts from a clean query
  const selectCategory = (next: string) => {
    if (next && next !== category) {
      navigate({ search: new URLSearchParams({ k: next }).toString() });
    }
  };

  return (
    <>
      <Helmet>
        <title>{content.title}</title>
        <meta name='description' content={content.metaDescription} />
      </Helmet>

      <div className='min-h-screen bg-background'>
        <Header />

        <main className='container mx-auto px-4 pt-24 pb-16'>
          <div className='max-w-7xl mx-auto'>
            <ToggleGroup
              type='single'
              value={category}
              onValueChange={selectCategory}
              aria-label='Välj karta'
              className='flex-wrap mb-8'
            >
              {MAP_CATEGORIES.map(c => (
                <ToggleGroupItem key={c} value={c} className='gap-1.5'>
                  {CONTENT[c].icon('h-4 w-4')}
                  {CONTENT[c].label}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>

            <div className='text-center mb-8'>
              <div className='inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4'>
                {content.icon('h-4 w-4')}
                {content.badge}
              </div>
              <h1 className='text-4xl md:text-5xl font-bold text-foreground mb-4'>{content.heading}</h1>
              <p className='text-lg text-muted-foreground max-w-3xl mx-auto'>{content.intro}</p>
            </div>

            <div key={category}>{content.view}</div>
          </div>
        </main>

        <Footer />
      </div>
    </>
  );
};

export default MapPage;
