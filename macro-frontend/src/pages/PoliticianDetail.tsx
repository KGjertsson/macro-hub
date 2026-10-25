import { Link, useParams } from 'react-router-dom';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import { MemberAvatar } from '@/components/PoliticianCard';
import { useElections, useMember, useParties } from '@/api/parliament';
import { Assignment, ParliamentDocument } from '@/models/Parliament';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
  ArrowLeft,
  BookOpen,
  Briefcase,
  Building2,
  Calendar,
  FileText,
  Loader2,
  Mail,
  Megaphone,
  Phone,
  User,
  Users,
  Vote,
  Globe,
} from 'lucide-react';

const assignmentTypeLabels: Record<string, string> = {
  kammaruppdrag: 'Kammaren',
  uppdrag: 'Utskott & nämnder',
  Departement: 'Regeringen',
  Riksdagsorgan: 'Riksdagsorgan',
  partiuppdrag: 'Partiuppdrag',
  talmansuppdrag: 'Talmansuppdrag',
  Europaparlamentet: 'Europaparlamentet',
};

const formatPeriod = (a: Assignment) => `${a.from ?? '?'} – ${a.current ? 'pågående' : (a.to ?? '')}`;

const NotFound = () => (
  <div className='text-center py-20'>
    <User className='w-16 h-16 mx-auto mb-4 text-muted-foreground' />
    <h1 className='text-2xl font-bold mb-2'>Ledamoten hittades inte</h1>
    <p className='text-muted-foreground mb-6'>Den efterfrågade profilen kunde inte hittas.</p>
    <Link to='/parliament' className='inline-flex items-center gap-2 text-primary hover:underline'>
      <ArrowLeft className='w-4 h-4' />
      Tillbaka till riksdagen
    </Link>
  </div>
);

const DocumentList = ({ documents, empty }: { documents: ParliamentDocument[]; empty: string }) =>
  documents.length === 0
    ? <p className='text-sm text-muted-foreground'>{empty}</p>
    : (
      <ul className='space-y-3'>
        {documents.map((d, index) => (
          <li key={index} className='text-sm'>
            <a href={d.url} target='_blank' rel='noreferrer' className='font-medium hover:text-primary hover:underline'>
              {d.title || 'Utan titel'}
            </a>
            <p className='text-xs text-muted-foreground'>{d.date}</p>
          </li>
        ))}
      </ul>
    );

