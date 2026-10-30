import { useQuery } from '@tanstack/react-query';
import { rootUrl, SAMPLE_STRATEGY, TIME_FRAME } from '@/models/Constants';
import { Dataset } from '@/models/Dataset';
import { SeriesConfig } from '@/models/SeriesConfig';

const baseUrl = `${rootUrl}/macro-analyzer`;

// How often the source publishes. Decides whether the period shows the day or only the month, and what the
// change is measured against: a daily rate against the value before it last moved, a monthly series against
// the previous month's observation (an unchanged month is a real observation there)
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
  // The value the change is measured against; null when the time frame does not reach back to it
  previous: number | null;
  // Daily series: the first day with the current value; null when it did not change within the time frame
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

// The same day one month earlier, clamped to the end of a shorter month (31 mar -> 28 feb)
const oneMonthBefore = (date: Date) => {
  const year = date.getUTCFullYear();
  const month = date.getUTCMonth() - 1;
  const lastDay = new Date(Date.UTC(year, month + 1, 0)).getUTCDate();
  return new Date(Date.UTC(year, month, Math.min(date.getUTCDate(), lastDay)));
};

// With day sampling the backend repeats a series' last value on every day up to its next observation and
// leaves the days after its latest observation null. The last non-null day is therefore the latest observation.
export const latestObservation = (
  labels: number[][],
  values: (number | null)[],
  frequency: Frequency,
): LatestObservation | null => {
  let last = values.length - 1;
  while (last >= 0 && values[last] === null) {
    last--;
  }
  if (last < 0) {
    return null;
  }
  const value = values[last] as number;
  const date = toDate(labels[last]);

  if (frequency === 'month') {
    // The day one month before carries the previous month's observation, repeated or on that very day
    const target = oneMonthBefore(date).getTime();
    const index = labels.findIndex(label => toDate(label).getTime() === target);
    return { value, date, previous: index >= 0 ? values[index] : null, changedAt: null };
  }

  // A repeated day cannot be told apart from a new observation with the same value, so a daily rate is
  // compared with the last value that differs from the latest one, i.e. the change since it last moved
  let before = last - 1;
  while (before >= 0 && values[before] === value) {
    before--;
  }
  const previous = before >= 0 ? values[before] : null;
  return {
    value,
    date,
    previous: previous ?? value,
    changedAt: previous === null ? null : toDate(labels[before + 1]),
  };
};

// Shares its key with the chart picker's series-config query, so the page fetches the configs once
export const useSeriesConfigs = () =>
  useQuery({
    queryKey: ['seriesConfig'],
    queryFn: async (): Promise<SeriesConfig[]> => {
      const response = await fetch(`${baseUrl}/series-config`);
      if (response.status === 204) {
        return [];
      }
      if (!response.ok) {
        throw new Error(`servern svarade med status ${response.status}`);
      }
      return response.json();
    },
  });

export const useStartPageIndicators = () =>
  useQuery({
    queryKey: ['startPageIndicators'],
    queryFn: async (): Promise<Record<string, LatestObservation | null>> => {
      const chartData = await fetchChartData(START_PAGE_INDICATORS.map(indicator => indicator.name));
      return Object.fromEntries(
        START_PAGE_INDICATORS.map(indicator => {
          const series = chartData?.chartData.find(dataset => dataset.name === indicator.name);
          return [
            indicator.name,
            series && chartData ? latestObservation(chartData.labels, series.values, indicator.frequency) : null,
          ];
        }),
      );
    },
    // One retry, so the failure message shows within a couple of seconds when the backend is down
    retry: 1,
  });
