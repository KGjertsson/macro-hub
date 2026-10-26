export interface PartySeats {
  code: string;
  name: string;
  color: string;
  seats: number;
}

export interface ElectionResult {
  year: number;
  termStart: string;
  termEnd: string;
  parties: PartySeats[];
}

export interface Composition {
  asOf: string;
  parties: PartySeats[];
}

export interface Party {
  code: string;
  name: string;
  color: string;
  ideology: string | null;
  active: boolean;
}

export interface MemberSummary {
  id: string;
  name: string;
  party: string;
  constituency: string;
  status: string;
  imageUrl: string | null;
  currentRole: string;
  yearsInParliament: number;
  attendancePct: number | null;
}

export interface Assignment {
  type: string;
  organCode: string;
  organName: string;
  role: string;
  status: string | null;
  from: string | null;
  to: string | null;
  current: boolean;
}

export interface ParliamentDocument {
  title: string;
  date: string;
  url: string;
}

// Votes cast during one riksmöte, e.g. "2025/26"
export interface SessionVotes {
  riksmote: string;
  yes: number;
  no: number;
  absent: number;
  abstain: number;
}

// Shared with the voting summary bar so each outcome keeps its colour across the page.
// Validated on the dark card surface for colour-blind separation and contrast.
export const voteOutcomes = [
  { key: 'yes', label: 'Ja', color: '#22a35a' },
  { key: 'no', label: 'Nej', color: '#c81e3a' },
  { key: 'abstain', label: 'Avstår', color: '#d97706' },
  { key: 'absent', label: 'Frånvarande', color: '#6b80c4' },
] as const;

export interface MemberDetail {
  summary: MemberSummary;
  imageUrlLarge: string | null;
  birthYear: number | null;
  gender: string | null;
  residence: string | null;
  runningNextElection: boolean | null;
  contact: { email: string | null; phone: string | null; website: string | null };
  assignments: Assignment[];
  biography: { label: string; text: string }[];
  votes: { yes: number; no: number; absent: number; abstain: number } | null;
  votesBySession: SessionVotes[];
  speechCount: number | null;
  motionCount: number | null;
  recentSpeeches: ParliamentDocument[];
  recentMotions: ParliamentDocument[];
}
