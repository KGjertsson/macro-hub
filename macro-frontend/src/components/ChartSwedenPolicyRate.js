'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';
import ChartJsComponent from '@/components/ChartComponentTwo';

const hardCodedLabels = "['a', 'b', 'c']";
const hardCodedValues = '[1, 2, 1.2]';

const ChartSwedenPolicyRate = () => {
  const [labels, setLabels] = useState(hardCodedLabels);
  const [values, setValues] = useState(hardCodedValues);

  useEffect(() => {
    const init = async () => {
      const response = await ky.get('http://localhost:8080/policy-rate/sweden');
      const body = await response.json();
      console.log(body);
      const valuesRaw = body.map((o) => o['value']);
      const labelsRaw = body.map((o) => o['date'].join('-'));

      setValues(valuesRaw);
      setLabels(labelsRaw);
    };
    init();
  }, []);

  return <ChartJsComponent values={values} labels={labels} />;
};

export default ChartSwedenPolicyRate;
