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

export const zip = <A, B>(listA: A[], listB: B[]): (A | B)[][] => {
  return listA.map((item, index) => [item, listB[index]]);
};
