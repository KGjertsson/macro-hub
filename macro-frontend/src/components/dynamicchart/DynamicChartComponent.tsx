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
import { alignBaselines, AxisRange, ChartSeries, SeriesStyle } from '@/components/dynamicchart/seriesMetadata';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip);

const numberFormat = new Intl.NumberFormat('sv-SE', { maximumFractionDigits: 2 });
// Rates are published with up to three decimals
const valueFormat = new Intl.NumberFormat('sv-SE', { maximumFractionDigits: 3 });

// Percentage-point changes always carry a sign: +4,70 / −0,25
const changeFormat = new Intl.NumberFormat('sv-SE', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
  signDisplay: 'exceptZero'
});
const changeTickFormat = new Intl.NumberFormat('sv-SE', { maximumFractionDigits: 2, signDisplay: 'exceptZero' });

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
  // Series are rebased to 100 on the left axis, percentage series show their change in percentage points
  indexed: boolean;
  // Units in the order their axes are assigned: the first goes left, the rest share the right axis
  units: string[];
}

const DynamicChartComponent = ({ labels, series, indexed, units }: Props) => {
  const textColor = themeColor('--muted-foreground');
  const gridColor = themeColor('--border');

  const hasIndex = indexed && series.some((s) => s.indexKind === 'index');
  const hasChange = indexed && series.some((s) => s.indexKind === 'change');

  // In index mode the index axis is on the left; the percentage-point axis goes right, or left when it is alone
  const axisFor = (s: ChartSeries) =>
    indexed
      ? (s.indexKind === 'change' && hasIndex ? 'y1' : 'y')
      : (units.indexOf(s.config.unit ?? '') <= 0 ? 'y' : 'y1');

  const scale = (
    position: 'left' | 'right',
    title: string,
    drawGrid: boolean,
    // Index mode marks the start level (100, or 0 p.e.) with a stronger grid line
    reference?: number,
    format: Intl.NumberFormat = numberFormat,
    range?: AxisRange
  ) => ({
    type: 'linear' as const,
    position,
    min: range?.min,
    max: range?.max,
    title: { display: true, text: title, color: textColor },
    ticks: {
      color: textColor,
      stepSize: range?.stepSize,
      callback: (value: number | string) => format.format(Number(value))
    },
    grid: {
      display: drawGrid || reference !== undefined,
      color: (context: { tick?: { value: number } }) =>
        context.tick?.value === reference ? textColor : drawGrid ? gridColor : 'transparent'
    },
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
    yAxisID: axisFor(s)
  }));

  const changeTitle = 'Förändring (procentenheter)';
  const scales: NonNullable<ChartOptions<'line'>['scales']> = {};
  if (!indexed) {
    scales.y = scale('left', units[0] ?? '', true);
    if (units.length > 1) {
      scales.y1 = scale('right', units.slice(1).join(' / '), false);
    }
  } else if (hasIndex && hasChange) {
    // Both baselines (100 and 0 p.e.) at the same height, so every line starts from one common line
    const [indexRange, changeRange] = alignBaselines([
      { baseline: 100, values: series.filter((s) => s.indexKind === 'index').flatMap((s) => s.values) },
      { baseline: 0, values: series.filter((s) => s.indexKind === 'change').flatMap((s) => s.values) }
    ]);
    scales.y = scale('left', 'Index (start = 100)', true, 100, numberFormat, indexRange);
    scales.y1 = scale('right', changeTitle, false, undefined, changeTickFormat, changeRange);
  } else if (hasIndex) {
    scales.y = scale('left', 'Index (start = 100)', true, 100);
  } else {
    scales.y = scale('left', changeTitle, true, 0, changeTickFormat);
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
                  if (s.indexKind === 'change') {
                    return `${s.config.displayName}: ${changeFormat.format(context.parsed.y)} p.e. (${value})`;
                  }
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
