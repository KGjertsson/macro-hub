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
  // Plotted values: the raw values, or rebased to 100 in index mode
  values: (number | null)[];
  rawValues: (number | null)[];
};

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

// Rebases a series to 100 at its first value in the window. A start at or below zero has no
// meaningful index (e.g. a negative policy rate), so such series give null
export const rebaseToIndex = (values: (number | null)[]): (number | null)[] | null => {
  const start = values.find((v) => v !== null);
  if (start === undefined || start === null || start <= 0) return null;
  return values.map((v) => (v === null ? null : (v / start) * 100));
};
