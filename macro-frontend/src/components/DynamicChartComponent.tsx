import React, { useEffect, useState } from 'react';

import {
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Title,
  Tooltip,
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import ky from 'ky';
import { DATASET_NAMES, rootUrl } from '@/models/Constants';
import { DataResponseItem } from '@/models/DataResponseItem';
import { shouldFetchData } from '@/utils/ObjectUtils';
import { Dataset, defaultDataset } from '@/models/Dataset';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const emptyCache: Array<Dataset> = defaultDataset;

interface Props {
  selectedItems: DATASET_NAMES[];
}

const DynamicChartComponent = ({ selectedItems }: Props) => {
  const [cache, setCache] = useState(emptyCache);
  const [labels, setLabels] = useState<string[]>([]);

  const deselect = (dataset: Dataset): Dataset => {
    return { ...dataset, selected: false };
  };

  const loadDataset = async (
    dataset: Dataset,
    selectedItems: DATASET_NAMES[]
  ) => {
    const isSelected = selectedItems.includes(dataset.name);

    if (shouldFetchData(dataset, isSelected)) {
      console.log('fetching data for ' + dataset.name);

      const responseRaw = await ky.get(rootUrl + dataset.url);
      const responseData: DataResponseItem[] = await responseRaw.json();

      const values = responseData.map((r) => r.value);
      const labels = responseData.map((r) => r.date.join('-'));
      setLabels(labels);

      return { ...dataset, data: values, selected: true };
    } else {
      if (isSelected) {
        console.log('already cached selected series: ' + dataset.name);
      }

      return dataset;
    }
  };

  useEffect(() => {
    const init = async () => {
      const updatedCache = await Promise.all(
        cache
          .map((dataset) => deselect(dataset))
          .map(async (dataset) => loadDataset(dataset, selectedItems))
      );

      setCache(updatedCache);
    };

    init();
  }, [selectedItems]);

  return (
    <Line
      className="mx-auto w-3/5 overflow-hidden"
      style={{ width: '1000px', height: '1000px' }}
      options={{
        responsive: true,
        plugins: {
          legend: {
            position: 'top',
          },
          title: {
            display: true,
            text: 'super bra titel',
          },
        },
      }}
      data={{
        labels: labels,
        datasets: cache
          .filter((dataset) => dataset.selected)
          .map((dataset) => {
            return {
              data: dataset.data,
              label: dataset.lineConfig.label,
              borderColor: dataset.lineConfig.borderColor,
              backgroundColor: dataset.lineConfig.backgroundColor,
              pointRadius: dataset.lineConfig.pointRadius,
            };
          }),
      }}
    />
  );
};

export default DynamicChartComponent;
