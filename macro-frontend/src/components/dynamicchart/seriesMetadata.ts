import { SeriesConfig } from '@/models/SeriesConfig';

// Categorical line colours in fixed order, stepped for the dark surface (validated for colour-vision deficiencies)
export const SERIES_PALETTE = [
  '#3987e5',
  '#d95926',
  '#199e70',
  '#c98500',
  '#d55181',
  '#008300',
  '#9085e9',
  '#e66767'
];

export type SeriesStyle = {
  color: string;
  dashed: boolean;
};

export type ChartSeries = {
  config: SeriesConfig;
  style: SeriesStyle;
  // Plotted values: the raw values, or in index mode rebased to 100 / the change in percentage points
  values: (number | null)[];
  rawValues: (number | null)[];
  // Set in index mode: how values were derived from rawValues
  indexKind?: IndexKind;
};

export type IndexKind = 'index' | 'change';

// Past the palette the hues repeat, so the second round is drawn dashed to stay distinguishable
export const styleForSlot = (slot: number): SeriesStyle => ({
  color: SERIES_PALETTE[slot % SERIES_PALETTE.length],
  dashed: slot >= SERIES_PALETTE.length
});

const isAggregate = (country: string) =>
  country.startsWith('Euroområdet') || country.startsWith('Europeiska unionen');

const countryRank = (country: string) => (country === 'Sverige' ? 0 : isAggregate(country) ? 1 : 2);

// Sverige first, then the EU/euro-area aggregates, then the rest alphabetically
export const compareCountries = (a: string, b: string) =>
  countryRank(a) - countryRank(b) || a.localeCompare(b, 'sv-SE');

const CATEGORY_ORDER = ['Räntor', 'Statsobligationer', 'Statsskuld', 'Valuta', 'Nyckeltal USA'];

const categoryRank = (category: string) =>
  CATEGORY_ORDER.includes(category) ? CATEGORY_ORDER.indexOf(category) : CATEGORY_ORDER.length;

// The known categories in a fixed order, any others after them alphabetically
export const orderedCategories = (configs: SeriesConfig[]) =>
  Array.from(new Set(configs.map((c) => c.category))).sort(
    (a, b) => categoryRank(a) - categoryRank(b) || a.localeCompare(b, 'sv-SE')
  );

export const formatMaturity = (months: number) =>
  months < 12 ? `${months} mån` : `${months / 12} år`;

// Every word of the query must occur somewhere in the name, so "tysk 10" finds "Statsobligation Tyskland 10-års löptid"
export const matchesAllWords = (text: string, query: string) => {
  const haystack = text.toLocaleLowerCase('sv-SE');
  return query
    .toLocaleLowerCase('sv-SE')
    .split(/\s+/)
    .filter(Boolean)
    .every((word) => haystack.includes(word));
};

export type CheckboxListGroup = {
  kind: 'list';
  subcategory: string;
  items: { label: string; config: SeriesConfig }[];
};

export type MaturityGridGroup = {
  kind: 'grid';
  subcategory: string;
  maturities: number[];
  rows: { country: string; cells: (SeriesConfig | undefined)[] }[];
};

// Subcategories holding the same countries in different units (e.g. Statsskuld nominellt / andel av BNP)
export type VariantListGroup = {
  kind: 'variants';
  variants: { subcategory: string; unit: string }[];
  rows: { country: string; configs: Record<string, SeriesConfig> }[];
};

export type PickerGroup = CheckboxListGroup | MaturityGridGroup | VariantListGroup;

const groupBy = <T>(items: T[], key: (item: T) => string): Map<string, T[]> => {
  const groups = new Map<string, T[]>();
  items.forEach((item) => {
    const k = key(item);
    groups.set(k, [...(groups.get(k) ?? []), item]);
  });
  return groups;
};

const hasDistinctCountries = (configs: SeriesConfig[]) =>
  configs.every((c) => c.country) && new Set(configs.map((c) => c.country)).size === configs.length;

