import { useQuery } from '@tanstack/react-query';
import { rootUrl, SAMPLE_STRATEGY, TIME_FRAME } from '@/models/Constants';
import { Dataset } from '@/models/Dataset';
import { SeriesConfig } from '@/models/SeriesConfig';

const baseUrl = `${rootUrl}/macro-analyzer`;

// How often the source publishes; decides whether the period shows the day or only the month
export type Frequency = 'day' | 'month';

export type IndicatorDefinition = {
  name: string;
  title: string;
  frequency: Frequency;
};

// The start page's cards, by series name
export const START_PAGE_INDICATORS: IndicatorDefinition[] = [
  { name: 'PolicyRateSweden', title: 'Styrränta Sverige', frequency: 'day' },
  { name: 'FEDEffectiveFederalFundsRate', title: 'Styrränta USA', frequency: 'day' },
  { name: 'FEDCPI', title: 'KPI USA (median)', frequency: 'month' },
  { name: 'FEDUnemploymentRate', title: 'Arbetslöshet USA', frequency: 'month' },
];

export type LatestObservation = {
  value: number;
  date: Date;
  // The value before the latest change; equal to value when the series did not change within the time frame
  previous: number;
  // The first day with the current value; null when the series did not change within the time frame
  changedAt: Date | null;
};

type ChartDataResponse = {
  labels: number[][];
  chartData: Dataset[];
};

const fetchChartData = async (names: string[]): Promise<ChartDataResponse | null> => {
  const response = await fetch(`${baseUrl}/chart-data`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      strategy: SAMPLE_STRATEGY.Day,
      timeFrame: TIME_FRAME.OneYear,
      chartSeriesParams: names.map(name => ({ name })),
    }),
  });
  // 204 when none of the series has data points yet
  if (response.status === 204) {
    return null;
  }
  if (!response.ok) {
    throw new Error(`POST /chart-data failed with ${response.status}`);
  }
  return response.json();
};

// Day labels arrive as [year, month, day]
const toDate = (label: number[]) => new Date(Date.UTC(label[0], label[1] - 1, label[2] ?? 1));

// With day sampling the backend repeats a series' last value on every day up to its next observation and
// leaves the days after its latest observation null. The last non-null day is therefore the latest observation.
// A repeated value cannot be told apart from a new observation with the same value, so the change is taken
// against the last value that differs from the latest one, i.e. the change since the value last moved.
export const latestObservation = (labels: number[][], values: (number | null)[]): LatestObservation | null => {
  let last = values.length - 1;
  while (last >= 0 && values[last] === null) {
    last--;
  }
  if (last < 0) {
    return null;
  }
  const value = values[last] as number;
  let before = last - 1;
  while (before >= 0 && values[before] === value) {
    before--;
  }
  const previous = before >= 0 ? values[before] : null;
  return {
    value,
    date: toDate(labels[last]),
    previous: previous ?? value,
    changedAt: previous === null ? null : toDate(labels[before + 1]),
  };
};

export type IndicatorData = {
  configs: SeriesConfig[];
  observations: Record<string, LatestObservation | null>;
};

export const useStartPageIndicators = () =>
  useQuery({
    queryKey: ['startPageIndicators'],
    queryFn: async (): Promise<IndicatorData> => {
      const [configResponse, chartData] = await Promise.all([
        fetch(`${baseUrl}/series-config`),
        fetchChartData(START_PAGE_INDICATORS.map(indicator => indicator.name)),
      ]);
      // The titles fall back to the card's own, so the cards still work without the configs
      const configs: SeriesConfig[] = configResponse.ok && configResponse.status !== 204
        ? await configResponse.json()
        : [];
      const observations = Object.fromEntries(
        START_PAGE_INDICATORS.map(indicator => {
          const series = chartData?.chartData.find(dataset => dataset.name === indicator.name);
          return [
            indicator.name,
            series && chartData ? latestObservation(chartData.labels, series.values) : null,
          ];
        }),
      );
      return { configs, observations };
    },
  });
