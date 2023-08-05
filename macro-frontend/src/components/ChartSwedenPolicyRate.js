'use client';
import { useEffect, useState } from 'react';
import ky from 'ky';

import ChartComponent from '@/components/ChartComponent';

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
      const valuesRaw = body
        .map((o) => o['value'])
        .slice(0, 5)
        .join(', ');
      const values = '[' + valuesRaw + ']';
      const labelsRaw = body
        .map((o) => '"' + o['date'].join('-') + '"')
        .slice(0, 5)
        .join(', ');
      const labels = '[' + labelsRaw + ']';

      setValues(values);
      setLabels(labels);
    };
    init();
  }, []);

  return <ChartComponent values={values} labels={labels} />;
};

export default ChartSwedenPolicyRate;
