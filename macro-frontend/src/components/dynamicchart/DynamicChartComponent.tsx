import React from 'react';

import {
  CategoryScale,
  Chart as ChartJS,
  ChartDataset,
  ChartOptions,
  LinearScale,
  LineElement,
  PointElement,
  Tooltip
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import { ChartSeries } from '@/components/dynamicchart/seriesMetadata';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip);

const numberFormat = new Intl.NumberFormat('sv-SE', { maximumFractionDigits: 2 });

const formatValue = (value: number, unit?: string) =>
  unit ? `${numberFormat.format(value)} ${unit}` : numberFormat.format(value);

// Canvas can't read CSS variables, so resolve the theme tokens (stored as "H S% L%") once per render
const themeColor = (token: string) =>
  `hsl(${getComputedStyle(document.documentElement).getPropertyValue(token).trim()})`;

interface Props {
  labels: string[];
  series: ChartSeries[];
  // Series are rebased to 100 and share one axis
  indexed: boolean;
  // Units in the order their axes are assigned: the first goes left, the rest share the right axis
  units: string[];
}

const DynamicChartComponent = ({ labels, series, indexed, units }: Props) => {
  const textColor = themeColor('--muted-foreground');
  const gridColor = themeColor('--border');

  const axisFor = (unit: string | undefined) => (indexed || units.indexOf(unit ?? '') <= 0 ? 'y' : 'y1');

  const scale = (position: 'left' | 'right', title: string, drawGrid: boolean) => ({
    type: 'linear' as const,
    position,
    title: { display: true, text: title, color: textColor },
    ticks: { color: textColor, callback: (value: number | string) => numberFormat.format(Number(value)) },
    grid: { color: gridColor, display: drawGrid },
    border: { color: gridColor }
  });

  const datasets: ChartDataset<'line', (number | null)[]>[] = series.map((s) => ({
    data: s.values,
    label: s.config.displayName,
    borderColor: s.style.color,
    backgroundColor: s.style.color,
    borderDash: s.style.dashed ? [6, 4] : undefined,
    borderWidth: 2,
    pointRadius: 0,
    pointHoverRadius: 4,
    spanGaps: true,
    yAxisID: axisFor(s.config.unit)
  }));

  const scales: NonNullable<ChartOptions<'line'>['scales']> = {
    y: scale('left', indexed ? 'Index (start = 100)' : (units[0] ?? ''), true)
  };
  if (!indexed && units.length > 1) {
    scales.y1 = scale('right', units.slice(1).join(' / '), false);
  }

  return (
    <div className='relative h-[min(60vh,560px)] min-h-[320px] w-full'>
      <Line
        options={{
          responsive: true,
          maintainAspectRatio: false,
          locale: 'sv-SE',
          interaction: { mode: 'index', intersect: false },
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                label: (context) => {
                  const s = series[context.datasetIndex];
                  const raw = s.rawValues[context.dataIndex];
                  if (raw === null) return `${s.config.displayName}: –`;
                  const value = formatValue(raw, s.config.unit);
                  return indexed
                    ? `${s.config.displayName}: ${numberFormat.format(context.parsed.y)} (${value})`
                    : `${s.config.displayName}: ${value}`;
                }
              }
            }
          },
          scales: {
            x: {
              ticks: { color: textColor, maxTicksLimit: 8, maxRotation: 0 },
              grid: { display: false },
              border: { color: gridColor }
            },
            ...scales
          }
        }}
        data={{ labels, datasets }}
      />
    </div>
  );
};

export default DynamicChartComponent;
