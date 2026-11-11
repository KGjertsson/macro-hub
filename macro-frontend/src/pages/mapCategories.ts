export const MAP_CATEGORIES = ['omraden', 'kommuner', 'regioner', 'valkretsar'] as const;

export type MapCategory = typeof MAP_CATEGORIES[number];

export const parseMapCategory = (raw: string | null): MapCategory =>
  MAP_CATEGORIES.find(category => category === raw) ?? 'omraden';
