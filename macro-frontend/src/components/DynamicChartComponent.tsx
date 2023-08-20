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
import DataSeries from '@/models/DataSeries';
import { rootUrl } from '@/models/Constants';
import { DataResponseItem } from '@/models/DataResponseItem';
import { GraphSelectionItem } from '@/models/GraphSelectionItem';
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

type Action =
  | { type: 'LOAD_NEW'; updatedName: string; updatedSeries: DataSeries }
  | { type: 'RESET' };

const reducer = (state: Dataset, action: Action) => {
  switch (action.type) {
    case 'LOAD_NEW':
      // const key: string = action.updatedName;
      // return { ...state, [key]: action.updatedSeries };
      return state;
    case 'RESET':
      return state;
  }
};

// const emptyCache: DatasetCache = {
//   SwedenPolicyRate: { selected: false },
//   UsdSekExchangeRate: { selected: false },
// };

const emptyCache: Array<Dataset> = defaultDataset;

interface Props {
  selectedItems: GraphSelectionItem[];
}

const DynamicChartComponent = ({ selectedItems }: Props) => {
  // const [cache, cacheDispatch] = useReducer(reducer, emptyCache);
  const [cache, setCache] = useState(emptyCache);

  const deselect = (dataset: Dataset): Dataset => {
    return { ...dataset, selected: false };
  };

  const loadDataset = async (dataset: Dataset) => {
    const isSelected = selectedItems.map((i) => i.name).includes(dataset.name);

    console.log(dataset);
    console.log(isSelected);
    console.log('---');

    if (shouldFetchData(dataset, isSelected)) {
      console.log('fetching data for ' + dataset.name);

      const responseRaw = await ky.get(rootUrl + dataset.url);
      const responseData: DataResponseItem[] = await responseRaw.json();

      const values = responseData.map((r) => r.value);
      const labels = responseData.map((r) => r.date.join('-'));

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
          .map(async (dataset) => loadDataset(dataset))
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
        labels: [],
        datasets: cache
          .filter((dataset) => dataset.selected)
          .map((dataset) => {
            return {
              data: dataset.data,
            };
          }),
      }}
    />
  );
};

// {
//   label: 'Internationella statsobligationer 5-års löptid: Eur',
//     data: valuesEur,
//   borderColor: 'rgb(255, 99, 71)',
//   backgroundColor: 'rgba(255, 99, 71, 0.1)',
//   pointRadius: 1,
// },

export default DynamicChartComponent;
