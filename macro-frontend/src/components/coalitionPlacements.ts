import { PartySeats } from '@/models/Parliament';

export type BarId = 'a' | 'b' | 'outside';

export type Placements = Readonly<Record<BarId, readonly string[]>>;

export const BAR_IDS: readonly BarId[] = ['a', 'b', 'outside'];

export const NO_PLACEMENTS: Placements = { a: [], b: [], outside: [] };

export interface StackSegment {
  party: PartySeats;
  seatsBelow: number;
}

export const sumSeats = (parties: readonly PartySeats[]) => parties.reduce((sum, party) => sum + party.seats, 0);

export const majorityOf = (totalSeats: number) => Math.floor(totalSeats / 2) + 1;

export const hasMajority = (seats: number, totalSeats: number) => seats >= majorityOf(totalSeats);

export const moveParty = (placements: Placements, code: string, to: BarId): Placements => {
  const isPlaced = BAR_IDS.some(bar => placements[bar].includes(code));
  if (!isPlaced || placements[to].includes(code)) return placements;
  const without = (bar: BarId) => placements[bar].filter(placed => placed !== code);
  return {
    a: without('a'),
    b: without('b'),
    outside: without('outside'),
    [to]: [...without(to), code],
  };
};

// Answers the same object when nothing changed, so that a component can call this while rendering and only set state on a difference
export const reconcilePlacements = (placements: Placements, codes: readonly string[]): Placements => {
  const present = new Set(codes);
  const placed = new Set(BAR_IDS.flatMap(bar => placements[bar]));
  const unplaced = codes.filter(code => !placed.has(code));
  if (unplaced.length === 0 && [...placed].every(code => present.has(code))) return placements;
  const kept = (bar: BarId) => placements[bar].filter(code => present.has(code));
  return { a: kept('a'), b: kept('b'), outside: [...kept('outside'), ...unplaced] };
};

export const stackSegments = (codes: readonly string[], parties: readonly PartySeats[]): StackSegment[] => {
  const stacked = codes.flatMap(code => parties.filter(party => party.code === code));
  return stacked.map((party, index) => ({ party, seatsBelow: sumSeats(stacked.slice(0, index)) }));
};
