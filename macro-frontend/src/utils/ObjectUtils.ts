import { Dataset } from '@/models/Dataset';

export const isEmpty = (dataset: Dataset) => {
  console.log('isEmpty');
  const foo = dataset.data === null || dataset.data === undefined;
  console.log('isEmpty is: ' + foo);
  return dataset.data === null || dataset.data === undefined;
};

export const shouldFetchData = (
  dataset: Dataset,
  selected: boolean
): boolean => {
  console.log('shouldFetchData');
  return isEmpty(dataset) && selected;
};
