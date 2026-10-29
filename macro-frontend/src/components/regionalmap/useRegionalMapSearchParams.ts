import { useNavigate, useSearchParams } from 'react-router-dom';
import { AREA_LEVELS, AreaLevel } from '@/models/RegionalStatistics';

export type RegionalMapState = {
  level: AreaLevel;
  // Null until the user picks one; the view then falls back to the first indicator
  indicator: string | null;
  // Null means the latest year with data
  year: number | null;
  area: string | null;
};

const parseLevel = (raw: string | null): AreaLevel =>
  AREA_LEVELS.find(level => level === raw) ?? 'DeSO';

const parseYear = (raw: string | null) => {
  const year = raw === null ? NaN : Number(raw);
  return Number.isInteger(year) ? year : null;
};

const setOrDelete = (params: URLSearchParams, key: string, value: string | null) => {
  if (value === null) {
    params.delete(key);
  } else {
    params.set(key, value);
  }
};

// Map selection lives in the query (?l=DeSO&i=population&y=2024&a=0180C1010) so views can be shared and reloaded
export const useRegionalMapSearchParams = (): [RegionalMapState, (change: Partial<RegionalMapState>) => void] => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const state: RegionalMapState = {
    level: parseLevel(searchParams.get('l')),
    indicator: searchParams.get('i') || null,
    year: parseYear(searchParams.get('y')),
    area: searchParams.get('a') || null,
  };

  const update = (change: Partial<RegionalMapState>) => {
    const next = { ...state, ...change };
    const params = new URLSearchParams(searchParams);
    params.set('l', next.level);
    setOrDelete(params, 'i', next.indicator);
    setOrDelete(params, 'y', next.year === null ? null : String(next.year));
    setOrDelete(params, 'a', next.area);
    navigate({ search: params.toString() }, { replace: true });
  };

  return [state, update];
};
