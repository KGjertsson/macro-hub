'use client';
import { useQuery } from 'react-query';
import ChartJsComponent from '@/components/ChartJsLineComponent';

async function fetchPolicyRate() {
  const response = await fetch('http://localhost:8080/policy-rate/sweden');
  return response.json();
}

const ChartSwedenPolicyRate = () => {
  const { data, isLoading, isError } = useQuery(
    'swedenPolicyRate',
    fetchPolicyRate
  );

  if (isLoading) {
    return <p>Loading...</p>;
  }

  if (isError) {
    return <p>Error fetching data</p>;
  }

  // @ts-ignore
  const values = data.map((o) => o['value']);
  // @ts-ignore
  const labels = data.map((o) => o['date'].join('-'));

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
