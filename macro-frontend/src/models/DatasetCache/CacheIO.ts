import { Dataset } from '@/models/Dataset';
import { DATASET_NAMES, rootUrl } from '@/models/Constants';
import { shouldFetchData } from '@/utils/ObjectUtils';
import ky from 'ky';
import { DataResponseItem } from '@/models/DataResponseItem';

export const loadDataset = async (
  dataset: Dataset,
  selectedItems: DATASET_NAMES[]
) => {
  const isSelected = selectedItems.includes(dataset.name);
  const shouldFetch = shouldFetchData(dataset, isSelected);
  if (shouldFetch) {
    return fetchData(dataset);
  } else {
    if (isSelected) {
      console.log(
        'already cached selected series: ' + DATASET_NAMES[dataset.name]
      );
      return { ...dataset, selected: true };
    }

    return dataset;
  }
};

const fetchData = async (dataset: Dataset) => {
  console.log('fetching data for ' + DATASET_NAMES[dataset.name]);
  const responseRaw = await ky.get(rootUrl + dataset.url);
  const responseData: DataResponseItem[] = await responseRaw.json();
  const values = responseData.map((r) => r.value);
  const labels = responseData.map((r) => r.date.join('-'));

  return { ...dataset, data: values, labels: labels, selected: true };
};