const PoliticianDetail = () => {
  const { id } = useParams<{ id: string }>();
  const { data: member, isLoading, isError } = useMember(id);
  const { data: parties } = useParties();
  const { data: elections } = useElections();

  if (isLoading) {
    return (
      <div className='min-h-screen bg-background'>
        <Header />
        <main className='container mx-auto px-4 pt-36 pb-20 flex justify-center text-muted-foreground'>
          <Loader2 className='w-8 h-8 animate-spin' />
        </main>
        <Footer />
      </div>
    );
  }

  if (isError || !member) {
    return (
      <div className='min-h-screen bg-background'>
        <Header />
        <main className='container mx-auto px-4 pt-24 pb-12'>
          <NotFound />
        </main>
        <Footer />
      </div>
    );
  }

  const summary = member.summary;
  const party = parties?.find(p => p.code === summary.party);
  const color = party?.color ?? '#9CA3AF';
  const latestElection = elections?.[0];
  const partySeats = latestElection?.parties.find(p => p.code === summary.party)?.seats;
  const currentAssignments = member.assignments.filter(a => a.current);
  const earlierAssignments = member.assignments.filter(a => !a.current);
  const votes = member.votes;
  const totalVotes = votes ? votes.yes + votes.no + votes.absent + votes.abstain : 0;
  const voteSegments = votes ? [
    { label: 'Ja', value: votes.yes, color: '#16a34a' },
    { label: 'Nej', value: votes.no, color: '#dc2626' },
    { label: 'Avstår', value: votes.abstain, color: '#ca8a04' },
    { label: 'Frånvarande', value: votes.absent, color: '#9ca3af' },
  ] : [];

  return (
    <div className='min-h-screen bg-background'>
      <Header />

      <main className='container mx-auto px-4 pt-24 pb-12'>
        <Link
          to='/parliament'
          className='inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-6'
        >
          <ArrowLeft className='w-4 h-4' />
          Tillbaka till riksdagen
        </Link>

        {/* Profile Header */}
        <section className='mb-8'>
          <Card className='glass-card overflow-hidden'>
            <div className='h-2' style={{ backgroundColor: color }} />
            <CardContent className='p-6 md:p-8'>
              <div className='flex flex-col md:flex-row gap-6 items-start'>
                <MemberAvatar
                  imageUrl={summary.imageUrl}
                  color={color}
                  className='w-24 h-24 md:w-32 md:h-32'
                  iconClassName='w-12 h-12 md:w-16 md:h-16'
                />

                <div className='flex-1'>
                  <div className='flex flex-wrap items-center gap-3 mb-2'>
                    <h1 className='text-3xl md:text-4xl font-display font-bold'>{summary.name}</h1>
                    {summary.party && (
                      <Badge className='text-sm px-3 py-1' style={{ backgroundColor: color, color: '#fff' }}>
                        {summary.party}
                      </Badge>
                    )}
                    {member.runningNextElection && <Badge variant='outline'>Kandiderar i nästa val</Badge>}
                  </div>

                  <p className='text-xl text-muted-foreground mb-1'>{summary.currentRole}</p>
                  <p className='text-sm text-muted-foreground mb-4'>{summary.status}</p>

                  <div className='flex flex-wrap gap-4 text-sm text-muted-foreground'>
                    <span className='flex items-center gap-2'>
                      <Building2 className='w-4 h-4' />
                      {summary.constituency}
                    </span>
                    <span className='flex items-center gap-2'>
                      <Calendar className='w-4 h-4' />
                      {summary.yearsInParliament} år i riksdagen
                    </span>
                    {party && (
                      <span className='flex items-center gap-2'>
                        <Users className='w-4 h-4' />
                        {party.name}
                      </span>
                    )}
                    {member.birthYear != null && (
                      <span className='flex items-center gap-2'>
                        <User className='w-4 h-4' />
                        Född {member.birthYear}{member.residence ? `, ${member.residence}` : ''}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </section>

        <div className='grid grid-cols-1 lg:grid-cols-2 gap-6'>
          {/* Voting record */}
          <Card className='glass-card'>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Vote className='w-5 h-5 text-primary' />
                Voteringar
              </CardTitle>
            </CardHeader>
            <CardContent>
              {votes && totalVotes > 0 ? (
                <>
                  <div className='flex items-baseline gap-2 mb-4'>
                    <span className='text-3xl font-bold' style={{ color }}>{summary.attendancePct ?? '–'} %</span>
                    <span className='text-sm text-muted-foreground'>närvaro i {totalVotes} voteringar</span>
                  </div>
                  <div className='flex h-3 rounded-full overflow-hidden mb-4'>
                    {voteSegments.map(s => (
                      <div key={s.label} style={{ width: `${(s.value / totalVotes) * 100}%`, backgroundColor: s.color }} />
                    ))}
                  </div>
                  <div className='grid grid-cols-2 gap-2 text-sm'>
                    {voteSegments.map(s => (
                      <span key={s.label} className='flex items-center gap-2'>
                        <span className='w-3 h-3 rounded-full' style={{ backgroundColor: s.color }} />
                        {s.label}: <span className='font-medium'>{s.value}</span>
                      </span>
                    ))}
                  </div>
                </>
              ) : (
                <p className='text-sm text-muted-foreground'>Ingen voteringsstatistik tillgänglig.</p>
              )}
            </CardContent>
          </Card>

          {/* Speeches & motions */}
          <Card className='glass-card'>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Megaphone className='w-5 h-5 text-primary' />
                Anföranden & motioner
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className='grid grid-cols-2 gap-4 mb-6'>
                <div className='p-4 rounded-lg bg-secondary/30 border border-border/50'>
                  <p className='text-sm text-muted-foreground mb-1'>Anföranden</p>
                  <p className='font-semibold text-2xl' style={{ color }}>{member.speechCount ?? '–'}</p>
                </div>
                <div className='p-4 rounded-lg bg-secondary/30 border border-border/50'>
                  <p className='text-sm text-muted-foreground mb-1'>Motioner</p>
                  <p className='font-semibold text-2xl' style={{ color }}>{member.motionCount ?? '–'}</p>
                </div>
              </div>
              <div className='grid grid-cols-1 md:grid-cols-2 gap-6'>
                <div>
                  <p className='text-sm font-medium mb-3 flex items-center gap-2'>
                    <Megaphone className='w-4 h-4' /> Senaste anföranden
                  </p>
                  <DocumentList documents={member.recentSpeeches} empty='Inga anföranden.' />
                </div>
                <div>
                  <p className='text-sm font-medium mb-3 flex items-center gap-2'>
                    <FileText className='w-4 h-4' /> Senaste motioner
                  </p>
                  <DocumentList documents={member.recentMotions} empty='Inga motioner.' />
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Assignments */}
          <Card className='glass-card'>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <Briefcase className='w-5 h-5 text-primary' />
                Uppdrag
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-6'>
              {[{ title: 'Nuvarande', items: currentAssignments }, { title: 'Tidigare', items: earlierAssignments }]
                .filter(group => group.items.length > 0)
                .map(group => (
                  <div key={group.title}>
                    <p className='text-sm font-medium text-muted-foreground mb-3'>{group.title}</p>
                    <div className='space-y-2 max-h-96 overflow-y-auto pr-1'>
                      {group.items.map((a, index) => (
                        <div key={index} className='p-3 rounded-lg bg-secondary/30 border border-border/50'>
                          <div className='flex items-center justify-between gap-3'>
                            <span className='font-medium'>{a.role}, {a.organName}</span>
                            <Badge variant='outline' className='text-xs flex-shrink-0'>
                              {assignmentTypeLabels[a.type] ?? a.type}
                            </Badge>
                          </div>
                          <p className='text-xs text-muted-foreground mt-1'>
                            {formatPeriod(a)}{a.status ? ` · ${a.status}` : ''}
                          </p>
                        </div>
                      ))}
                    </div>
                  </div>
                ))}
              {member.assignments.length === 0 && (
                <p className='text-sm text-muted-foreground'>Inga uppdrag registrerade.</p>
              )}
            </CardContent>
          </Card>

          {/* Biography & contact */}
          <Card className='glass-card'>
            <CardHeader>
              <CardTitle className='flex items-center gap-2'>
                <BookOpen className='w-5 h-5 text-primary' />
                Biografi & kontakt
              </CardTitle>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='flex flex-col gap-2 text-sm'>
                {member.contact.email && (
                  <a href={`mailto:${member.contact.email}`} className='flex items-center gap-2 hover:text-primary'>
                    <Mail className='w-4 h-4' /> {member.contact.email}
                  </a>
                )}
                {member.contact.phone && (
                  <span className='flex items-center gap-2'><Phone className='w-4 h-4' /> {member.contact.phone}</span>
                )}
                {member.contact.website && (
                  <a href={member.contact.website} target='_blank' rel='noreferrer'
                     className='flex items-center gap-2 hover:text-primary'>
                    <Globe className='w-4 h-4' /> {member.contact.website}
                  </a>
                )}
              </div>
              {member.biography.map((entry, index) => (
                <div key={index}>
                  <p className='text-sm font-medium text-muted-foreground mb-1'>{entry.label}</p>
                  <p className='text-sm whitespace-pre-line'>{entry.text}</p>
                </div>
              ))}
              {member.biography.length === 0 && !member.contact.email && !member.contact.phone && !member.contact.website && (
                <p className='text-sm text-muted-foreground'>Ingen biografi tillgänglig.</p>
              )}
            </CardContent>
          </Card>

          {/* Party Info */}
          {party && (
            <Card className='glass-card lg:col-span-2'>
              <CardHeader>
                <CardTitle className='flex items-center gap-2'>
                  <Users className='w-5 h-5 text-primary' />
                  Partiinformation
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
                  <div className='p-4 rounded-lg bg-secondary/30 border border-border/50'>
                    <p className='text-sm text-muted-foreground mb-1'>Parti</p>
                    <p className='font-semibold flex items-center gap-2'>
                      <span className='w-3 h-3 rounded-full' style={{ backgroundColor: color }} />
                      {party.name}
                    </p>
                  </div>
                  <div className='p-4 rounded-lg bg-secondary/30 border border-border/50'>
                    <p className='text-sm text-muted-foreground mb-1'>
                      Mandat {latestElection ? `(val ${latestElection.year})` : ''}
                    </p>
                    <p className='font-semibold text-2xl' style={{ color }}>{partySeats ?? '–'}</p>
                  </div>
                  <div className='p-4 rounded-lg bg-secondary/30 border border-border/50'>
                    <p className='text-sm text-muted-foreground mb-1'>Ideologi</p>
                    <p className='font-semibold'>{party.ideology ?? '–'}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </main>

      <Footer />
    </div>
  );
};

export default PoliticianDetail;
