import { useEffect, useState } from 'react';

import DynamicChartComponent from '@/components/DynamicChartComponent';

const graphs = {
  SwedenPolicyRate: 'SwedenPolicyRate',
  UsdSekExchangeRate: 'UsdSekExchangeRate',
};

const graphItems = Object.values(graphs).map((graph) => {
  return {
    value: graph,
    key: graph,
    text: graph,
  };
});

const DynamicChartSettings = () => {
  const [graph, setGraph] = useState([]);

  useEffect(() => {
    const init = async () => {
      const { Dropdown, Ripple, Select, initTE } = await import('tw-elements');
      initTE({ Select, Dropdown, Ripple });
    };

    init();
  }, []);

  return (
    <div>
      <div className="relative" data-te-dropdown-ref>
        <label
          htmlFor="countries_multiple"
          className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
        >
          Select an option
        </label>
        <select
          data-te-select-init
          multiple
          onChange={(e) => {
            const selectedGraphs = Array.from(
              e.target.selectedOptions,
              (option) => option.value,
            );
            setGraph(selectedGraphs);
          }}
        >
          {graphItems.map((graphItem) => (
            <option key={graphItem.key} value={graphItem.value}>
              {graphItem.text}
            </option>
          ))}
        </select>
        <label data-te-select-label-ref>Example label</label>
      </div>
      <DynamicChartComponent />
    </div>
  );
};

export default DynamicChartSettings;
