import { useQuery } from '@tanstack/react-query';
import { rootUrl } from '@/models/Constants';
import { LocalElectionDetail, LocalElectionKind, LocalElectionSummary } from '@/models/LocalElection';

const baseUrl = `${rootUrl}/macro-analyzer`;

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

export const useLocalElections = (kind: LocalElectionKind) =>
  useQuery({
    queryKey: [kind, 'elections'],
    queryFn: () => getJson<LocalElectionSummary[]>(`/${kind}/elections`, []),
    staleTime: Infinity,
  });

export const useLocalElectionDetail = (kind: LocalElectionKind, code: string | null) =>
  useQuery({
    queryKey: [kind, 'detail', code],
    queryFn: () => getJson<LocalElectionDetail | null>(`/${kind}/${encodeURIComponent(code ?? '')}`, null),
    enabled: code !== null,
    staleTime: Infinity,
  });
