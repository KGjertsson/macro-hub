import { useQuery } from '@tanstack/react-query';
import { rootUrl, SAMPLE_STRATEGY, TIME_FRAME } from '@/models/Constants';
import { Dataset } from '@/models/Dataset';
import { SeriesConfig } from '@/models/SeriesConfig';

const baseUrl = `${rootUrl}/macro-analyzer`;

// Labels are [year, month, day] with day sampling, [year, month] with month sampling and [year] with year sampling
export type ChartDataResponse = {
  labels: number[][];
  chartData: Dataset[];
};

// The series aligned to one set of labels; null on 204, when none of them has data points in the time frame
export const fetchChartData = async (
  names: string[],
  strategy: SAMPLE_STRATEGY,
  timeFrame: TIME_FRAME,
): Promise<ChartDataResponse | null> => {
  const response = await fetch(`${baseUrl}/chart-data`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      strategy,
      timeFrame,
      chartSeriesParams: names.map(name => ({ name })),
    }),
  });
  if (response.status === 204) {
    return null;
  }
  if (!response.ok) {
    throw new Error(`POST /chart-data failed with ${response.status}`);
  }
  return response.json();
};

// Labels as UTC dates; a missing month or day is the first one
export const labelToDate = (label: number[]) => new Date(Date.UTC(label[0], (label[1] ?? 1) - 1, label[2] ?? 1));

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
