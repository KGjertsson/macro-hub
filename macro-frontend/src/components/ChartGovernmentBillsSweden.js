'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';
import MultiLineChartJsComponent from '@/components/MultiLineChartJsComponent';

const ChartGovernmentBillsSweden = () => {
  const [values1Month, setValues1Month] = useState([]);
  const [values3Month, setValues3Month] = useState([]);
  const [values6Month, setValues6Month] = useState([]);
  const [values12Month, setValues12Month] = useState([]);

  const [labels1Month, setLabels1Month] = useState([]);

  useEffect(() => {
    const init = async () => {
      const url1M = 'http://localhost:8080/government-bills/sweden?period=1';
      const url3M = 'http://localhost:8080/government-bills/sweden?period=3';
      const url6M = 'http://localhost:8080/government-bills/sweden?period=6';
      const url12M = 'http://localhost:8080/government-bills/sweden?period=12';

      const response1Month = await ky.get(url1M);
      const response3Month = await ky.get(url3M);
      const response6Month = await ky.get(url6M);
      const response12Month = await ky.get(url12M);

      const body1M = await response1Month.json();
      const body3M = await response3Month.json();
      const body6M = await response6Month.json();
      const body12M = await response12Month.json();

      const valuesRaw1M = body1M.map((o) => o['value']);
      const valuesRaw3M = body3M.map((o) => o['value']);
      const valuesRaw6M = body6M.map((o) => o['value']);
      const valuesRaw12M = body12M.map((o) => o['value']);

      const labelsRaw1M = body1M.map((o) => o['date'].join('-'));

      setValues1Month(valuesRaw1M);
      setValues3Month(valuesRaw3M);
      setValues6Month(valuesRaw6M);
      setValues12Month(valuesRaw12M);

      setLabels1Month(labelsRaw1M);
    };
    init();
  }, []);

  return (
    <MultiLineChartJsComponent
      datasets={[
        {
          label: 'Svensk statsskuldväxel 1-månads löptid',
          data: values1Month,
          borderColor: 'rgb(255, 255, 0)',
          backgroundColor: 'rgba(255, 255, 0, 1)',
          pointRadius: 1,
        },
        {
          label: 'Svensk statsskuldväxel 3-månaders löptid',
          data: values3Month,
          borderColor: 'rgb(255, 99, 71)',
          backgroundColor: 'rgba(255, 99, 71, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Svensk statsskuldväxel 6-månaders löptid',
          data: values6Month,
          borderColor: 'rgb(34, 139, 34)',
          backgroundColor: 'rgba(34, 139, 34, 0.1)',
          pointRadius: 1,
        },
        {
          label: 'Svensk statskuldsväxel 12-månaders löptid',
          data: values12Month,
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.1)',
          pointRadius: 1,
        },
      ]}
      labels={labels1Month}
      title={'Svenska statsskuldväxlar'}
    />
  );
};

export default ChartGovernmentBillsSweden;
