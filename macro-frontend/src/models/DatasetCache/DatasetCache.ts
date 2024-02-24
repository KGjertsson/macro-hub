import { Dataset, defaultDataset } from '@/models/Dataset';

export type DatasetCache = {
  labels: string[];
  chartData: Dataset[];
};

export const emptyCache: DatasetCache = {
  labels: [],
  chartData: defaultDataset,
};

export const deselect = (dataset: Dataset): Dataset => {
  return { ...dataset, selected: false };
};
