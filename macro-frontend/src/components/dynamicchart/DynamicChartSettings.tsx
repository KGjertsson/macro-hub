import React, { useEffect, useState } from 'react';
import { rootUrl, SAMPLE_STRATEGY, sampleStrategies } from '@/models/Constants';
import DynamicChartRenderComponent from '@/components/dynamicchart/DynamicChartRenderComponent';
import { SeriesConfig } from '@/models/SeriesConfig';
import { useQuery } from '@tanstack/react-query';

import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import { Select, SelectChangeEvent } from '@mui/material';
import Divider from '@mui/material/Divider';
import Typography from '@mui/material/Typography';

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

  const filterSelectedGraphs = (
    event: SelectChangeEvent<typeof selectedItems>
  ) => {
    const value = event.target.value;
    setSelectedItems(typeof value === 'string' ? value.split(',') : value);
  };

  const setSampleFromEvent = (e: SelectChangeEvent) => {
    const selectedOption =
      SAMPLE_STRATEGY[e.target.value as keyof typeof SAMPLE_STRATEGY];

    setSample(selectedOption);
  };

  const dataSelectionComponent = () => {
    return (
      <FormControl fullWidth>
        <InputLabel id="demo-simple-select-label">Datatyp</InputLabel>
        <Select
          multiple
          labelId="data-selection-component"
          id="data-selection-component"
          label="Upplösning"
          value={selectedItems}
          onChange={filterSelectedGraphs}
        >
          <Typography>
            <b>Riksbanken</b>
          </Typography>
          {allSeriesConfigs
            .map((config) => config.displayName)
            .map((name) => (
              <MenuItem key={name} value={name}>
                {name}
              </MenuItem>
            ))}
          <Divider />
          <Typography>
            <b>Statistiska centralbyrån</b>
          </Typography>
        </Select>
      </FormControl>
    );
  };

  const strategySelectionComponent = () => {
    return (
      <FormControl fullWidth>
        <InputLabel id="demo-simple-select-label">Upplösning</InputLabel>
        <Select
          labelId="demo-simple-select-label"
          id="demo-simple-select"
          value={SAMPLE_STRATEGY[sample]}
          label="Upplösning"
          onChange={setSampleFromEvent}
        >
          {sampleStrategies.map((sample) => (
            <MenuItem key={sample} value={sample}>
              {sampleStrategyDisplay[sample as keyof SampleStrategyDisplay]}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
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
      <div style={{ margin: '10px' }} />
      {strategySelectionComponent()}
      <DynamicChartRenderComponent
        selectedItems={allSeriesConfigs.filter((d) =>
          selectedItems.includes(d.displayName)
        )}
        sampleStrategy={sample}
      />
    </div>
  );
};

export default DynamicChartSettings;
