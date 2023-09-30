import { Dataset } from '@/models/Dataset';
import { NO_FILTER, SAMPLE_SIZE, sampleToFilter } from '@/models/Constants';
import { DatasetCache } from '@/models/DatasetCache/DatasetCache';
import { unionLabels } from '@/models/DatasetCache/Labels';

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
  const unionOfLabels = unionLabels(sampledDatasets);
  console.log(unionOfLabels);
  const sampledDatasetsFull = sampledDatasets.map((d) => {
    const firstLabel = Date.parse(d.labels![0]);
    const lastLabel = Date.parse(d.labels![d.labels!.length - 1]);
    let offset = -1;

    const extendedData = unionOfLabels.map((label, index) => {
      const currentLabel = Date.parse(label);

      if (currentLabel === firstLabel) {
        offset = index;
      }

      if (currentLabel < firstLabel || currentLabel > lastLabel) {
        return 0;
      } else {
        return d.data![index - offset];
      }
    });

    return { ...d, labels: unionOfLabels, data: extendedData };
  });

  return { labels: unionOfLabels, datasets: sampledDatasetsFull };
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
