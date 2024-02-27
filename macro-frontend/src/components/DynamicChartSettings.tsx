import { ChangeEvent, useEffect, useState } from 'react';
import {
  allDatasetNames,
  DATASET_NAMES,
  SAMPLE_STRATEGY,
  sampleStrategies,
} from '@/models/Constants';
import DynamicChartRenderComponent from '@/components/DynamicChartRenderComponent';
import { defaultDataset } from '@/models/Dataset';

const DynamicChartSettings = () => {
  const [selectedItems, setSelectedItems] = useState<DATASET_NAMES[]>([]);
  const [sample, setSample] = useState<SAMPLE_STRATEGY>(SAMPLE_STRATEGY.Month);

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
      (selected) => SAMPLE_STRATEGY[selected as keyof typeof SAMPLE_STRATEGY]
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
        <select
          data-te-select-init
          onChange={setSampleFromEvent}
          defaultValue={SAMPLE_STRATEGY[SAMPLE_STRATEGY.Month]}
        >
          {sampleStrategies.map((sample) => (
            <option key={sample} value={sample}>
              {sample}
            </option>
          ))}
        </select>
        <label data-te-select-label-ref>Super bra label</label>
      </div>
      <DynamicChartRenderComponent
        selectedItems={defaultDataset.filter((d) =>
          selectedItems.includes(d.name)
        )}
        sampleStrategy={sample}
      />
    </div>
  );
};

export default DynamicChartSettings;
