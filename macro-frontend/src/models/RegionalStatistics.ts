export type AreaLevel = 'DeSO' | 'RegSO';

export const AREA_LEVELS: AreaLevel[] = ['DeSO', 'RegSO'];

export type GeoVersion = 'DeSO2018' | 'DeSO2025' | 'RegSO2020' | 'RegSO2025';

export interface IndicatorAvailability {
  year: number;
  geoVersion: GeoVersion;
}

export interface RegionalIndicator {
  id: string;
  label: string;
  description: string | null;
  category: string;
  unit: string;
  decimals: number;
  availability: Record<AreaLevel, IndicatorAvailability[]>;
}

export interface RegionalValues {
  indicator: string;
  level: AreaLevel;
  year: number;
  geoVersion: GeoVersion;
  // Keys have no _DeSO2025/_RegSO2025 suffix
  values: Record<string, number>;
}

export interface AreaProperties {
  code: string;
  name: string;
  kommunkod: string;
  regsokod?: string;
}

export const GEO_FILES: Record<GeoVersion, string> = {
  DeSO2018: 'deso_2018',
  DeSO2025: 'deso_2025',
  RegSO2020: 'regso_2020',
  RegSO2025: 'regso_2025',
};

export const geoVersionLabel = (version: GeoVersion) => version.replace(/(\d{4})$/, ' $1');
