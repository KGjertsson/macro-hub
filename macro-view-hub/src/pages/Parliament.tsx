import { useState } from 'react';
import { Link } from 'react-router-dom';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import SeatDistribution from '@/components/SeatDistribution';
import PoliticianCard from '@/components/PoliticianCard';
import ParliamentBlog from '@/components/ParliamentBlog';
import { parties, politicians } from '@/data/parliamentData';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ArrowLeft, Building, Search, Users } from 'lucide-react';

const Parliament = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [partyFilter, setPartyFilter] = useState<string>('all');

  const filteredPoliticians = politicians.filter(politician => {
    const matchesSearch = politician.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      politician.role.toLowerCase().includes(searchQuery.toLowerCase()) ||
      politician.constituency.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesParty = partyFilter === 'all' || politician.party === partyFilter;
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
        <section className='mb-12'>
          <div className='flex items-center gap-3 mb-4'>
            <div className='p-3 rounded-xl bg-primary/10'>
              <Building className='w-8 h-8 text-primary' />
            </div>
            <div>
              <h1 className='text-3xl md:text-4xl font-display font-bold'>Parliament Overview</h1>
              <p className='text-muted-foreground'>Riksdag composition and member profiles</p>
            </div>
          </div>
        </section>

        {/* Seat Distribution */}
        <section className='mb-12 animate-fade-in'>
          <SeatDistribution />
        </section>

        {/* Politicians Section */}
        <section>
          <div className='flex items-center gap-3 mb-6'>
            <div className='p-2 rounded-lg bg-secondary/50'>
              <Users className='w-5 h-5 text-primary' />
            </div>
            <h2 className='text-2xl font-display font-semibold'>Members of Parliament</h2>
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
                {parties.map(party => (
                  <SelectItem key={party.id} value={party.id}>
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
            {filteredPoliticians.map((politician, index) => (
              <div
                key={politician.id}
                className='animate-fade-in'
                style={{ animationDelay: `${index * 0.05}s` }}
              >
                <PoliticianCard politician={politician} />
              </div>
            ))}
          </div>

          {filteredPoliticians.length === 0 && (
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
