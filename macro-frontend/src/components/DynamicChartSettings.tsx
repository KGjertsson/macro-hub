import { ChangeEvent, useEffect, useState } from 'react';
import DynamicDataCache from '@/components/DynamicDataCache';
import {
  allDatasetNames,
  allSamples,
  DATASET_NAMES,
  SAMPLE_SIZE,
} from '@/models/Constants';

const DynamicChartSettings = () => {
  const [selectedItems, setSelectedItems] = useState<DATASET_NAMES[]>([]);
  const [sample, setSample] = useState<SAMPLE_SIZE>(SAMPLE_SIZE.Day);

  useEffect(() => {
    const init = async () => {
      const { Dropdown, Ripple, Select, initTE } = await import('tw-elements');
      initTE({ Select, Dropdown, Ripple });
    };

    init();
  }, []);

  const filterSelectedGraphs = (e: ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = parseOptionFromElement(e).map(
      (selected) => DATASET_NAMES[selected as keyof typeof DATASET_NAMES]
    );

    setSelectedItems(selectedOptions);
  };

  const setSampleFromEvent = (e: ChangeEvent<HTMLSelectElement>) => {
    const selectedOption = parseOptionFromElement(e).map(
      (selected) => SAMPLE_SIZE[selected as keyof typeof SAMPLE_SIZE]
    )[0];

    setSample(selectedOption);
  };

  const parseOptionFromElement = (
    e: ChangeEvent<HTMLSelectElement>
  ): string[] => {
    return Array.from(e.target.selectedOptions, (option) => option.value);
  };

  return (
    <div>
      <div className="relative" data-te-dropdown-ref>
        <label
          htmlFor="countries_multiple"
          className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
        >
          Select an option
        </label>
        <select data-te-select-init multiple onChange={filterSelectedGraphs}>
          {allDatasetNames.map((name) => (
            <option key={name} value={name}>
              {name}
            </option>
          ))}
        </select>
        <select data-te-select-init onChange={setSampleFromEvent}>
          {allSamples.map((sample) => (
            <option key={sample} value={sample}>
              {sample}
            </option>
          ))}
        </select>
        <label data-te-select-label-ref>Super bra label</label>
      </div>
      <DynamicDataCache
        selectedItemNames={selectedItems}
        selectedSample={sample}
      />
    </div>
  );
};

export default DynamicChartSettings;
