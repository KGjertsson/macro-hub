import React from 'react';
import { rootUrl } from '@/models/Constants';
import DynamicChartRenderComponent from '@/components/dynamicchart/DynamicChartRenderComponent';
import ChartControls from '@/components/dynamicchart/ChartControls';
import SeriesChips from '@/components/dynamicchart/SeriesChips';
import SeriesPicker from '@/components/dynamicchart/SeriesPicker';
import { useChartSearchParams } from '@/components/dynamicchart/useChartSearchParams';
import { useSeriesStyles } from '@/components/dynamicchart/useSeriesStyles';
import { SeriesConfig } from '@/models/SeriesConfig';
import { useQuery } from '@tanstack/react-query';

const DynamicChartSettings = () => {
  const [state, update] = useChartSearchParams();

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

  // In the order they were added; names the backend doesn't know are ignored
  const selectedItems = state.series.flatMap((name) => allSeriesConfigs.filter((config) => config.name === name));
  const selectedNames = selectedItems.map((config) => config.name);
  const styles = useSeriesStyles(selectedNames);

  const toggleSeries = (name: string) => {
    update({
      series: selectedNames.includes(name)
        ? selectedNames.filter((selected) => selected !== name)
        : [...selectedNames, name]
    });
  };

  return (
    <div className='flex flex-col gap-4'>
      {error && <p className='text-sm text-destructive'>Kunde inte hämta serier: {error.message}</p>}
      <div className='flex flex-wrap items-center gap-2'>
        <SeriesPicker configs={allSeriesConfigs} selectedNames={selectedNames} onToggle={toggleSeries} />
        <SeriesChips selected={selectedItems} styles={styles} onRemove={toggleSeries} />
      </div>
      <ChartControls
        timeFrame={state.timeFrame}
        sampleStrategy={state.sampleStrategy}
        indexed={state.indexed}
        onTimeFrameChange={(timeFrame) => update({ timeFrame })}
        onSampleStrategyChange={(sampleStrategy) => update({ sampleStrategy })}
        onIndexedChange={(indexed) => update({ indexed })}
      />
      {data && (
        <DynamicChartRenderComponent
          selectedItems={selectedItems}
          styles={styles}
          sampleStrategy={state.sampleStrategy}
          timeFrame={state.timeFrame}
          indexed={state.indexed}
        />
      )}
    </div>
  );
};

export default DynamicChartSettings;