const toListGroup = (subcategory: string, configs: SeriesConfig[]): CheckboxListGroup => {
  const byCountry = hasDistinctCountries(configs);
  const items = configs.map((config) => ({
    label: byCountry ? (config.country as string) : config.displayName,
    config
  }));
  if (byCountry) {
    items.sort((a, b) => compareCountries(a.label, b.label));
  }
  return { kind: 'list', subcategory, items };
};

const toGridGroup = (subcategory: string, configs: SeriesConfig[]): MaturityGridGroup => {
  const maturities = Array.from(new Set(configs.map((c) => c.maturityMonths as number))).sort((a, b) => a - b);
  const countries = Array.from(new Set(configs.map((c) => c.country as string))).sort(compareCountries);
  const rows = countries.map((country) => ({
    country,
    cells: maturities.map((months) =>
      configs.find((c) => c.country === country && c.maturityMonths === months)
    )
  }));
  return { kind: 'grid', subcategory, maturities, rows };
};

const toVariantGroup = (bySubcategory: Map<string, SeriesConfig[]>): VariantListGroup => {
  const variants = Array.from(bySubcategory.entries()).map(([subcategory, configs]) => ({
    subcategory,
    unit: configs[0].unit ?? subcategory
  }));
  const countries = Array.from(bySubcategory.values())[0]
    .map((c) => c.country as string)
    .sort(compareCountries);
  const rows = countries.map((country) => ({
    country,
    configs: Object.fromEntries(
      Array.from(bySubcategory.entries()).map(([subcategory, configs]) => [
        subcategory,
        configs.find((c) => c.country === country) as SeriesConfig
      ])
    )
  }));
  return { kind: 'variants', variants, rows };
};

const isVariantCategory = (bySubcategory: Map<string, SeriesConfig[]>) => {
  const groups = Array.from(bySubcategory.values());
  if (groups.length < 2 || groups.some((g) => g.some((c) => c.maturityMonths != null) || !hasDistinctCountries(g))) {
    return false;
  }
  const countrySet = (g: SeriesConfig[]) => g.map((c) => c.country).sort().join('|');
  return groups.every((g) => countrySet(g) === countrySet(groups[0]));
};

// Builds a category's picker layout from the metadata: maturities become a country × maturity grid,
// parallel per-country subcategories become one list with a unit toggle, everything else a checkbox list
export const buildPickerGroups = (configs: SeriesConfig[]): PickerGroup[] => {
  const bySubcategory = groupBy(configs, (c) => c.subcategory ?? '');
  if (isVariantCategory(bySubcategory)) {
    return [toVariantGroup(bySubcategory)];
  }
  return Array.from(bySubcategory.entries()).map(([subcategory, group]) =>
    group.every((c) => c.maturityMonths != null && c.country)
      ? toGridGroup(subcategory, group)
      : toListGroup(subcategory, group)
  );
};

const firstValue = (values: (number | null)[]) => values.find((v) => v !== null) ?? null;

// Rebases a series to 100 at its first non-null value in the window (series are null before their
// own start). A start at or below zero has no meaningful index (e.g. a negative policy rate), so such series give null
export const rebaseToIndex = (values: (number | null)[]): (number | null)[] | null => {
  const start = firstValue(values);
  if (start === null || start <= 0) return null;
  return values.map((v) => (v === null ? null : (v / start) * 100));
};

// Change since the first non-null value in the window, so the series starts at 0
export const changeSinceStart = (values: (number | null)[]): (number | null)[] | null => {
  const start = firstValue(values);
  if (start === null) return null;
  return values.map((v) => (v === null ? null : v - start));
};

// Rates and shares (unit "%" or "% av BNP") are compared in percentage points rather than indexed:
// a rate going 0,3 % → 5 % is not "index 1 700", and a negative start can't be indexed at all
export const isPercentUnit = (unit: string | undefined) => (unit ?? '').startsWith('%');

