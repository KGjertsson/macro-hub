import React, { useEffect, useState } from 'react';
import { rootUrl, SAMPLE_STRATEGY, sampleStrategies, TIME_FRAME, timeWindows } from '@/models/Constants';
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
  Year: 'År'
};

type TimeFrameDisplay = {
  All: string,
  OneYear: string,
  FiveYear: string,
  TenYear: string,
  OneMonth: string
};

const timeFrameDisplay: TimeFrameDisplay = {
  All: 'Allt',
  OneYear: 'Ett år',
  FiveYear: 'Fem år',
  TenYear: 'Tio år',
  OneMonth: 'En månad'
};

const DynamicChartSettings = () => {
  const [selectedItems, setSelectedItems] = useState<string[]>([]);
  const [sample, setSample] = useState<SAMPLE_STRATEGY>(SAMPLE_STRATEGY.Month);
  const [timeFrame, setTimeFrame] = useState<TIME_FRAME>(TIME_FRAME.OneYear);
  const [allSeriesConfigs, setAllSeriesConfigs] = useState<SeriesConfig[]>([]);

  const fetchSeriesConfig = () =>
    fetch(rootUrl + '/macro-analyzer/series-config', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then((res) => res.json());

  const { isPending, error, data } = useQuery({
    queryKey: ['seriesConfig'],
    queryFn: fetchSeriesConfig
  });

  useEffect(() => {
    if (!isPending && !error) {
      console.log(data);
      setAllSeriesConfigs(data);
    }
  }, [isPending, error, data]);

  // No UI lib init needed; we use Tailwind/native elements

  const filterSelectedGraphs = (
    event: React.ChangeEvent<HTMLSelectElement>
  ) => {
    const options = Array.from(event.target.selectedOptions).map((o) => o.value);
    setSelectedItems(options);
  };

  const setSampleFromEvent = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOption = SAMPLE_STRATEGY[e.target.value as keyof typeof SAMPLE_STRATEGY];
    setSample(selectedOption);
  };

  const setTimeWindowFromEvent = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOption = TIME_FRAME[e.target.value as keyof typeof TIME_FRAME];
    setTimeFrame(selectedOption);
  };

  const renderOptGroupByCategory = (category: string) => {
    return (
      <optgroup key={`grp-${category}`} label={category}>
        {allSeriesConfigs
          .filter((config) => config.category === category)
          .map((config) => (
            <option key={config.name} value={config.displayName}>
              {config.displayName}
            </option>
          ))}
      </optgroup>
    );
  };

  const dataSelectionComponent = () => {
    const categories = Array.from(new Set(allSeriesConfigs.map((c) => c.category)));
    return (
      <div className='w-full'>
        <label htmlFor='data-selection-component' className='block text-sm font-medium mb-1'>Datatyp</label>
        <select
          multiple
          id='data-selection-component'
          value={selectedItems}
          onChange={filterSelectedGraphs}
          className='block w-full rounded-md border border-gray-300 bg-white p-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 min-h-32'
        >
          {categories.map((category) => renderOptGroupByCategory(category))}
        </select>
        <p className='text-xs text-gray-500 mt-1'>Håll Ctrl/Cmd för att välja flera.</p>
      </div>
    );
  };

  const strategySelectionComponent = () => {
    return (
      <div className='w-full'>
        <label htmlFor='resolution-select' className='block text-sm font-medium mb-1'>Upplösning</label>
        <select
          id='resolution-select'
          value={SAMPLE_STRATEGY[sample]}
          onChange={setSampleFromEvent}
          className='block w-full rounded-md border border-gray-300 bg-white p-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500'
        >
          {sampleStrategies.map((s) => (
            <option key={s} value={s}>
              {sampleStrategyDisplay[s as keyof SampleStrategyDisplay]}
            </option>
          ))}
        </select>
      </div>
    );
  };

  const timeFrameSelectionComponent = () => {
    return (
      <div className='w-full'>
        <label htmlFor='timeframe-select' className='block text-sm font-medium mb-1'>Tidsram</label>
        <select
          id='timeframe-select'
          value={TIME_FRAME[timeFrame]}
          onChange={setTimeWindowFromEvent}
          className='block w-full rounded-md border border-gray-300 bg-white p-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500'
        >
          {timeWindows.map((timeWindow) => (
            <option key={timeWindow} value={timeWindow}>
              {timeFrameDisplay[timeWindow as keyof TimeFrameDisplay]}
            </option>
          ))}
        </select>
      </div>
    );
  };

  return (
    <div>
      <p className='mb-2 text-sm text-gray-700'>Välj datatyp och upplösning för dynamisk rendering</p>
      {dataSelectionComponent()}
      <div className='my-2' />
      {strategySelectionComponent()}
      <div className='my-2' />
      {timeFrameSelectionComponent()}
      <DynamicChartRenderComponent
        selectedItems={allSeriesConfigs.filter((d) =>
          selectedItems.includes(d.displayName)
        )}
        sampleStrategy={sample}
        timeFrame={timeFrame}
      />
    </div>
  );
};

export default DynamicChartSettings;
