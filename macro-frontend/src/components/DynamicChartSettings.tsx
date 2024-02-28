import { ChangeEvent, useEffect, useState } from 'react';
import { rootUrl, SAMPLE_STRATEGY, sampleStrategies } from '@/models/Constants';
import DynamicChartRenderComponent from '@/components/DynamicChartRenderComponent';
import { SeriesConfig } from '@/models/SeriesConfig';
import { useQuery } from '@tanstack/react-query';

const DynamicChartSettings = () => {
  const [selectedItems, setSelectedItems] = useState<string[]>([]);
  const [sample, setSample] = useState<SAMPLE_STRATEGY>(SAMPLE_STRATEGY.Month);
  const [allSeriesConfigs, setAllSeriesConfigs] = useState<SeriesConfig[]>([]);

  const fetchSeriesConfig = () =>
    fetch(rootUrl + '/macro-analyzer/series-config', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    }).then((res) => res.json());

  const { isPending, error, data } = useQuery({
    queryKey: ['seriesConfig'],
    queryFn: fetchSeriesConfig,
  });

  useEffect(() => {
    if (!isPending && !error) {
      setAllSeriesConfigs(data);
    }
  }, [isPending, error, data]);

  useEffect(() => {
    const init = async () => {
      const { Dropdown, Ripple, Select, initTE } = await import('tw-elements');
      initTE({ Select, Dropdown, Ripple });
    };

    init();
  }, []);

  const filterSelectedGraphs = (e: ChangeEvent<HTMLSelectElement>) => {
    const selectedOptions = parseOptionFromElement(e);

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
          {allSeriesConfigs
            .map((config) => config.name)
            .map((name) => (
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
        selectedItems={allSeriesConfigs.filter((d) =>
          selectedItems.includes(d.name)
        )}
        sampleStrategy={sample}
      />
    </div>
  );
};

export default DynamicChartSettings;