// Index mode: percentage series become the change in percentage points, the rest are rebased to 100.
// Series that can't be indexed (start at or below zero) are returned separately instead of plotted
export const toIndexMode = (series: ChartSeries[]): { plotted: ChartSeries[]; notIndexable: ChartSeries[] } => {
  const plotted: ChartSeries[] = [];
  const notIndexable: ChartSeries[] = [];
  series.forEach((s) => {
    const indexKind: IndexKind = isPercentUnit(s.config.unit) ? 'change' : 'index';
    const values = indexKind === 'change' ? changeSinceStart(s.rawValues) : rebaseToIndex(s.rawValues);
    if (values) {
      plotted.push({ ...s, values, indexKind });
    } else {
      notIndexable.push(s);
    }
  });
  return { plotted, notIndexable };
};

// The smallest "nice" tick step (1, 2, 2,5 or 5 × 10^n) of at least `min`
const niceStepAtLeast = (min: number) => {
  if (!(min > 0)) return 1;
  const magnitude = 10 ** Math.floor(Math.log10(min));
  return ([1, 2, 2.5, 5, 10].find((m) => m * magnitude >= min - 1e-9 * magnitude) as number) * magnitude;
};

export type AxisRange = { min: number; max: number; stepSize: number };

// Decimals needed to write a tick step exactly (0,25 → 2, 2,5 → 1, 50 → 0)
export const stepDecimals = (stepSize: number) => {
  let decimals = 0;
  while (decimals < 10 && Math.abs(Math.round(stepSize * 10 ** decimals) - stepSize * 10 ** decimals) > 1e-6) {
    decimals++;
  }
  return decimals;
};

// Scales for axes that each have a baseline (index 100, change 0) so all baselines sit at the same height.
// Every axis gets its own nice step but the same number of steps below and above its baseline; the split
// that leaves the least empty space wins
export const alignBaselines = (axes: { baseline: number; values: (number | null)[] }[]): AxisRange[] => {
  const extents = axes.map(({ baseline, values }) => {
    // A loop rather than Math.min(...values): daily series over all years are too long to spread
    let low = baseline;
    let high = baseline;
    values.forEach((v) => {
      if (v !== null) {
        low = Math.min(low, v);
        high = Math.max(high, v);
      }
    });
    return { baseline, below: baseline - low, above: high - baseline };
  });

  // Nothing moves away from its baseline: keep the baselines mid-axis instead of on the bottom border
  if (extents.every((e) => e.below === 0 && e.above === 0)) {
    return extents.map(({ baseline }) => ({ min: baseline - 2, max: baseline + 2, stepSize: 1 }));
  }

  let best: { ranges: AxisRange[]; waste: number } | null = null;
  for (let intervals = 4; intervals <= 10; intervals++) {
    for (let stepsBelow = 0; stepsBelow <= intervals; stepsBelow++) {
      const stepsAbove = intervals - stepsBelow;
      if (extents.some((e) => (e.below > 0 && stepsBelow === 0) || (e.above > 0 && stepsAbove === 0))) continue;
      const ranges = extents.map(({ baseline, below, above }) => {
        const stepSize = niceStepAtLeast(
          Math.max(stepsBelow ? below / stepsBelow : 0, stepsAbove ? above / stepsAbove : 0)
        );
        // Rounded to the step's decimals so the ticks (and the baseline tick) carry no float noise
        const round = (value: number) => Number(value.toFixed(stepDecimals(stepSize)));
        return { min: round(baseline - stepsBelow * stepSize), max: round(baseline + stepsAbove * stepSize), stepSize };
      });
      const waste = ranges.reduce(
        (sum, r, i) => sum + 1 - (extents[i].below + extents[i].above) / (r.max - r.min),
        0
      );
      // Strictly better only, so ties keep the fewer intervals
      if (!best || waste < best.waste - 1e-9) best = { ranges, waste };
    }
  }
  return (best as { ranges: AxisRange[] }).ranges;
};
