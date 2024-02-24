import React, { useReducer } from 'react';
import { DATASET_NAMES, rootUrl, SAMPLE_STRATEGY } from '@/models/Constants';
import DynamicChartComponent from '@/components/DynamicChartComponent';
import { DatasetCache } from '@/models/DatasetCache/DatasetCache';
import { Dataset } from '@/models/Dataset';
import { useQuery } from '@tanstack/react-query';

interface Props {
  selectedItems: Dataset[];
  sampleStrategy: SAMPLE_STRATEGY;
}

enum CacheActionTypes {
  SET_LABELS,
  SET_CACHE,
}

type CacheAction = {
  type: CacheActionTypes;
  payload: DatasetCache;
};

const alignedBundleReducer = (
  state: DatasetCache,
  action: CacheAction
): DatasetCache => {
  switch (action.type) {
    case CacheActionTypes.SET_LABELS:
      return { ...state, labels: action.payload.labels };
    case CacheActionTypes.SET_CACHE:
      return {
        labels: action.payload.labels,
        datasets: action.payload.datasets,
      };
    default:
      throw new Error(
        'Unexpected action type in alignedBundleReducer: ' + action.type
      );
  }
};

const defaultSampled: DatasetCache = { labels: [], datasets: [] };

const DynamicChartRenderComponent = ({
  selectedItems,
  sampleStrategy,
}: Props) => {
  const [alignedBundle, alignedBundleDispatch] = useReducer(
    alignedBundleReducer,
    defaultSampled
  );

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
  console.log('body');
  console.log(body);
  console.log('---');

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

  console.log(isPending);
  console.log(error);
  console.log(data);

  if (isPending) return 'Loading...';

  if (error) return 'An error has occurred: ' + error.message;

  return <DynamicChartComponent sampled={data} />;

  // useEffect(() => {
  //   const init = async () => {
  //     const updatedCache = await updateCache(selectedItems);
  //     const newSelection = updatedCache.filter((d) => d.selected);
  //
  //     const generatedLabels = generateLabels(newSelection);
  //
  //     const sampled = sample(generatedLabels, newSelection, sampleStrategy);
  //     alignedBundleDispatch({
  //       type: CacheActionTypes.SET_CACHE,
  //       payload: sampled,
  //     });
  //   };
  //
  //   init().then(() => console.log('init DynamicDataCache'));
  // }, [selectedItems, sampleStrategy]);
};

export default DynamicChartRenderComponent;
