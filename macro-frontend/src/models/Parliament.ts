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
  speechCount: number | null;
  motionCount: number | null;
  recentSpeeches: ParliamentDocument[];
  recentMotions: ParliamentDocument[];
}
