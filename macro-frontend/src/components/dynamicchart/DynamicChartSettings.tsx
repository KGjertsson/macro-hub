import React, { useState } from 'react';
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

  const fetchSeriesConfig = async (): Promise<SeriesConfig[]> => {
    const res = await fetch(rootUrl + '/macro-analyzer/series-config', {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (res.status === 204) {
      return [];
    }
    if (!res.ok) {
      throw new Error(`servern svarade med status ${res.status}`);
    }
    return res.json();
  };

  const { error, data } = useQuery({
    queryKey: ['seriesConfig'],
    queryFn: fetchSeriesConfig
  });

  const allSeriesConfigs = data ?? [];

  const setSampleFromEvent = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOption = SAMPLE_STRATEGY[e.target.value as keyof typeof SAMPLE_STRATEGY];
    setSample(selectedOption);
  };

  const setTimeWindowFromEvent = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedOption = TIME_FRAME[e.target.value as keyof typeof TIME_FRAME];
    setTimeFrame(selectedOption);
  };

  const toggleDataSelection = (displayName: string) => {
    setSelectedItems((prev) =>
      prev.includes(displayName)
        ? prev.filter((item) => item !== displayName)
        : [...prev, displayName]
    );
  };

  const dataSelectionComponent = () => {
    const categories = Array.from(new Set(allSeriesConfigs.map((c) => c.category)));
    return (
      <div className='w-full'>
        <label className='block text-sm font-medium mb-2'>Datatyp</label>
        <div className='border border-border rounded-md p-3 max-h-64 overflow-y-auto bg-card'>
          {categories.map((category) => (
            <div key={category} className='mb-3 last:mb-0'>
              <p className='text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2'>{category}</p>
              <div className='flex flex-wrap gap-2'>
                {allSeriesConfigs
                  .filter((config) => config.category === category)
                  .map((config) => {
                    const isSelected = selectedItems.includes(config.displayName);
                    return (
                      <button
                        type='button'
                        key={config.name}
                        onClick={() => toggleDataSelection(config.displayName)}
                        className={`px-3 py-1.5 text-sm rounded-md border transition-colors ${
                          isSelected
                            ? 'bg-primary text-primary-foreground border-primary'
                            : 'bg-background border-border hover:bg-muted'
                        }`}
                      >
                        {config.displayName}
                      </button>
                    );
                  })}
              </div>
            </div>
          ))}
        </div>
        <p className='text-xs text-muted-foreground mt-1'>Klicka för att välja/avvälja datatyper.</p>
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
          className='block w-full rounded-md border border-border bg-card text-foreground p-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary'
        >
          {sampleStrategies.map((s) => (
            <option key={s} value={s} className='bg-card text-foreground'>
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
          className='block w-full rounded-md border border-border bg-card text-foreground p-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary focus:border-primary'
        >
          {timeWindows.map((timeWindow) => (
            <option key={timeWindow} value={timeWindow} className='bg-card text-foreground'>
              {timeFrameDisplay[timeWindow as keyof TimeFrameDisplay]}
            </option>
          ))}
        </select>
      </div>
    );
  };

  return (
    <div>
      <p className='mb-2 text-sm text-muted-foreground'>Välj datatyp och upplösning för dynamisk rendering</p>
      {error && <p className='mb-2 text-sm text-destructive'>Kunde inte hämta datatyper: {error.message}</p>}
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
