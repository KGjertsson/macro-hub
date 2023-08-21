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
import { Dataset } from '@/models/Dataset';

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
  labels: string[];
  selected: Dataset[];
}

const DynamicChartComponent = ({ labels, selected }: Props) => {
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
        labels: labels,
        datasets: selected.map((dataset) => {
          return {
            data: dataset.data,
            label: dataset.lineConfig.label,
            borderColor: dataset.lineConfig.borderColor,
            backgroundColor: dataset.lineConfig.backgroundColor,
            pointRadius: dataset.lineConfig.pointRadius,
          };
        }),
      }}
    />
  );
};

export default DynamicChartComponent;
