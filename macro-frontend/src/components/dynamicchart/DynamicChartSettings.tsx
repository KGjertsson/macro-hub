import { ChangeEvent, useEffect, useState } from 'react';
import { rootUrl, SAMPLE_STRATEGY, sampleStrategies } from '@/models/Constants';
import DynamicChartRenderComponent from '@/components/dynamicchart/DynamicChartRenderComponent';
import { SeriesConfig } from '@/models/SeriesConfig';
import { useQuery } from '@tanstack/react-query';

type SampleStrategyDisplay = {
  Day: string;
  Month: string;
  Year: string;
};

const sampleStrategyDisplay: SampleStrategyDisplay = {
  Day: 'Dag',
  Month: 'Månad',
  Year: 'År',
};

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

  const dataSelectionComponent = () => {
    return (
      <div>
        <select data-te-select-init multiple onChange={filterSelectedGraphs}>
          {allSeriesConfigs
            .map((config) => config.name)
            .map((name) => (
              <option key={name} value={name}>
                {name}
              </option>
            ))}
        </select>
        <label data-te-select-label-ref>Välj datatyp</label>
      </div>
    );
  };

  const strategySelectionComponent = () => {
    return (
      <div style={{ marginTop: '10px' }}>
        <select
          data-te-select-init
          onChange={setSampleFromEvent}
          defaultValue={SAMPLE_STRATEGY[SAMPLE_STRATEGY.Month]}
        >
          {sampleStrategies.map((sample) => (
            <option key={sample} value={sample}>
              {sampleStrategyDisplay[sample as keyof SampleStrategyDisplay]}
            </option>
          ))}
        </select>
        <label data-te-select-label-ref>Välj upplösning</label>
      </div>
    );
  };

  return (
    <div>
      <div className="relative" data-te-dropdown-ref>
        <label
          htmlFor="countries_multiple"
          className="block mb-2 text-sm font-medium text-gray-900 dark:text-white"
        >
          Välj datatyp och upplösning för dynamisk rendering
        </label>
      </div>
      {dataSelectionComponent()}
      {strategySelectionComponent()}
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
