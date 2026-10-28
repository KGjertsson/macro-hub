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
import { ChartSeries, SeriesStyle } from '@/components/dynamicchart/seriesMetadata';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip);

const numberFormat = new Intl.NumberFormat('sv-SE', { maximumFractionDigits: 2 });
// Rates are published with up to three decimals
const valueFormat = new Intl.NumberFormat('sv-SE', { maximumFractionDigits: 3 });

const formatValue = (value: number, unit?: string) =>
  unit ? `${valueFormat.format(value)} ${unit}` : valueFormat.format(value);

// Canvas can't read CSS variables, so resolve the theme tokens (stored as "H S% L%") once per render
const themeColor = (token: string) =>
  `hsl(${getComputedStyle(document.documentElement).getPropertyValue(token).trim()})`;

// Tooltip swatch: a short line sample like the series chips, so a dashed series isn't shown as the
// same filled square as the solid series sharing its hue. An SVG image stays sharp on high-DPI canvases
const swatchCache = new Map<string, HTMLImageElement>();
const swatchFor = ({ color, dashed }: SeriesStyle) => {
  const key = `${color}:${dashed}`;
  let image = swatchCache.get(key);
  if (!image) {
    const dash = dashed ? " stroke-dasharray='4 2'" : '';
    const svg = `<svg xmlns='http://www.w3.org/2000/svg' width='16' height='12'><line x1='0' y1='6' x2='16' y2='6' stroke='${color}' stroke-width='3'${dash}/></svg>`;
    image = new Image(16, 12);
    image.src = `data:image/svg+xml,${encodeURIComponent(svg)}`;
    swatchCache.set(key, image);
  }
  return image;
};

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
              // Series are null outside their own date range; leave them out instead of listing a blank
              filter: (item) => item.parsed.y !== null && !Number.isNaN(item.parsed.y),
              usePointStyle: true,
              boxWidth: 16,
              boxPadding: 4,
              callbacks: {
                labelPointStyle: (context) => ({ pointStyle: swatchFor(series[context.datasetIndex].style), rotation: 0 }),
                label: (context) => {
                  const s = series[context.datasetIndex];
                  const value = formatValue(s.rawValues[context.dataIndex] as number, s.config.unit);
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
