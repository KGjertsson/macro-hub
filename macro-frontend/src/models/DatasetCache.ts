import { Dataset, defaultDataset } from '@/models/Dataset';
import {
  DATASET_NAMES,
  NO_FILTER,
  rootUrl,
  SAMPLE_SIZE,
  sampleToFilter,
} from '@/models/Constants';
import { shouldFetchData } from '@/utils/ObjectUtils';
import ky from 'ky';
import { DataResponseItem } from '@/models/DataResponseItem';

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

export const sample = (
  labels: string[],
  datasets: Dataset[],
  selectedSample: SAMPLE_SIZE
): DatasetCache => {
  const filter = sampleToFilter[selectedSample];
  console.log('sampling with filter: ' + filter);
  console.log(labels);
  console.log(datasets);
  console.log(selectedSample);

  if (filter === NO_FILTER) {
    console.log('nothing to sample');
    return { labels, datasets: datasets };
  } else {
    console.log('running sample');
    const regexpFilter = filter as RegExp;
    return runSampling(labels, datasets, regexpFilter);
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

const runSampling = (
  labels: string[],
  datasets: Dataset[],
  filter: RegExp
): DatasetCache => {
  const sampledLabels = labels.reduce((result: string[], current: string) => {
    const match = filter.exec(current);

    if (match) {
      const groupElement = match[0];
      if (!result.includes(groupElement)) {
        result.push(groupElement);
      }
    } else {
      console.log('No match found');
    }

    return result;
  }, []);
  const sampledDatasets = datasets.map((d) => sampleDataset(d, filter));
  
  return { labels: sampledLabels, datasets: sampledDatasets };
};

const sampleDataset = (dataset: Dataset, filter: RegExp): Dataset => {
  const labelsRaw = dataset.labels!;
  const dataRaw = dataset.data!;

  const sampledLabels: string[] = [];
  const sampledData: number[] = [];

  labelsRaw.forEach((label, index) => {
    const match = filter.exec(label);

    if (match) {
      const uniqueKey = match[0];
      if (!sampledLabels.includes(uniqueKey)) {
        sampledLabels.push(labelsRaw[index]);
        sampledData.push(dataRaw[index]);
      }
    }
  });

  return { ...dataset, data: sampledData, labels: sampledLabels };
};
