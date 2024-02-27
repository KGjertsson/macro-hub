import { Dataset } from '@/models/Dataset';
import {
  DATASET_NAMES,
  NO_FILTER,
  SAMPLE_STRATEGY,
  sampleToFilter,
} from '@/models/Constants';
import { DatasetWithLabels } from '@/models/DatasetCache/DatasetWithLabels';
import { unionLabels } from '@/models/DatasetCache/Labels';

export const sample = (
  labels: string[],
  datasets: Dataset[],
  selectedSample: SAMPLE_STRATEGY
): DatasetWithLabels => {
  const filter = sampleToFilter[selectedSample];

  if (filter === NO_FILTER) {
    console.log('No sampling needed with filter = ' + filter);

    return { labels, datasets: datasets };
  } else {
    const regexpFilter = filter as RegExp;

    return runSampling(datasets, regexpFilter);
  }
};

const runSampling = (
  datasets: Dataset[],
  filter: RegExp
): DatasetWithLabels => {
  console.log('Running sampling with filter = ' + filter);
  const sampledDatasets = datasets.map((d) => sampleDataset(d, filter));
  const unionOfLabels = unionLabels(sampledDatasets);
  const sampledDatasetsFull = sampledDatasets.map((dataset) =>
    extendDatasetToFullLabels(dataset, unionOfLabels)
  );

  return { labels: unionOfLabels, datasets: sampledDatasetsFull };
};

const sampleDataset = (dataset: Dataset, filter: RegExp): Dataset => {
  const labelsRaw = dataset.labels!;
  const dataRaw = dataset.values!;

  const sampledLabels: string[] = [];
  const sampledData: number[] = [];

  labelsRaw.forEach((label, index) => {
    const match = filter.exec(label);

    if (match) {
      const uniqueKey = match[0];
      if (!sampledLabels.includes(uniqueKey)) {
        sampledLabels.push(uniqueKey);
        sampledData.push(dataRaw[index]);
      }
    }
  });

  return { ...dataset, values: sampledData, labels: sampledLabels };
};

const extendDatasetToFullLabels = (
  dataset: Dataset,
  unionOfLabels: string[]
): Dataset => {
  {
    console.log('extending dataset: ' + DATASET_NAMES[dataset.name]);
    const datasetLabels = dataset.labels!;

    const firstLabel = new Date(datasetLabels[0]);
    let offset = -1;

    const extendedData = unionOfLabels.map((label, index) => {
      const currentLabel = new Date(label);

      if (currentLabel.getTime() === firstLabel.getTime()) {
        offset = index;
      }

      if (currentLabel.getTime() < firstLabel.getTime()) {
        return 0;
      } else {
        return dataset.values![index - offset];
      }
    });

    return { ...dataset, labels: unionOfLabels, values: extendedData };
  }
};
