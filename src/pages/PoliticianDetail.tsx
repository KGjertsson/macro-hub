import { Link, useParams } from 'react-router-dom';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import { getPartyById, getPoliticianById } from '@/data/parliamentData';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ArrowLeft, Briefcase, Building2, Calendar, Trophy, User, Users } from 'lucide-react';

const PoliticianDetail = () => {
  const { id } = useParams<{ id: string }>();
  const politician = getPoliticianById(id || '');
  const party = politician ? getPartyById(politician.party) : undefined;

  if (!politician || !party) {
    return (
      <div className='min-h-screen bg-background'>
        <Header />
        <main className='container mx-auto px-4 py-8'>
          <div className='text-center py-20'>
            <User className='w-16 h-16 mx-auto mb-4 text-muted-foreground' />
            <h1 className='text-2xl font-bold mb-2'>Politician Not Found</h1>
            <p className='text-muted-foreground mb-6'>The requested profile could not be found.</p>
            <Link
              to='/parliament'
              className='inline-flex items-center gap-2 text-primary hover:underline'
            >
              <ArrowLeft className='w-4 h-4' />
              Back to Parliament
            </Link>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className='min-h-screen bg-background'>
      <Header />

      <main className='container mx-auto px-4 py-8'>
        {/* Back link */}
        <Link
          to='/parliament'
          className='inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-6'
        >
          <ArrowLeft className='w-4 h-4' />
          Back to Parliament
        </Link>

        {/* Profile Header */}
        <section className='mb-8'>
          <Card className='glass-card overflow-hidden'>
            <div
              className='h-2'
              style={{ backgroundColor: party.color }}
            />
            <CardContent className='p-6 md:p-8'>
              <div className='flex flex-col md:flex-row gap-6 items-start'>
                {/* Avatar */}
                <div
                  className='w-24 h-24 md:w-32 md:h-32 rounded-full flex items-center justify-center flex-shrink-0'
                  style={{
                    backgroundColor: party.color + '20',
                    borderColor: party.color,
                    borderWidth: 3
                  }}
                >
                  <User className='w-12 h-12 md:w-16 md:h-16' style={{ color: party.color }} />
                </div>

                <div className='flex-1'>
                  <div className='flex flex-wrap items-center gap-3 mb-2'>
                    <h1 className='text-3xl md:text-4xl font-display font-bold'>{politician.name}</h1>
                    <Badge
                      className='text-sm px-3 py-1'
                      style={{ backgroundColor: party.color, color: '#fff' }}
                    >
                      {party.shortName}
                    </Badge>
                  </div>

                  <p className='text-xl text-muted-foreground mb-4'>{politician.role}</p>

                  <div className='flex flex-wrap gap-4 text-sm'>
                    <span className='flex items-center gap-2 text-muted-foreground'>
                      <Building2 className='w-4 h-4' />
                      {politician.constituency}
                    </span>
                    <span className='flex items-center gap-2 text-muted-foreground'>
                      <Calendar className='w-4 h-4' />
                      {politician.yearsInParliament} years in Parliament
                    </span>
                    <span className='flex items-center gap-2 text-muted-foreground'>
                      <Users className='w-4 h-4' />
                      {party.name}
                    </span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </section>

        <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
          {/* Achievements */}
          <Card className='glass-card'>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Trophy className='w-5 h-5 text-primary' />
                Key Achievements
              </CardTitle>
            </CardHeader>
            <CardContent>
              <ul className='space-y-4'>
                {politician.achievements.map((achievement, index) => (
                  <li
                    key={index}
                    className='flex items-start gap-3 animate-fade-in'
                    style={{ animationDelay: `${index * 0.1}s` }}
                  >
                    <span
                      className='w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0 mt-0.5'
                      style={{ backgroundColor: party.color + '30', color: party.color }}
                    >
                      {index + 1}
                    </span>
                    <span>{achievement}</span>
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>

          {/* Committees */}
          <Card className='glass-card'>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Briefcase className='w-5 h-5 text-primary' />
                Committee Memberships
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className='space-y-3'>
                {politician.committees.map((committee, index) => (
                  <div
                    key={index}
                    className='p-4 rounded-lg bg-secondary/30 border border-border/50 animate-fade-in'
                    style={{ animationDelay: `${index * 0.1}s` }}
                  >
                    <div className='flex items-center gap-3'>
                      <div
                        className='w-2 h-2 rounded-full'
                        style={{ backgroundColor: party.color }}
                      />
                      <span className='font-medium'>{committee}</span>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Party Info */}
          <Card className='glass-card lg:col-span-2'>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Users className='w-5 h-5 text-primary' />
                Party Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
                <div className='p-4 rounded-lg bg-secondary/30 border border-border/50'>
                  <p className='text-sm text-muted-foreground mb-1'>Party Name</p>
                  <p className='font-semibold flex items-center gap-2'>
                    <span
                      className='w-3 h-3 rounded-full'
                      style={{ backgroundColor: party.color }}
                    />
                    {party.name}
                  </p>
                </div>
                <div className='p-4 rounded-lg bg-secondary/30 border border-border/50'>
                  <p className='text-sm text-muted-foreground mb-1'>Parliamentary Seats</p>
                  <p className='font-semibold text-2xl' style={{ color: party.color }}>
                    {party.seats}
                  </p>
                </div>
                <div className='p-4 rounded-lg bg-secondary/30 border border-border/50'>
                  <p className='text-sm text-muted-foreground mb-1'>Ideology</p>
                  <p className='font-semibold'>{party.ideology}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default PoliticianDetail;
