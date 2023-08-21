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

  const loadDataset = async (
    dataset: Dataset,
    selectedItems: DATASET_NAMES[]
  ) => {
    const isSelected = selectedItems.includes(dataset.name);

    if (shouldFetchData(dataset, isSelected)) {
      console.log('fetching data for ' + dataset.name);

      const responseRaw = await ky.get(rootUrl + dataset.url);
      const responseData: DataResponseItem[] = await responseRaw.json();

      const values = responseData.map((r) => r.value);
      const labels = responseData.map((r) => r.date.join('-'));
      setLabels(labels);

      return { ...dataset, data: values, selected: true };
    } else {
      if (isSelected) {
        console.log('already cached selected series: ' + dataset.name);
      }

      return { ...dataset, selected: true };
    }
  };

  useEffect(() => {
    const init = async () => {
      const updatedCache = await Promise.all(
        cache
          .map((dataset) => deselect(dataset))
          .map(async (dataset) => loadDataset(dataset, selectedItems))
      );
      const newSelection = updatedCache.filter((d) => d.selected);

      setCache(updatedCache);
      setSelected(newSelection);
    };

    init();
  }, [selectedItems]);

  return <DynamicChartComponent labels={labels} selected={selected} />;
};

export default DynamicDataCache;
