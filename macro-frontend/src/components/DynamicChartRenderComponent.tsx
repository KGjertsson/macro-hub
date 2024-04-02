import React from 'react';
import { rootUrl, SAMPLE_STRATEGY } from '@/models/Constants';
import DynamicChartComponent from '@/components/DynamicChartComponent';
import { useQuery } from '@tanstack/react-query';
import { SeriesConfig } from '@/models/SeriesConfig';

interface Props {
  selectedItems: SeriesConfig[];
  sampleStrategy: SAMPLE_STRATEGY;
}

const DynamicChartRenderComponent = ({
  selectedItems,
  sampleStrategy,
}: Props) => {
  const queryKey =
    sampleStrategy +
    '@' +
    selectedItems.map((i: SeriesConfig) => i.name).join(',');

  const body = {
    strategy: sampleStrategy,
    chartSeriesParams: selectedItems.map((config) => {
      return {
        name: config.name,
        country: config.country,
        period: config.period,
      };
    }),
  };

  const fetchChartData = () =>
    fetch(rootUrl + '/macro-analyzer/chart-data', {
      method: 'POST',
      body: JSON.stringify(body),
      headers: {
        'Content-Type': 'application/json',
      },
    }).then((res) => res.json());

  const { isPending, error, data } = useQuery({
    queryKey: [queryKey],
    queryFn: fetchChartData,
    enabled: selectedItems.length > 0,
  });

  if (isPending) return 'Loading...';

  if (error) return 'An error has occurred: ' + error.message;

  const labels =
    sampleStrategy === SAMPLE_STRATEGY.Month
      ? data.labels.map((label: String) => label[0] + '-' + label[1])
      : data.labels;
  const formattedData = {
    ...data,
    labels: labels,
  };
  console.log(formattedData);

  return <DynamicChartComponent sampled={formattedData} />;
};

export default DynamicChartRenderComponent;
