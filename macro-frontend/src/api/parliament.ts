import { useQuery } from '@tanstack/react-query';
import { rootUrl } from '@/models/Constants';
import { Composition, ElectionResult, MemberDetail, MemberSummary, Party } from '@/models/Parliament';

const baseUrl = `${rootUrl}/macro-analyzer/parliament`;

// The backend answers 204 when nothing has been scraped yet
const getJson = async <T>(path: string, empty: T): Promise<T> => {
  const response = await fetch(baseUrl + path);
  if (response.status === 204) {
    return empty;
  }
  if (!response.ok) {
    throw new Error(`GET ${path} failed with ${response.status}`);
  }
  return response.json();
};

export const useElections = () =>
  useQuery({
    queryKey: ['parliament', 'elections'],
    queryFn: () => getJson<ElectionResult[]>('/elections', []),
  });

export const useComposition = () =>
  useQuery({
    queryKey: ['parliament', 'composition'],
    queryFn: () => getJson<Composition | null>('/composition', null),
  });

export const useParties = () =>
  useQuery({
    queryKey: ['parliament', 'parties'],
    queryFn: () => getJson<Party[]>('/parties', []),
    staleTime: Infinity,
  });

export const useParliamentMembers = (year: number | undefined) =>
  useQuery({
    queryKey: ['parliament', 'members', year],
    queryFn: () => getJson<MemberSummary[]>(`/members?year=${year}`, []),
    enabled: year !== undefined,
  });

export const useMember = (id: string | undefined) =>
  useQuery({
    queryKey: ['parliament', 'member', id],
    queryFn: () => getJson<MemberDetail | null>(`/members/${encodeURIComponent(id ?? '')}`, null),
    enabled: !!id,
  });

export const partyColor = (parties: Party[] | undefined, code: string) =>
  parties?.find(p => p.code === code)?.color ?? '#9CA3AF';
