import { ChangeEvent, useEffect, useState } from 'react';
import DynamicChartComponent from '@/components/DynamicChartComponent';
import GraphConfigItem from '@/models/GraphConfigItem';

const graphs = [
  new GraphConfigItem('SwedenPolicyRate', '/policy-rate/sweden'),
  new GraphConfigItem('UsdSekExchangeRate', '/exchange-rate/usd-sek'),
];

const DynamicChartSettings = () => {
  const [configItems, setConfigItems] = useState<GraphConfigItem[]>([]);

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
    );

    const selectedGraphs = graphs.filter((g) =>
      selectedOptions.includes(g.name)
    );

    setConfigItems(selectedGraphs);
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
          {graphs.map((graph) => (
            <option key={graph.name} value={graph.name}>
              {graph.name}
            </option>
          ))}
        </select>
        <label data-te-select-label-ref>Example label</label>
      </div>
      <DynamicChartComponent selectedItems={configItems} />
    </div>
  );
};

export default DynamicChartSettings;
