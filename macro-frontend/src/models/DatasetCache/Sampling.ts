import { Dataset } from '@/models/Dataset';
import { NO_FILTER, SAMPLE_SIZE, sampleToFilter } from '@/models/Constants';
import { DatasetCache } from '@/models/DatasetCache/DatasetCache';

export const sample = (
  labels: string[],
  datasets: Dataset[],
  selectedSample: SAMPLE_SIZE
): DatasetCache => {
  const filter = sampleToFilter[selectedSample];

  if (filter === NO_FILTER) {
    console.log('No sampling needed with filter = ' + filter);

    return { labels, datasets: datasets };
  } else {
    const regexpFilter = filter as RegExp;

    return runSampling(labels, datasets, regexpFilter);
  }
};

const runSampling = (
  labels: string[],
  datasets: Dataset[],
  filter: RegExp
): DatasetCache => {
  console.log('Running sampling with filter = ' + filter);
  const sampledDatasets = datasets.map((d) => sampleDataset(d, filter));
  const sampledLabels = extractLatestLabels(sampledDatasets);

  return { labels: sampledLabels, datasets: sampledDatasets };
};

const extractLatestLabels = (datasets: Dataset[]): string[] => {
  return datasets.reduce((result: string[], current: Dataset) => {
    if (result.length === 0) {
      return current.labels!;
    }
    const resultEnd = Date.parse(result[result.length - 1]);
    const currentEnd = Date.parse(current.labels![current.labels!.length - 1]);

    return currentEnd > resultEnd ? current.labels! : result;
  }, []);
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
        sampledLabels.push(uniqueKey);
        sampledData.push(dataRaw[index]);
      }
    }
  });

  return { ...dataset, data: sampledData, labels: sampledLabels };
};
