export type LocalElectionKind = 'municipalities' | 'regions' | 'constituencies';

export interface LocalPartyResult {
  code: string;
  abbreviation: string;
  name: string;
  color: string;
  votes: number;
  voteShare: number;
  seats: number;
}

export interface LocalElectionSummary {
  year: number;
  code: string;
  name: string;
  lanCode: string;
  totalSeats: number;
  turnout: number | null;
  largestParty: string | null;
  largestPartyColor: string;
  parties: LocalPartyResult[];
}

export interface ElectedMember {
  name: string;
  order: number;
  electedOn: string | null;
}

export interface ElectedParty {
  partyCode: string;
  abbreviation: string;
  name: string;
  color: string;
  members: ElectedMember[];
}

export interface Governance {
  coalitionName: string;
  description: string | null;
  governingParties: string[];
  supportParties: string[];
}

export interface LocalElectionDetail {
  summary: LocalElectionSummary;
  elected: ElectedParty[];
  governance: Governance | null;
}
