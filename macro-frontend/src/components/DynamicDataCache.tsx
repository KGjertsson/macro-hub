import React, { useEffect, useReducer } from 'react';
import { DATASET_NAMES, SAMPLE_STRATEGY } from '@/models/Constants';
import DynamicChartComponent from '@/components/DynamicChartComponent';
import {
  DatasetWithLabels,
  deselect,
  emptyCache,
} from '@/models/DatasetCache/DatasetWithLabels';
import { sample } from '@/models/DatasetCache/Sampling';
import { loadDataset } from '@/models/DatasetCache/CacheIO';
import { generateLabels } from '@/models/DatasetCache/Labels';
import { Dataset } from '@/models/Dataset';

interface Props {
  selectedItemNames: DATASET_NAMES[];
  selectedSample: SAMPLE_STRATEGY;
}

enum CacheActionTypes {
  SET_LABELS,
  SET_CACHE,
}

type CacheAction = {
  type: CacheActionTypes;
  payload: DatasetWithLabels;
};

const cacheReducer = (
  state: DatasetWithLabels,
  action: CacheAction
): DatasetWithLabels => {
  switch (action.type) {
    case CacheActionTypes.SET_LABELS:
      return { ...state, labels: action.payload.labels };
    case CacheActionTypes.SET_CACHE:
      return {
        labels: action.payload.labels,
        chartData: action.payload.chartData,
      };
    default:
      throw new Error('Unexpected action type in cacheReducer: ' + action.type);
  }
};

const defaultSampled: DatasetWithLabels = { labels: [], chartData: [] };

const DynamicDataCache = ({ selectedItemNames, selectedSample }: Props) => {
  const [cache, cacheDispatch] = useReducer(cacheReducer, emptyCache);
  const [sampled, sampledDispatch] = useReducer(cacheReducer, defaultSampled);

  useEffect(() => {
    const init = async () => {
      const updatedCache = await updateCache(selectedItemNames);
      const newSelection = updatedCache.filter((d) => d.selected);

      const generatedLabels = generateLabels(newSelection);
      cacheDispatch({
        type: CacheActionTypes.SET_CACHE,
        payload: { labels: generatedLabels, chartData: updatedCache },
      });

      const sampled = sample(generatedLabels, newSelection, selectedSample);
      sampledDispatch({
        type: CacheActionTypes.SET_CACHE,
        payload: sampled,
      });
    };

    init().then(() => console.log('init DynamicDataCache'));
  }, [selectedItemNames, selectedSample]);

  const updateCache = async (
    selectedItemNames: DATASET_NAMES[]
  ): Promise<Dataset[]> => {
    return Promise.all(
      cache.chartData
        .map((dataset) => deselect(dataset))
        .map(async (dataset) => loadDataset(dataset, selectedItemNames))
    );
  };

  return <DynamicChartComponent sampled={sampled} />;
};

export default DynamicDataCache;
