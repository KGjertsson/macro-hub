import { Dataset, defaultDataset } from '@/models/Dataset';

export type DatasetWithLabels = {
  labels: string[];
  chartData: Dataset[];
};

export const emptyCache: DatasetWithLabels = {
  labels: [],
  chartData: defaultDataset,
};

export const deselect = (dataset: Dataset): Dataset => {
  return { ...dataset, selected: false };
};
