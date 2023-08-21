import { ChangeEvent, useEffect, useState } from 'react';
import DynamicDataCache from '@/components/DynamicDataCache';
import { allDatasetNames, DATASET_NAMES } from '@/models/Constants';

const DynamicChartSettings = () => {
  const [selectedItems, setSelectedItems] = useState<DATASET_NAMES[]>([]);

  useEffect(() => {
    const init = async () => {
      const { Dropdown, Ripple, Select, initTE } = await import('tw-elements');
      initTE({ Select, Dropdown, Ripple });
    };

    init();
  }, []);

  const filterSelectedGraphs = (e: ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = Array.from(
      e.target.selectedOptions,
      (option) => option.value
    ).map((selected) => DATASET_NAMES[selected as keyof typeof DATASET_NAMES]);

    setSelectedItems(selectedOptions);
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
        <label data-te-select-label-ref>Example label</label>
      </div>
      <DynamicDataCache selectedItems={selectedItems} />
    </div>
  );
};

export default DynamicChartSettings;
