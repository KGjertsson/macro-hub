import React, { useEffect, useReducer } from 'react';
import { DATASET_NAMES, SAMPLE_SIZE } from '@/models/Constants';
import DynamicChartComponent from '@/components/DynamicChartComponent';
import {
  DatasetCache,
  deselect,
  emptyCache,
} from '@/models/DatasetCache/DatasetCache';
import { sample } from '@/models/DatasetCache/Sampling';
import { loadDataset } from '@/models/DatasetCache/CacheIO';
import { generateLabels } from '@/models/DatasetCache/Labels';

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

  useEffect(() => {
    const init = async () => {
      const updatedCache = await Promise.all(
        cache.datasets
          .map((dataset) => deselect(dataset))
          .map(async (dataset) => loadDataset(dataset, selectedItemNames))
      );
      const newSelection = updatedCache.filter((d) => d.selected);

      const generatedLabels = generateLabels(newSelection);
      cacheDispatch({
        type: CacheActionTypes.SET_CACHE,
        payload: { labels: generatedLabels, datasets: updatedCache },
      });

      const sampled = sample(generatedLabels, newSelection, selectedSample);
      sampledDispatch({
        type: CacheActionTypes.SET_CACHE,
        payload: sampled,
      });
    };

    init().then(() => console.log('init DynamicDataCache'));
  }, [selectedItemNames, selectedSample]);

  // useEffect(() => {
  //
  // }, [selectedSample])

  return <DynamicChartComponent sampled={sampled} />;
};

export default DynamicDataCache;
