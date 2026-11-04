import { RegionalIndicator } from '@/models/RegionalStatistics';

// A share of the sorted values, as [numerator, denominator]. Whole numbers keep the index exact, where
// a fraction like 3 / 7 could round to just below the integer and land on the value before
type Share = [number, number];

// Five seventh-sized classes, then p90, p97 and p99 so a long upper tail is not one wide class. The 6/7
// break (about p86) is left out because it nearly coincides with p90
const SEQUENTIAL_UPPER_TAIL_BREAKS: Share[] = [[1, 7], [2, 7], [3, 7], [4, 7], [5, 7], [90, 100], [97, 100], [99, 100]];
const SEQUENTIAL_LOWER_TAIL_BREAKS: Share[] = [[1, 100], [3, 100], [10, 100], [2, 7], [3, 7], [4, 7], [5, 7], [6, 7]];
// How many times longer the lower tail must be to get the lower set; near-equal tails must not flip the
// legend between years
const LOWER_TAIL_MARGIN = 1.5;
const DIVERGING_ARM_BREAKS: Share[] = [[1, 3], [2, 3]];
const DIVERGING_CLASS_COUNT = 7;

// Sequential blue, steps 100..700 of the dataviz reference ramp in increments of 75 (light = low). Steps
// 175, 325, 475 and 625 are OKLab midpoints of the ramp's neighbours; OKLab L falls by about 0.07 per step
const SEQUENTIAL = ['#cde2fb', '#abccf5', '#86b6ef', '#61a0ea', '#3987e5', '#2771ca', '#1c5cab', '#14488b', '#0d366b'];
// Red <- gray -> blue with matching lightness per step (OKLab L ≈ 0.42 / 0.61 / 0.81 on each arm)
const DIVERGING = ['#8a2423', '#d44f48', '#f3aca6', '#f0efec', '#9ec5f4', '#3987e5', '#184f95'];
export const NO_DATA_COLOR = '#b4b3ad';

const LIGHT_BORDER = '#ffffff';
// Softer than the #0b0b0b of the hover and selected lines, so those stay stronger
const DARK_BORDER = '#262626';
// The land colour of the positron basemap, which shows through the translucent fill
const BASEMAP_LAND = '#f2f3f0';
// Halfway between RegionalMap's fill opacity at zoom 9 (0.85) and zoom 14 (0.55)
const TYPICAL_FILL_OPACITY = 0.7;

const toRgb = (hex: string) => [1, 3, 5].map(i => parseInt(hex.slice(i, i + 2), 16));

// WCAG relative luminance
const luminance = (rgb: number[]) => {
  const [r, g, b] = rgb.map(channel => {
    const c = channel / 255;
    return c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4;
  });
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
};

const contrast = (a: number, b: number) => (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);

// The border colour with the higher contrast against the fill as it looks on the map, not the opaque
// colour: most of the ramp ends up mid-toned there, so white is only for the darkest fills
export const borderColor = (fill: string) => {
  const land = toRgb(BASEMAP_LAND);
  const onMap = luminance(toRgb(fill).map((c, i) => c * TYPICAL_FILL_OPACITY + land[i] * (1 - TYPICAL_FILL_OPACITY)));
  return contrast(luminance(toRgb(LIGHT_BORDER)), onMap) > contrast(luminance(toRgb(DARK_BORDER)), onMap) ? LIGHT_BORDER : DARK_BORDER;
};

export interface ValueClass {
  color: string;
  min: number;
  max: number;
}

export interface Classification {
  classes: ValueClass[];
  classByCode: Map<string, number>;
  diverging: boolean;
}

const quantileBreaks = (sorted: number[], shares: Share[]) =>
  shares.map(([numerator, denominator]) => sorted[Math.floor((numerator * sorted.length) / denominator)]);

// Equal values always share a group, so ties can give fewer groups
const quantileGroups = (sorted: number[], shares: Share[]) => {
  if (sorted.length === 0) {
    return [];
  }
  const breaks = Array.from(new Set(quantileBreaks(sorted, shares))).filter(b => b > sorted[0]);
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

// The upper set unless the lower tail is clearly longer, measured at p1 and p99 so one extreme area can't
// decide it. The lower set also needs p1 > min: otherwise the low end is a spike of equal values (e.g. areas
// at exactly 0), not a tail, and its low breaks would collapse into one class
const sequentialBreaks = (sorted: number[]) => {
  const [p1, median, p99] = quantileBreaks(sorted, [[1, 100], [1, 2], [99, 100]]);
  return p1 > sorted[0] && median - p1 >= LOWER_TAIL_MARGIN * (p99 - median) ? SEQUENTIAL_LOWER_TAIL_BREAKS : SEQUENTIAL_UPPER_TAIL_BREAKS;
};

const spread = (ramp: string[], count: number) =>
  count === 1 ? [ramp[Math.floor(ramp.length / 2)]] : Array.from({ length: count }, (_, i) => ramp[Math.round((i * (ramp.length - 1)) / (count - 1))]);

const toClasses = (groups: number[][], colors: string[]): ValueClass[] =>
  groups.map((group, i) => ({ color: colors[i], min: group[0], max: group[group.length - 1] }));

export const classify = (values: Record<string, number>): Classification => {
  const sorted = Object.values(values).sort((a, b) => a - b);
  const diverging = sorted.length > 0 && sorted[0] < 0 && sorted[sorted.length - 1] > 0;

  let classes: ValueClass[];
  if (diverging) {
    const magnitudes = sorted.map(Math.abs).sort((a, b) => a - b);
    const neutralLimit = magnitudes[Math.floor(magnitudes.length / DIVERGING_CLASS_COUNT)];
    const negative = quantileGroups(sorted.filter(v => v < -neutralLimit), DIVERGING_ARM_BREAKS);
    const neutral = sorted.filter(v => Math.abs(v) <= neutralLimit);
    const positive = quantileGroups(sorted.filter(v => v > neutralLimit), DIVERGING_ARM_BREAKS);
    // An arm with fewer than three classes keeps the colours nearest the midpoint
    classes = [
      ...toClasses(negative, DIVERGING.slice(0, 3).slice(3 - negative.length)),
      ...toClasses(neutral.length > 0 ? [neutral] : [], [DIVERGING[3]]),
      ...toClasses(positive, DIVERGING.slice(4, 4 + positive.length)),
    ];
  } else {
    const groups = quantileGroups(sorted, sequentialBreaks(sorted));
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
