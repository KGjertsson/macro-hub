import React, { useCallback, useEffect, useRef } from 'react';

import {
  CategoryScale,
  Chart as ChartJS,
  ChartDataset,
  ChartOptions,
  LinearScale,
  LineElement,
  Plugin,
  PointElement,
  Tooltip
} from 'chart.js';
import { Line } from 'react-chartjs-2';
import { alignBaselines, AxisRange, ChartSeries, SeriesStyle, stepDecimals } from '@/components/dynamicchart/seriesMetadata';

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

const formatValue = (value: number, unit?: string) =>
  unit ? `${valueFormat.format(value)} ${unit}` : valueFormat.format(value);

// Canvas can't read CSS variables, so resolve the theme tokens (stored as "H S% L%") once per render
const themeColor = (token: string, alpha = 1) =>
  `hsl(${getComputedStyle(document.documentElement).getPropertyValue(token).trim()} / ${alpha})`;

// Shorter drags are clicks: on daily data a single pixel can span several labels
const MIN_DRAG_PX = 5;

// A drag in progress, in canvas pixels; the band is drawn between its start and the pointer
type Drag = { pointerId: number; startX: number; currentX: number };

// Draws the selection band of a drag over the plot area
const dragBandPlugin = (
  dragRef: React.RefObject<Drag | null>,
  colorsRef: React.RefObject<{ fill: string; edge: string }>
): Plugin<'line'> => ({
  id: 'dragZoomBand',
  // Chart.js ignores pointer events during a drag, so a touch drag doesn't keep re-opening the tooltip
  beforeEvent: () => (dragRef.current ? false : undefined),
  afterDatasetsDraw: (chart) => {
    const drag = dragRef.current;
    if (!drag) {
      return;
    }
    const { left, right, top, bottom } = chart.chartArea;
    const from = Math.max(left, Math.min(drag.startX, drag.currentX));
    const to = Math.min(right, Math.max(drag.startX, drag.currentX));
    const { ctx } = chart;
    ctx.save();
    ctx.fillStyle = colorsRef.current.fill;
    ctx.fillRect(from, top, to - from, bottom - top);
    ctx.strokeStyle = colorsRef.current.edge;
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(from, top);
    ctx.lineTo(from, bottom);
    ctx.moveTo(to, top);
    ctx.lineTo(to, bottom);
    ctx.stroke();
    ctx.restore();
  }
});

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
  // Called with the first and last label index of a drag across the plot
  onZoom: (start: number, end: number) => void;
  // Double-click on the plot
  onResetZoom: () => void;
}

