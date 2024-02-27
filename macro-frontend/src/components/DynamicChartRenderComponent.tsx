import React from 'react';
import { DATASET_NAMES, rootUrl, SAMPLE_STRATEGY } from '@/models/Constants';
import DynamicChartComponent from '@/components/DynamicChartComponent';
import { Dataset } from '@/models/Dataset';
import { useQuery } from '@tanstack/react-query';

interface Props {
  selectedItems: Dataset[];
  sampleStrategy: SAMPLE_STRATEGY;
}

const DynamicChartRenderComponent = ({
  selectedItems,
  sampleStrategy,
}: Props) => {
  const body = {
    strategy: sampleStrategy,
    chartSeriesParams: selectedItems.map((d) => {
      return {
        name: DATASET_NAMES[d.name],
        country: '',
        period: '',
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
    queryKey: ['repoData'],
    queryFn: fetchChartData,
    enabled: selectedItems.length > 0,
  });

  if (isPending) return 'Loading...';

  if (error) return 'An error has occurred: ' + error.message;

  const formattedData = {
    ...data,
    labels: data.labels.map((label: String) => label[0] + '-' + label[1]),
  };
  console.log(formattedData);

  return <DynamicChartComponent sampled={formattedData} />;
};

export default DynamicChartRenderComponent;
