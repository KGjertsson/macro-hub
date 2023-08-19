import React, { useEffect, useState } from 'react';
import { useQuery } from 'react-query';

import {
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Title,
  Tooltip,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
);

const graphData = {
  SwedenPolicyRate: [],
  UsdSekExchangeRate: [],
};

const fetchPolicyRate = async () => {
  const response = await fetch('http://localhost:8080/policy-rate/sweden');
  return response.json();
};

const DynamicChartComponent = ({ graphConfig }) => {
  const { data, isLoading, isError } = useQuery(
    'swedenPolicyRate',
    fetchPolicyRate,
  );

  useEffect(() => {
    const init = async () => {
      graphConfig.forEach((graphConfigItem) => {
        const selectedGraphData = graphData[graphConfigItem];
        if (selectedGraphData.length === 0) {
          console.log('fetching data for ' + selectedGraphData);
        }
      });
    };

    init();
  }, [graphConfig]);

  if (isLoading) {
    return <p>Loading...</p>;
  }

  if (isError) {
    return <p>Error fetching data</p>;
  }

  return (
    <Line
      className="mx-auto w-3/5 overflow-hidden"
      style={{ width: '1000px', height: '1000px' }}
      options={{
        responsive: true,
        plugins: {
          legend: {
            position: 'top',
          },
          title: {
            display: true,
            text: title,
          },
        },
      }}
      data={{
        labels,
        datasets: datasets,
      }}
    />
  );
};

export default DynamicChartComponent;
