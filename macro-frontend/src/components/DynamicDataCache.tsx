import React, { useEffect, useReducer } from 'react';
import { DATASET_NAMES, SAMPLE_SIZE } from '@/models/Constants';
import { Dataset } from '@/models/Dataset';
import DynamicChartComponent from '@/components/DynamicChartComponent';
import {
  DatasetCache,
  deselect,
  emptyCache,
  loadDataset,
  sample,
} from '@/models/DatasetCache';

const emptySelected: Dataset[] = [];

interface Props {
  selectedItemNames: DATASET_NAMES[];
  selectedSample: SAMPLE_SIZE;
}

enum CacheActionTypes {
  SET_LABELS,
  SET_CACHE,
}

type CacheAction = {
  type: CacheActionTypes;
  payload: DatasetCache;
};

const cacheReducer = (
  state: DatasetCache,
  action: CacheAction
): DatasetCache => {
  console.log('executing cacheReducer with:');
  console.log(state);
  console.log(action);
  switch (action.type) {
    case CacheActionTypes.SET_LABELS:
      return { ...state, labels: action.payload.labels };
    case CacheActionTypes.SET_CACHE:
      return {
        labels: action.payload.labels,
        datasets: action.payload.datasets,
      };
    default:
      throw new Error('Unexpected action type in cacheReducer: ' + action.type);
  }
};

const defaultSampled: DatasetCache = { labels: [], datasets: [] };

const DynamicDataCache = ({ selectedItemNames, selectedSample }: Props) => {
  const [cache, cacheDispatch] = useReducer(cacheReducer, emptyCache);
  const [sampled, sampledDispatch] = useReducer(cacheReducer, defaultSampled);

  // const [selected, setSelected] = useState(emptySelected);
  // const [labels, setLabels] = useState<string[]>([]);
  // const [sampledBundle, setSampledBundle] = useState<Bundle>();

  useEffect(() => {
    const init = async () => {
      const updatedCache = await Promise.all(
        cache.datasets
          .map((dataset) => deselect(dataset))
          .map(async (dataset) => loadDataset(dataset, selectedItemNames))
      );
      const newSelection = updatedCache.filter((d) => d.selected);

      let maxDate = Date.parse('1900');
      let maxIndex = -1;

      newSelection.forEach((dataset, index) => {
        const latestLabelString = dataset.labels![dataset.labels!.length - 1];
        const latestLabelDate = Date.parse(latestLabelString);

        if (latestLabelDate > maxDate) {
          maxDate = latestLabelDate;
          maxIndex = index;
        }
      });

      if (maxIndex !== -1) {
        const latestLabels = newSelection[maxIndex].labels!;
        cacheDispatch({
          type: CacheActionTypes.SET_CACHE,
          payload: { labels: latestLabels, datasets: updatedCache },
        });

        const sampled = sample(latestLabels, newSelection, selectedSample);
        sampledDispatch({
          type: CacheActionTypes.SET_CACHE,
          payload: sampled,
        });
      }
    };

    init().then(() => console.log('init DynamicDataCache'));
  }, [selectedItemNames, selectedSample]);

  // useEffect(() => {
  //
  // }, [selectedSample])

  return <DynamicChartComponent sampled={sampled} />;
};

export default DynamicDataCache;
