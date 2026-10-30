import { useQuery } from '@tanstack/react-query';
import { SAMPLE_STRATEGY, TIME_FRAME } from '@/models/Constants';
import { fetchChartData, labelToDate } from '@/api/chartData';

// How often the source publishes, which decides how the observations are read out of /chart-data
export type TrendFrequency = 'fiscalYear' | 'month';

export type TrendDefinition = {
  name: string;
  frequency: TrendFrequency;
};

export type TrendPoint = {
  date: Date;
  value: number;
};

// The backend fills every day between two observations with the earlier one's value. Month sampling keeps the
// first day of each month, which for a series observed on the 1st is exactly its observations (an unchanged
// month is a real observation there). A fiscal-year series is observed once a year (FRED dates FYGDP on 30 sep),
// so month or year sampling would show the previous year's value on most labels; with day sampling its
// observations are the days where a new value starts. Two years in a row with the same value would merge into
// one point, which does not happen for GDP.
const sampling: Record<TrendFrequency, SAMPLE_STRATEGY> = {
  fiscalYear: SAMPLE_STRATEGY.Day,
  month: SAMPLE_STRATEGY.Month,
};

export const toTrendPoints = (
  labels: number[][],
  values: (number | null)[],
  frequency: TrendFrequency,
): TrendPoint[] =>
  values.flatMap((value, index) => {
    if (value === null) {
      return [];
    }
    if (frequency === 'fiscalYear' && index > 0 && values[index - 1] === value) {
      return [];
    }
    return [{ date: labelToDate(labels[index]), value }];
  });

// One query per series: the series differ in sampling, and aligning them in one call would pad each to the
// others' range
export const useTrendSeries = ({ name, frequency }: TrendDefinition) =>
  useQuery({
    queryKey: ['trendSeries', name],
    queryFn: async (): Promise<TrendPoint[]> => {
      const chartData = await fetchChartData([name], sampling[frequency], TIME_FRAME.TenYear);
      const series = chartData?.chartData.find(dataset => dataset.name === name);
      return chartData && series ? toTrendPoints(chartData.labels, series.values, frequency) : [];
    },
    // One retry, so the failure message shows within a couple of seconds when the backend is down
    retry: 1,
  });
