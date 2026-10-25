import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import SeatDistribution from '@/components/SeatDistribution';
import PoliticianCard from '@/components/PoliticianCard';
import ParliamentBlog from '@/components/ParliamentBlog';
import { partyColor, useComposition, useElections, useParliamentMembers, useParties } from '@/api/parliament';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import { ArrowLeft, Building, Calendar, Loader2, Search, Users } from 'lucide-react';

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

      <main className='container mx-auto px-4 py-8'>
        {/* Back link */}
        <Link to='/'
              className='inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-6'>
          <ArrowLeft className='w-4 h-4' />
          Back to Dashboard
        </Link>

        {/* Hero Section */}
        <section className='mb-8'>
          <div className='flex items-center gap-3 mb-4'>
            <div className='p-3 rounded-xl bg-primary/10'>
              <Building className='w-8 h-8 text-primary' />
            </div>
            <div>
              <h1 className='text-3xl md:text-4xl font-display font-bold'>Sveriges riksdag</h1>
              <p className='text-muted-foreground'>Riksdagens komposition och medlemmar</p>
            </div>
          </div>
        </section>

        {/* Year Selection */}
        <section className='mb-6'>
          <div className='flex items-center gap-4'>
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
              <Tabs value={seatView} onValueChange={(v) => setSeatView(v as SeatView)}>
                <TabsList>
                  <TabsTrigger value='election'>Valresultat</TabsTrigger>
                  <TabsTrigger value='current'>Nuvarande fördelning</TabsTrigger>
                </TabsList>
              </Tabs>
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
            <h2 className='text-2xl font-display font-semibold'>Members of Parliament</h2>
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
                placeholder='Search by name, role, or constituency...'
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className='pl-10 bg-card border-border/50'
              />
            </div>
            <Select value={partyFilter} onValueChange={setPartyFilter}>
              <SelectTrigger className='w-full md:w-48 bg-card border-border/50'>
                <SelectValue placeholder='Filter by party' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value='all'>All Parties</SelectItem>
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

          {!membersLoading && filteredPoliticians.length === 0 && (
            <div className='text-center py-12 text-muted-foreground'>
              <Users className='w-12 h-12 mx-auto mb-4 opacity-50' />
              <p>No politicians found matching your criteria</p>
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
