import React from 'react';
import { rootUrl, SAMPLE_STRATEGY, TIME_FRAME } from '@/models/Constants';
import DynamicChartComponent from '@/components/dynamicchart/DynamicChartComponent';
import { useQuery } from '@tanstack/react-query';
import { SeriesConfig } from '@/models/SeriesConfig';
import { Dataset } from '@/models/Dataset';

interface Props {
  selectedItems: SeriesConfig[];
  sampleStrategy: SAMPLE_STRATEGY;
  timeFrame: TIME_FRAME;
}

type ChartLabel = number | string | number[];

type ChartDataResponse = {
  labels: ChartLabel[];
  chartData: Dataset[];
};

// Dates arrive as arrays ([2024, 6] for months, [2024, 6, 1] for days) or as a plain year
const formatLabel = (label: ChartLabel): string =>
  Array.isArray(label)
    ? label.map((part, index) => (index === 0 ? String(part) : String(part).padStart(2, '0'))).join('-')
    : String(label);

const DynamicChartRenderComponent = ({
  selectedItems,
  sampleStrategy,
  timeFrame
}: Props) => {
  const findDisplayName = (name: string) =>
    selectedItems.find((item) => item.name === name)?.displayName ?? name;

  const formatDisplayName = (raw: Dataset[]): Dataset[] => {
    return raw.map((d) => ({
      ...d,
      name: findDisplayName(d.name),
    }));
  };

  const body = {
    strategy: sampleStrategy,
    timeFrame: timeFrame,
    chartSeriesParams: selectedItems.map((config) => {
      return {
        name: config.name,
      };
    }),
  };

  // The backend answers 204 when the selected series have no data points
  const fetchChartData = async (): Promise<ChartDataResponse | null> => {
    const res = await fetch(rootUrl + '/macro-analyzer/chart-data', {
      method: 'POST',
      body: JSON.stringify(body),
      headers: {
        'Content-Type': 'application/json',
      },
    });
    if (res.status === 204) {
      return null;
    }
    if (!res.ok) {
      throw new Error(`servern svarade med status ${res.status}`);
    }
    return res.json();
  };

  const { isPending, error, data } = useQuery({
    queryKey: ['chartData', sampleStrategy, timeFrame, selectedItems.map((i) => i.name)],
    queryFn: fetchChartData,
    enabled: selectedItems.length > 0,
  });

  if (selectedItems.length === 0) {
    return <p className='mt-6 text-sm text-muted-foreground'>Välj minst en datatyp för att visa en graf.</p>;
  }

  if (isPending) return <div />;

  if (error) return <p className='mt-6 text-sm text-destructive'>Ett fel uppstod: {error.message}</p>;

  if (!data) {
    return <p className='mt-6 text-sm text-muted-foreground'>Ingen data finns för de valda datatyperna.</p>;
  }

  const formattedData = {
    chartData: formatDisplayName(data.chartData),
    labels: data.labels.map(formatLabel),
  };

  return <DynamicChartComponent sampled={formattedData} />;
};

export default DynamicChartRenderComponent;
