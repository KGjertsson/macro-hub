import { RegionalIndicator } from '@/models/RegionalStatistics';

export const CLASS_COUNT = 7;

// Sequential blue, steps 100..700 of the dataviz reference ramp (light = low)
const SEQUENTIAL = ['#cde2fb', '#9ec5f4', '#6da7ec', '#3987e5', '#256abf', '#184f95', '#0d366b'];
// Red <- gray -> blue with matching lightness per step (OKLab L ≈ 0.42 / 0.61 / 0.81 on each arm)
const DIVERGING = ['#8a2423', '#d44f48', '#f3aca6', '#f0efec', '#9ec5f4', '#3987e5', '#184f95'];
export const NO_DATA_COLOR = '#b4b3ad';

export interface ValueClass {
  color: string;
  // Inclusive lower and upper value of the areas in the class
  min: number;
  max: number;
}

export interface Classification {
  classes: ValueClass[];
  // Area code to index in classes
  classByCode: Map<string, number>;
  diverging: boolean;
}

const quantileBreaks = (sorted: number[], count: number) =>
  Array.from({ length: count - 1 }, (_, i) => sorted[Math.floor(((i + 1) * sorted.length) / count)]);

// Splits sorted values into at most `count` groups of about equal size; equal values always share a group
const quantileGroups = (sorted: number[], count: number) => {
  if (sorted.length === 0) {
    return [];
  }
  const breaks = Array.from(new Set(quantileBreaks(sorted, count))).filter(b => b > sorted[0]);
  const groups: number[][] = [[]];
  let next = 0;
  for (const value of sorted) {
    while (next < breaks.length && value >= breaks[next]) {
      groups.push([]);
      next++;
    }
    groups[groups.length - 1].push(value);
  }
  return groups.filter(g => g.length > 0);
};

// Picks `count` colours spread over the ramp, keeping both ends
const spread = (ramp: string[], count: number) =>
  count === 1 ? [ramp[Math.floor(ramp.length / 2)]] : Array.from({ length: count }, (_, i) => ramp[Math.round((i * (ramp.length - 1)) / (count - 1))]);

const toClasses = (groups: number[][], colors: string[]): ValueClass[] =>
  groups.map((group, i) => ({ color: colors[i], min: group[0], max: group[group.length - 1] }));

// Seven quantile classes; data with both signs (e.g. flyttnetto) gets three classes per side and a gray
// class around zero holding the seventh of the areas closest to it
export const classify = (values: Record<string, number>): Classification => {
  const sorted = Object.values(values).sort((a, b) => a - b);
  const diverging = sorted.length > 0 && sorted[0] < 0 && sorted[sorted.length - 1] > 0;

  let classes: ValueClass[];
  if (diverging) {
    const magnitudes = sorted.map(Math.abs).sort((a, b) => a - b);
    const neutralLimit = magnitudes[Math.floor(magnitudes.length / CLASS_COUNT)];
    const negative = quantileGroups(sorted.filter(v => v < -neutralLimit), 3);
    const neutral = sorted.filter(v => Math.abs(v) <= neutralLimit);
    const positive = quantileGroups(sorted.filter(v => v > neutralLimit), 3);
    // An arm with fewer than three classes keeps the colours nearest the midpoint
    classes = [
      ...toClasses(negative, DIVERGING.slice(0, 3).slice(3 - negative.length)),
      ...toClasses(neutral.length > 0 ? [neutral] : [], [DIVERGING[3]]),
      ...toClasses(positive, DIVERGING.slice(4, 4 + positive.length)),
    ];
  } else {
    const groups = quantileGroups(sorted, CLASS_COUNT);
    classes = toClasses(groups, spread(SEQUENTIAL, groups.length));
  }

  const classByCode = new Map<string, number>();
  for (const [code, value] of Object.entries(values)) {
    // Classes are sorted and contiguous, so the first one whose max reaches the value holds it
    classByCode.set(code, classes.findIndex(c => value <= c.max));
  }
  return { classes, classByCode, diverging };
};

export const formatNumber = (value: number, decimals: number) =>
  value.toLocaleString('sv-SE', { minimumFractionDigits: decimals, maximumFractionDigits: decimals });

// "antal" is implied by the indicator's label, so counts are shown bare
export const formatValue = (value: number, indicator: Pick<RegionalIndicator, 'unit' | 'decimals'>) =>
  indicator.unit === 'antal' ? formatNumber(value, indicator.decimals) : `${formatNumber(value, indicator.decimals)} ${indicator.unit}`;
