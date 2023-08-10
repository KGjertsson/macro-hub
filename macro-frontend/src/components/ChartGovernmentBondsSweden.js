'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';
import MultiLineChartJsComponent from '@/components/MultiLineChartJsComponent';

const ChartGovernmentBondsSweden = () => {
  const [values2Month, setValues2Month] = useState([]);
  const [values5Month, setValues5Month] = useState([]);
  const [values7Month, setValues7Month] = useState([]);
  const [values10Month, setValues10Month] = useState([]);

  const [labels2Month, setLabels2Month] = useState([]);

  useEffect(() => {
    const init = async () => {
      const url2M = 'http://localhost:8080/government-bonds/sweden?period=2';
      const url5M = 'http://localhost:8080/government-bonds/sweden?period=5';
      const url7M = 'http://localhost:8080/government-bonds/sweden?period=7';
      const url10M = 'http://localhost:8080/government-bonds/sweden?period=10';

      const response2Month = await ky.get(url2M);
      const response5Month = await ky.get(url5M);
      const response7Month = await ky.get(url7M);
      const response10Month = await ky.get(url10M);

      const body2M = await response2Month.json();
      const body5M = await response5Month.json();
      const body7M = await response7Month.json();
      const body10M = await response10Month.json();

      const valuesRaw2M = body2M.map((o) => o['value']);
      const valuesRaw5M = body5M.map((o) => o['value']);
      const valuesRaw7M = body7M.map((o) => o['value']);
      const valuesRaw10M = body10M.map((o) => o['value']);

      const labelsRaw2M = body2M.map((o) => o['date'].join('-'));

      setValues2Month(valuesRaw2M);
      setValues5Month(valuesRaw5M);
      setValues7Month(valuesRaw7M);
      setValues10Month(valuesRaw10M);

      setLabels2Month(labelsRaw2M);
    };
    init();
  }, []);

  return (
    <MultiLineChartJsComponent
      datasets={[
        {
          label: 'Svensk statsobligation 2-års löptid',
          data: values2Month,
          borderColor: 'rgb(255, 255, 0)',
          backgroundColor: 'rgba(255, 255, 0, 1)',
          pointRadius: 1,
        },
        {
          label: 'Svensk statsobligation 5-års löptid',
          data: values5Month,
          borderColor: 'rgb(255, 99, 71)',
          backgroundColor: 'rgba(255, 99, 71, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Svensk statsobligation 7-års löptid',
          data: values7Month,
          borderColor: 'rgb(34, 139, 34)',
          backgroundColor: 'rgba(34, 139, 34, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Svensk statsobligation 10-års löptid',
          data: values10Month,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
      ]}
      labels={labels2Month}
      title={'Svenska Statsobligationer'}
    />
  );
};

export default ChartGovernmentBondsSweden;
