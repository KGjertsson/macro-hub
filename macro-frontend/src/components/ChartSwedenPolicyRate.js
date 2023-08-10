'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';

import ChartJsComponent from '@/components/ChartJsLineComponent';

const ChartSwedenPolicyRate = () => {
  const [labels, setLabels] = useState([]);
  const [values, setValues] = useState([]);

  useEffect(() => {
    const init = async () => {
      const response = await ky.get('http://localhost:8080/policy-rate/sweden');
      const body = await response.json();
      const valuesRaw = body.map((o) => o['value']);
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
      label={'Styrränta'}
      borderColor={'rgb(75, 192, 192)'}
      backgroundColor={'rgb(75, 192, 192, 0.1)'}
      title={'Sveriges styrränta'}
    />
  );
};

export default ChartSwedenPolicyRate;
