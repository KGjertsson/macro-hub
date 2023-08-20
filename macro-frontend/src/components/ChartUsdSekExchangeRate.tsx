'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';

import ChartJsComponent from '@/components/ChartJsLineComponent';

const ChartUsdSekExchangeRate = () => {
  const [labels, setLabels] = useState([]);
  const [values, setValues] = useState([]);

  useEffect(() => {
    const init = async () => {
      const url = 'http://localhost:8080/exchange-rate/usd-sek';
      const response = await ky.get(url);
      const body = await response.json();
      // @ts-ignore
      const valuesRaw = body.map((o) => o['value']);
      // @ts-ignore
      const labelsRaw = body.map((o) => o['date'].join('-'));

      setValues(valuesRaw);
      setLabels(labelsRaw);
    };
    init();
  }, []);

  return (
    <ChartJsComponent
      values={values}
      labels={labels}
      label={'USD till SEK växelkurs'}
      borderColor={'rgb(75, 192, 192)'}
      backgroundColor={'rgb(75, 192, 192, 0.1)'}
      title={'USD till SEK växelkurs'}
    />
  );
};

export default ChartUsdSekExchangeRate;