const DynamicChartComponent = ({ labels, series, indexed, units, onZoom, onResetZoom }: Props) => {
  const textColor = themeColor('--muted-foreground');
  const gridColor = themeColor('--border');

  const chartRef = useRef<ChartJS<'line'>>(null);
  const dragRef = useRef<Drag | null>(null);
  const bandColors = useRef({ fill: '', edge: '' });
  bandColors.current = { fill: themeColor('--primary', 0.15), edge: themeColor('--primary', 0.6) };
  // Created once: react-chartjs-2 would rebuild the chart for a new plugins array
  const plugins = useRef([dragBandPlugin(dragRef, bandColors)]);

  const pointerX = (event: React.PointerEvent) =>
    event.clientX - (chartRef.current?.canvas.getBoundingClientRect().left ?? 0);

  const endDrag = useCallback(() => {
    dragRef.current = null;
    chartRef.current?.draw();
  }, []);

  // Esc cancels a drag in progress
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && dragRef.current) {
        endDrag();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [endDrag]);

  const onPointerDown = (event: React.PointerEvent<HTMLDivElement>) => {
    const chart = chartRef.current;
    if (!chart || !event.isPrimary || event.button !== 0) {
      return;
    }
    const x = pointerX(event);
    const y = event.clientY - chart.canvas.getBoundingClientRect().top;
    const { left, right, top, bottom } = chart.chartArea;
    if (x < left || x > right || y < top || y > bottom) {
      return;
    }
    // Capture so the drag keeps following the pointer outside the chart
    event.currentTarget.setPointerCapture(event.pointerId);
    dragRef.current = { pointerId: event.pointerId, startX: x, currentX: x };
  };

  const onPointerMove = (event: React.PointerEvent<HTMLDivElement>) => {
    const chart = chartRef.current;
    const drag = dragRef.current;
    if (!chart || !drag || drag.pointerId !== event.pointerId) {
      return;
    }
    drag.currentX = pointerX(event);
    // Hide the tooltip that was showing when the drag started; Chart.js ignores events until it ends
    if (chart.tooltip && chart.tooltip.getActiveElements().length > 0) {
      chart.tooltip.setActiveElements([], { x: 0, y: 0 });
      chart.setActiveElements([]);
      chart.update('none');
    } else {
      chart.draw();
    }
  };

  const onPointerUp = (event: React.PointerEvent<HTMLDivElement>) => {
    const chart = chartRef.current;
    const drag = dragRef.current;
    if (!chart || !drag || drag.pointerId !== event.pointerId) {
      return;
    }
    endDrag();
    const { left, right } = chart.chartArea;
    const indexAt = (x: number) => {
      const index = chart.scales.x.getValueForPixel(Math.max(left, Math.min(right, x)));
      return Math.max(0, Math.min(labels.length - 1, Math.round(index ?? 0)));
    };
    const endX = pointerX(event);
    const a = indexAt(drag.startX);
    const b = indexAt(endX);
    // Spanning fewer than two data points, or a few pixels of hand movement on dense data, is a click
    if (a !== b && Math.abs(endX - drag.startX) >= MIN_DRAG_PX) {
      onZoom(Math.min(a, b), Math.max(a, b));
    }
  };

  const hasIndex = indexed && series.some((s) => s.indexKind === 'index');
  const hasChange = indexed && series.some((s) => s.indexKind === 'change');

  // In index mode the index axis is on the left; the percentage-point axis goes right, or left when it is alone
  const axisFor = (s: ChartSeries) =>
    indexed
      ? (s.indexKind === 'change' && hasIndex ? 'y1' : 'y')
      : (units.indexOf(s.config.unit ?? '') <= 0 ? 'y' : 'y1');

  type ScaleOptions = {
    drawGrid?: boolean;
    // Index mode marks the start level (100, or 0 p.e.) with a stronger grid line
    reference?: number;
    // Percentage-point changes are labelled with a sign
    signed?: boolean;
    range?: AxisRange;
    // Labels below this value are left blank (the grid line stays)
    labelsFrom?: number;
  };

  const scale = (
    position: 'left' | 'right',
    title: string,
    { drawGrid = false, reference, signed = false, range, labelsFrom }: ScaleOptions = {}
  ) => {
    // Fixed steps get as many decimals as the step needs, so neighbouring ticks never share a label
    const format = new Intl.NumberFormat('sv-SE', {
      maximumFractionDigits: range ? stepDecimals(range.stepSize) : 2,
      signDisplay: signed ? 'exceptZero' : 'auto'
    });
    return {
      type: 'linear' as const,
      position,
      min: range?.min,
      max: range?.max,
      title: { display: true, text: title, color: textColor },
      ticks: {
        color: textColor,
        stepSize: range?.stepSize,
        callback: (value: number | string) =>
          labelsFrom !== undefined && Number(value) < labelsFrom ? '' : format.format(Number(value))
      },
      grid: {
        display: drawGrid,
        // Tolerant compare: tick values can carry float noise (0 as 6e-17)
        color: (context: { tick?: { value: number } }) =>
          reference !== undefined && context.tick && Math.abs(context.tick.value - reference) < 1e-9
            ? textColor
            : gridColor
      },
      border: { color: gridColor }
    };
  };

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
    scales.y = scale('left', units[0] ?? '', { drawGrid: true });
    if (units.length > 1) {
      scales.y1 = scale('right', units.slice(1).join(' / '));
    }
  } else if (hasIndex && hasChange) {
    // Both baselines (100 and 0 p.e.) at the same height, so every line starts from one common line
    const indexValues = series.filter((s) => s.indexKind === 'index').flatMap((s) => s.values);
    const [indexRange, changeRange] = alignBaselines([
      { baseline: 100, values: indexValues },
      { baseline: 0, values: series.filter((s) => s.indexKind === 'change').flatMap((s) => s.values) }
    ]);
    // Alignment can stretch the index axis below 0; label those ticks only if an index series gets there
    const indexBelowZero = indexValues.some((v) => v !== null && v < 0);
    scales.y = scale('left', 'Index (start = 100)', {
      drawGrid: true,
      reference: 100,
      range: indexRange,
      labelsFrom: indexBelowZero ? undefined : 0
    });
    scales.y1 = scale('right', changeTitle, { signed: true, range: changeRange });
  } else if (hasIndex) {
    scales.y = scale('left', 'Index (start = 100)', { drawGrid: true, reference: 100 });
  } else {
    scales.y = scale('left', changeTitle, { drawGrid: true, reference: 0, signed: true });
  }

  return (
    <div
      // Horizontal touch drags select a period; vertical ones still scroll the page
      className='relative h-[min(60vh,560px)] min-h-[320px] w-full cursor-crosshair touch-pan-y select-none'
      onPointerDown={onPointerDown}
      onPointerMove={onPointerMove}
      onPointerUp={onPointerUp}
      onPointerCancel={endDrag}
      onDoubleClick={onResetZoom}
    >
      <Line
        ref={chartRef}
        plugins={plugins.current}
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
