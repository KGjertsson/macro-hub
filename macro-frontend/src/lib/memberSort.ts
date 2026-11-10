import type { MemberSummary } from '@/models/Parliament';

export const MIN_VOTES_FOR_SHARE = 100;

interface SortMetric {
  value: (member: MemberSummary) => number | null;
  format: (value: number) => string;
  sampleSize?: (member: MemberSummary) => number;
}

export interface MemberSortOption {
  id: string;
  label: string;
  metric: SortMetric | null;
  note: string | null;
}

const countLabel = (count: number, one: string, many: string) =>
  `${count.toLocaleString('sv-SE')} ${count === 1 ? one : many}`;

const oneDecimal = (value: number) =>
  value.toLocaleString('sv-SE', { minimumFractionDigits: 1, maximumFractionDigits: 1 });

const castVotes = ({ votes }: MemberSummary) => (votes ? votes.yes + votes.no + votes.abstain : 0);

const totalVotes = (member: MemberSummary) => castVotes(member) + (member.votes?.absent ?? 0);

const percentOf = (part: number, whole: number) => (whole > 0 ? (part / whole) * 100 : null);

const WHOLE_CAREER_NOTE =
  'Räknat över hela tiden i riksdagen, inte bara den valda mandatperioden. ' +
  'Statistik finns bara för ledamöter från de två senaste mandatperioderna, övriga visas sist.';

const smallSampleNote = (sample: string) =>
  `${WHOLE_CAREER_NOTE} Ledamöter med färre än ${MIN_VOTES_FOR_SHARE} ${sample} visas efter dem som har fler.`;

export const memberSortOptions: readonly MemberSortOption[] = [
  { id: 'name', label: 'Namn', metric: null, note: null },
  {
    id: 'votes',
    label: 'Flest voteringar',
    metric: {
      value: member => (member.votes ? totalVotes(member) : null),
      format: value => countLabel(value, 'votering', 'voteringar'),
    },
    note: WHOLE_CAREER_NOTE,
  },
  {
    id: 'absence',
    label: 'Högst frånvaro',
    metric: {
      value: member => percentOf(member.votes?.absent ?? 0, totalVotes(member)),
      format: value => `${oneDecimal(value)} % frånvaro`,
      sampleSize: totalVotes,
    },
    note: smallSampleNote('voteringar'),
  },
  {
    id: 'speeches',
    label: 'Flest anföranden',
    metric: {
      value: member => member.speechCount,
      format: value => countLabel(value, 'anförande', 'anföranden'),
    },
    note: WHOLE_CAREER_NOTE,
  },
  {
    id: 'motions',
    label: 'Flest motioner',
    metric: {
      value: member => member.motionCount,
      format: value => countLabel(value, 'motion', 'motioner'),
    },
    note: WHOLE_CAREER_NOTE,
  },
  {
    id: 'years',
    label: 'Längst tid i riksdagen',
    metric: {
      value: member => member.yearsInParliament,
      format: value => `${oneDecimal(value)} år i riksdagen`,
    },
    note: null,
  },
  {
    id: 'noShare',
    label: 'Störst andel nej-röster',
    metric: {
      value: member => percentOf(member.votes?.no ?? 0, castVotes(member)),
      format: value => `${oneDecimal(value)} % nej-röster`,
      sampleSize: castVotes,
    },
    note: smallSampleNote('avgivna röster'),
  },
];

export const defaultMemberSortOption = memberSortOptions[0];

export const findMemberSortOption = (id: string) =>
  memberSortOptions.find(option => option.id === id) ?? defaultMemberSortOption;

const WITH_ENOUGH_DATA = 0;
const SMALL_SAMPLE = 1;
const NO_DATA = 2;

const dataTier = (metric: SortMetric, member: MemberSummary, value: number | null) => {
  if (value === null) return NO_DATA;
  return metric.sampleSize && metric.sampleSize(member) < MIN_VOTES_FOR_SHARE ? SMALL_SAMPLE : WITH_ENOUGH_DATA;
};

// The API lists members by surname and Array.sort is stable, so equal values stay in name order
export const sortMembers = (members: MemberSummary[], { metric }: MemberSortOption): MemberSummary[] => {
  if (!metric) return members;

  return members
    .map(member => {
      const value = metric.value(member);
      return { member, value, tier: dataTier(metric, member, value) };
    })
    .sort((a, b) => a.tier - b.tier || (b.value ?? 0) - (a.value ?? 0))
    .map(({ member }) => member);
};

export const formatMemberSortValue = (member: MemberSummary, { metric }: MemberSortOption): string | undefined => {
  const value = metric?.value(member) ?? null;
  return metric && value !== null ? metric.format(value) : undefined;
};
