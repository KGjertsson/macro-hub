import React, { useEffect, useState } from 'react';
import ky from 'ky';
import { DATASET_NAMES, rootUrl } from '@/models/Constants';
import { DataResponseItem } from '@/models/DataResponseItem';
import { shouldFetchData } from '@/utils/ObjectUtils';
import { Dataset, defaultDataset } from '@/models/Dataset';
import DynamicChartComponent from '@/components/DynamicChartComponent';

const emptyCache: Dataset[] = defaultDataset;
const emptySelected: Dataset[] = [];

interface Props {
  selectedItems: DATASET_NAMES[];
}

const DynamicDataCache = ({ selectedItems }: Props) => {
  const [cache, setCache] = useState(emptyCache);
  const [selected, setSelected] = useState(emptySelected);
  const [labels, setLabels] = useState<string[]>([]);

  const deselect = (dataset: Dataset): Dataset => {
    return { ...dataset, selected: false };
  };

  const selectDataset = async (
    dataset: Dataset,
    selectedItems: DATASET_NAMES[]
  ) => {
    const isSelected = selectedItems.includes(dataset.name);
    if (shouldFetchData(dataset, isSelected)) {
      console.log('fetching data for ' + DATASET_NAMES[dataset.name]);

      const responseRaw = await ky.get(rootUrl + dataset.url);
      const responseData: DataResponseItem[] = await responseRaw.json();

      const values = responseData.map((r) => r.value);
      const labels = responseData.map((r) => r.date.join('-'));
      setLabels(labels);

      return { ...dataset, data: values, labels: labels, selected: true };
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

  useEffect(() => {
    const init = async () => {
      const updatedCache = await Promise.all(
        cache
          .map((dataset) => deselect(dataset))
          .map(async (dataset) => selectDataset(dataset, selectedItems))
      );
      const newSelection = updatedCache.filter((d) => d.selected);

      let maxDate = Date.parse('1900');
      let maxIndex = -1;

      newSelection.forEach((dataset, index) => {
        const latestLabelString = dataset.labels![dataset.labels!.length - 1];
        const latestLabelDate = Date.parse(latestLabelString);

        if (latestLabelDate > maxDate) {
          maxDate = latestLabelDate;
          maxIndex = index;
        }
      });

      if (maxIndex !== -1) {
        const latestLabels = newSelection[maxIndex].labels!;
        setLabels(latestLabels);
      }

      setCache(updatedCache);
      setSelected(newSelection);
    };

    init();
  }, [selectedItems]);

  return <DynamicChartComponent labels={labels} selected={selected} />;
};

export default DynamicDataCache;
