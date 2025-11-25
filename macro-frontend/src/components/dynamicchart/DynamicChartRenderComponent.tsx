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

const DynamicChartRenderComponent = ({
  selectedItems,
  sampleStrategy,
  timeFrame
}: Props) => {
  const findDisplayName = (name: String) =>
    selectedItems.filter((item) => item.name === name)[0].displayName;

  const formatDisplayName = (raw: Dataset[]): Dataset[] => {
    return raw.map((d) => {
      const displayName = findDisplayName(d.name);
      return {
        ...d,
        name: displayName,
      };
    });
  };

  const queryKey =
    sampleStrategy +
    '@' +
    timeFrame +
    '@' +
    selectedItems.map((i: SeriesConfig) => i.name).join(',');

  const body = {
    strategy: sampleStrategy,
    timeFrame: timeFrame,
    chartSeriesParams: selectedItems.map((config) => {
      return {
        name: config.name,
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

  if (isPending) return <div />;

  if (error) return 'An error has occurred: ' + error.message;

  const labels =
    sampleStrategy === SAMPLE_STRATEGY.Month
      ? data.labels.map((label: String) => label[0] + '-' + label[1])
      : data.labels;

  const formattedData = {
    chartData: formatDisplayName(data.chartData),
    labels: labels,
  };

  return <DynamicChartComponent sampled={formattedData} />;
};

export default DynamicChartRenderComponent;
