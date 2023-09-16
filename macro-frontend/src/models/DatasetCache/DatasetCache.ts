import { Dataset, defaultDataset } from '@/models/Dataset';

export type DatasetCache = {
  datasets: Dataset[];

  labels: string[];
};

export const emptyCache: DatasetCache = {
  labels: [],
  datasets: defaultDataset,
};

export const deselect = (dataset: Dataset): Dataset => {
  return { ...dataset, selected: false };
};
