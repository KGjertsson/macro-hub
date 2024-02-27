import React from 'react';

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
import { DatasetWithLabels } from '@/models/DatasetCache/DatasetWithLabels';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

interface Props {
  sampled: DatasetWithLabels;
}

const DynamicChartComponent = ({ sampled }: Props) => {
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
            text: 'super bra titel',
          },
        },
      }}
      data={{
        labels: sampled.labels,
        datasets: sampled.chartData.map((dataset) => {
          return {
            data: dataset.values,
            label: dataset.name.toString(),
            borderColor: dataset.color,
            backgroundColor: dataset.color,
            pointRadius: 2,
          };
        }),
      }}
    />
  );
};

export default DynamicChartComponent;
