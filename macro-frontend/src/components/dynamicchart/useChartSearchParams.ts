import { useNavigate, useSearchParams } from 'react-router-dom';
import { SAMPLE_STRATEGY, TIME_FRAME } from '@/models/Constants';

export const DEFAULT_SERIES = ['PolicyRateSweden', 'FEDEffectiveFederalFundsRate'];
const DEFAULT_TIME_FRAME = TIME_FRAME.TenYear;
const DEFAULT_SAMPLE_STRATEGY = SAMPLE_STRATEGY.Month;

export type ChartState = {
  // Series names in the order they were added; unknown names are filtered out by the caller
  series: string[];
  timeFrame: TIME_FRAME;
  sampleStrategy: SAMPLE_STRATEGY;
  indexed: boolean;
};

// Accepts only the enum's key names ("TenYear"), not its numeric reverse mappings
const parseEnumKey = <E extends Record<string, string | number>>(enumObject: E, key: string | null) =>
  key !== null && typeof enumObject[key] === 'number' ? (enumObject[key] as E[keyof E]) : undefined;

const parseSeries = (raw: string | null) =>
  raw === null ? DEFAULT_SERIES : Array.from(new Set(raw.split(',').filter(Boolean)));

// Chart selection lives in the query (?s=A,B&t=TenYear&r=Month&idx=1) so views can be shared and reloaded
export const useChartSearchParams = (): [ChartState, (change: Partial<ChartState>) => void] => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const state: ChartState = {
    series: parseSeries(searchParams.get('s')),
    timeFrame: parseEnumKey(TIME_FRAME, searchParams.get('t')) ?? DEFAULT_TIME_FRAME,
    sampleStrategy: parseEnumKey(SAMPLE_STRATEGY, searchParams.get('r')) ?? DEFAULT_SAMPLE_STRATEGY,
    indexed: searchParams.get('idx') === '1'
  };

  const update = (change: Partial<ChartState>) => {
    const next = { ...state, ...change };
    const params = new URLSearchParams(searchParams);
    params.set('s', next.series.join(','));
    params.set('t', TIME_FRAME[next.timeFrame]);
    params.set('r', SAMPLE_STRATEGY[next.sampleStrategy]);
    if (next.indexed) {
      params.set('idx', '1');
    } else {
      params.delete('idx');
    }
    // Commas are legal in a query; keep them unescaped so the URL stays readable
    navigate({ search: params.toString().replace(/%2C/g, ',') }, { replace: true });
  };

  return [state, update];
};
