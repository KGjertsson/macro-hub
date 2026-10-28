import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import SeatDistribution from '@/components/SeatDistribution';
import PoliticianCard from '@/components/PoliticianCard';
import ParliamentBlog from '@/components/ParliamentBlog';
import { partyColor, seatLabel, useComposition, useElections, useParliamentMembers, useParties } from '@/api/parliament';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import { ArrowLeft, Building, Calendar, Info, Loader2, Search, Users } from 'lucide-react';

type SeatView = 'election' | 'current';

const Parliament = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [partyFilter, setPartyFilter] = useState<string>('all');
  const [yearOverride, setYearOverride] = useState<number>();
  const [seatView, setSeatView] = useState<SeatView>('election');

  const { data: elections, isLoading: electionsLoading } = useElections();
  const { data: composition } = useComposition();
  const { data: parties } = useParties();

  // The newest election is the default term
  const selectedYear = yearOverride ?? elections?.[0]?.year;
  const election = elections?.find(e => e.year === selectedYear);
  const isLatestTerm = selectedYear === elections?.[0]?.year;
  const showCurrent = seatView === 'current' && isLatestTerm && !!composition;

  // The current view only exists for the latest term, so it is compared with the latest election
  const seatChanges = useMemo(() => {
    if (!composition || !election) return [];
    const parties = [...election.parties, ...composition.parties.filter(c => !election.parties.some(e => e.code === c.code))];
    const seats = (list: typeof parties, code: string) => list.find(p => p.code === code)?.seats ?? 0;
    return parties
      .map(party => ({ party, diff: seats(composition.parties, party.code) - seats(election.parties, party.code) }))
      .filter(c => c.diff !== 0);
  }, [composition, election]);

  const { data: members, isLoading: membersLoading } = useParliamentMembers(selectedYear);

  const memberParties = useMemo(() => {
    const codes = new Set((members ?? []).map(m => m.party).filter(Boolean));
    return (parties ?? []).filter(p => codes.has(p.code));
  }, [members, parties]);

  const filteredPoliticians = (members ?? []).filter(member => {
    const query = searchQuery.toLowerCase();
    const matchesSearch = member.name.toLowerCase().includes(query) ||
      member.currentRole.toLowerCase().includes(query) ||
      member.constituency.toLowerCase().includes(query);
    const matchesParty = partyFilter === 'all' || member.party === partyFilter;
    return matchesSearch && matchesParty;
  });

  return (
    <div className='min-h-screen bg-background'>
      <Header />

      <main className='container mx-auto px-4 pt-24 pb-12'>
        {/* Back link */}
        <Link to='/'
              className='inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-6'>
          <ArrowLeft className='w-4 h-4' />
          Tillbaka till startsidan
        </Link>

        {/* Hero Section */}
        <section className='mb-8'>
          <div className='flex items-center gap-3 mb-4'>
            <div className='p-3 rounded-xl bg-primary/10'>
              <Building className='w-8 h-8 text-primary' />
            </div>
            <div>
              <h1 className='text-3xl md:text-4xl font-display font-bold'>Sveriges riksdag</h1>
              <p className='text-muted-foreground'>Riksdagens sammansättning och ledamöter</p>
            </div>
          </div>
        </section>

        {/* Year Selection */}
        <section className='mb-6'>
          <div className='flex flex-wrap items-center gap-4'>
            <div className='flex items-center gap-2'>
              <Calendar className='w-5 h-5 text-muted-foreground' />
              <span className='text-sm font-medium'>Valår:</span>
            </div>
            <Select
              value={selectedYear?.toString()}
              onValueChange={(v) => {
                setYearOverride(parseInt(v));
                setPartyFilter('all');
              }}
            >
              <SelectTrigger className='w-32 bg-card border-border/50'>
                <SelectValue placeholder='–' />
              </SelectTrigger>
              <SelectContent>
                {(elections ?? []).map(e => (
                  <SelectItem key={e.year} value={e.year.toString()}>
                    {e.year}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {isLatestTerm && composition && (
              <div className='flex items-center gap-1'>
                <Tabs value={seatView} onValueChange={(v) => setSeatView(v as SeatView)}>
                  <TabsList>
                    <TabsTrigger value='election'>Valresultat</TabsTrigger>
                    <TabsTrigger value='current'>Nuvarande fördelning</TabsTrigger>
                  </TabsList>
                </Tabs>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button
                      variant='ghost'
                      size='icon'
                      className='h-8 w-8 shrink-0 text-muted-foreground'
                      aria-label='Om valresultat och nuvarande fördelning'
                    >
                      <Info className='w-4 h-4' />
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className='w-80 space-y-2 text-sm'>
                    <p>
                      <strong>Valresultat</strong> visar hur riksdagens 349 mandat fördelades efter valet, enligt
                      Valmyndighetens slutliga resultat.
                    </p>
                    <p>
                      <strong>Nuvarande fördelning</strong> räknar de ledamöter som sitter i riksdagen i dag och det
                      parti de tillhör nu. Den kan skilja sig från valresultatet när en ledamot lämnar sitt parti och
                      blir partilös (politisk vilde). Statsråd och tjänstlediga ledamöter ersätts av en ersättare från
                      samma parti, så de påverkar inte fördelningen.
                    </p>
                  </PopoverContent>
                </Popover>
              </div>
            )}
          </div>
        </section>

        {/* Seat Distribution */}
        <section className='mb-12 animate-fade-in'>
          {electionsLoading && <Skeleton className='h-96 w-full rounded-xl' />}
          {!electionsLoading && election && (
            <SeatDistribution
              title={showCurrent ? `Tjänstgörande ledamöter ${composition.asOf}` : `Valresultat ${election.year}`}
              parties={showCurrent ? composition.parties : election.parties}
            />
          )}
          {!electionsLoading && election && showCurrent && (
            <p className='mt-4 text-sm text-muted-foreground'>
              {seatChanges.length === 0 ? (
                `Samma fördelning som efter valet ${election.year}.`
              ) : (
                <>
                  Skillnad mot valet {election.year}:{' '}
                  {seatChanges.map(({ party, diff }, index) => (
                    <span key={party.code}>
                      {index > 0 && ' · '}
                      <span className='inline-flex items-center gap-1.5'>
                        <span className='w-2.5 h-2.5 rounded-full' style={{ backgroundColor: party.color }} />
                        <span className='font-medium text-foreground'>
                          {seatLabel(party)} {diff > 0 ? `+${diff}` : `−${-diff}`}
                        </span>
                      </span>
                    </span>
                  ))}
                </>
              )}
            </p>
          )}
          {!electionsLoading && !election && (
            <p className='text-center py-12 text-muted-foreground'>Inga valresultat tillgängliga</p>
          )}
        </section>

        {/* Politicians Section */}
        <section>
          <div className='flex items-center gap-3 mb-6'>
            <div className='p-2 rounded-lg bg-secondary/50'>
              <Users className='w-5 h-5 text-primary' />
            </div>
            <h2 className='text-2xl font-display font-semibold'>Riksdagsledamöter</h2>
            {members && (
              <span className='text-sm text-muted-foreground'>
                {filteredPoliticians.length} av {members.length}
              </span>
            )}
          </div>

          {/* Filters */}
          <div className='flex flex-col md:flex-row gap-4 mb-6'>
            <div className='relative flex-1 max-w-md'>
              <Search className='absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground' />
              <Input
                placeholder='Sök på namn, roll eller valkrets...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className='pl-10 bg-card border-border/50'
              />
            </div>
            <Select value={partyFilter} onValueChange={setPartyFilter}>
              <SelectTrigger className='w-full md:w-48 bg-card border-border/50'>
                <SelectValue placeholder='Filtrera på parti' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>Alla partier</SelectItem>
                {memberParties.map(party => (
                  <SelectItem key={party.code} value={party.code}>
                    <span className='flex items-center gap-2'>
                      <span
                        className='w-3 h-3 rounded-full'
                        style={{ backgroundColor: party.color }}
                      />
                      {party.name}
                    </span>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Politicians Grid */}
          <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6'>
            {filteredPoliticians.map((member, index) => (
              <div
                key={member.id}
                className='animate-fade-in'
                style={{ animationDelay: `${Math.min(index, 20) * 0.03}s` }}
              >
                <PoliticianCard member={member} partyColor={partyColor(parties, member.party)} />
              </div>
            ))}
          </div>

          {membersLoading && (
            <div className='flex justify-center py-12 text-muted-foreground'>
              <Loader2 className='w-6 h-6 animate-spin' />
            </div>
          )}

          {!membersLoading && members && filteredPoliticians.length === 0 && (
            <div className='text-center py-12 text-muted-foreground'>
              <Users className='w-12 h-12 mx-auto mb-4 opacity-50' />
              <p>Inga ledamöter matchar dina sökkriterier</p>
            </div>
          )}
        </section>

        {/* Blog Section */}
        <ParliamentBlog />
      </main>

      <Footer />
    </div>
  );
};

export default Parliament;
