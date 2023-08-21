import { Dataset } from '@/models/Dataset';

export const isEmpty = (dataset: Dataset) => {
  return dataset.data === null || dataset.data === undefined;
};

export const shouldFetchData = (
  dataset: Dataset,
  selected: boolean
): boolean => {
  return isEmpty(dataset) && selected;
};
