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

interface ChartJsComponentProps {
  values: number[]; // Assuming values is an array of numbers
  labels: string[]; // Assuming labels is an array of strings
  label: string;
  borderColor: string;
  backgroundColor: string;
  title: string;
}

const ChartJsComponent = ({
  values,
  labels,
  label,
  borderColor,
  backgroundColor,
  title,
}: ChartJsComponentProps) => {
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
        datasets: [
          {
            label: label,
            data: values,
            borderColor: borderColor,
            backgroundColor: backgroundColor,
            pointRadius: 1,
          },
        ],
      }}
    />
  );
};

export default ChartJsComponent;