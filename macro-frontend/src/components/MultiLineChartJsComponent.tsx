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

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

interface MultiLineChartJsComponentProps {
  datasets: number[];
  labels: string[];
  title: string;
}

const MultiLineChartJsComponent = ({
  datasets,
  labels,
  title,
}: MultiLineChartJsComponentProps) => {
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
        datasets: [],
      }}
    />
  );
};

export default MultiLineChartJsComponent;
