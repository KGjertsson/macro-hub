import { ChartSeries } from '@/components/dynamicchart/seriesMetadata';

// A zoomed view: the first and last visible label, as formatted on the x-axis ("2020-01")
export type ChartZoom = [start: string, end: string];

// Inclusive indices into the fetched labels
export type ZoomWindow = { start: number; end: number };

// "2020-01,2022-06" from the z query parameter; anything else is no zoom
export const parseZoom = (raw: string | null): ChartZoom | undefined => {
  const parts = raw?.split(',');
  return parts?.length === 2 && parts[0] && parts[1] ? [parts[0], parts[1]] : undefined;
};

export const formatZoom = ([start, end]: ChartZoom) => `${start},${end}`;

// The labels of a zoom must both be in the data, in order; otherwise the full window is shown
export const resolveZoom = (labels: string[], zoom: ChartZoom | undefined): ZoomWindow | undefined => {
  if (!zoom) {
    return undefined;
  }
  const start = labels.indexOf(zoom[0]);
  const end = labels.indexOf(zoom[1]);
  return start >= 0 && end >= start ? { start, end } : undefined;
};

export const sliceLabels = (labels: string[], window: ZoomWindow | undefined) =>
  window ? labels.slice(window.start, window.end + 1) : labels;

export const sliceSeries = (series: ChartSeries[], window: ZoomWindow | undefined): ChartSeries[] =>
  window
    ? series.map((s) => ({
      ...s,
      values: s.values.slice(window.start, window.end + 1),
      rawValues: s.rawValues.slice(window.start, window.end + 1)
    }))
    : series;
