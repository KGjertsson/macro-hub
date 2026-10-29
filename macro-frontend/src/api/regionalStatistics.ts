import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { feature } from 'topojson-client';
import type { BBox, Feature, FeatureCollection, Geometry, Position } from 'geojson';
import type { GeometryCollection, Topology } from 'topojson-specification';
import { rootUrl } from '@/models/Constants';
import { AreaLevel, AreaProperties, GEO_FILES, GeoVersion, RegionalIndicator, RegionalValues } from '@/models/RegionalStatistics';

const baseUrl = `${rootUrl}/macro-analyzer/regional-statistics`;

// The backend answers 204 when nothing has been scraped yet
const getJson = async <T>(path: string, empty: T): Promise<T> => {
  const response = await fetch(baseUrl + path);
  if (response.status === 204) {
    return empty;
  }
  if (!response.ok) {
    throw new Error(`GET ${path} failed with ${response.status}`);
  }
  return response.json();
};

export const useRegionalIndicators = () =>
  useQuery({
    queryKey: ['regional-statistics', 'indicators'],
    queryFn: () => getJson<RegionalIndicator[]>('/indicators', []),
    staleTime: Infinity,
  });

// Without a year the backend picks the latest one; the answer says which year and boundary version it used
export const useRegionalValues = (indicator: string | null, level: AreaLevel, year: number | null) =>
  useQuery({
    queryKey: ['regional-statistics', 'values', indicator, level, year],
    queryFn: () => {
      const params = new URLSearchParams({ indicator: indicator ?? '', level });
      if (year !== null) {
        params.set('year', String(year));
      }
      return getJson<RegionalValues | null>(`/values?${params}`, null);
    },
    enabled: indicator !== null,
    staleTime: Infinity,
    placeholderData: keepPreviousData,
  });

export interface AreaFeature {
  feature: Feature<Geometry, AreaProperties>;
  bbox: BBox;
}

export interface AreaGeometry {
  version: GeoVersion;
  collection: FeatureCollection<Geometry, AreaProperties>;
  byCode: Map<string, AreaFeature>;
}

const extendBounds = (bbox: BBox, coordinates: unknown) => {
  if (typeof (coordinates as Position)[0] === 'number') {
    const [x, y] = coordinates as Position;
    bbox[0] = Math.min(bbox[0], x);
    bbox[1] = Math.min(bbox[1], y);
    bbox[2] = Math.max(bbox[2], x);
    bbox[3] = Math.max(bbox[3], y);
  } else {
    (coordinates as unknown[]).forEach(child => extendBounds(bbox, child));
  }
};

const boundsOf = (geometry: Geometry): BBox => {
  const bbox: BBox = [Infinity, Infinity, -Infinity, -Infinity];
  if (geometry.type === 'Polygon' || geometry.type === 'MultiPolygon') {
    extendBounds(bbox, geometry.coordinates);
  }
  return bbox;
};

// The DeSO files are about 3 MB, so only the version the values refer to is fetched, once per session
export const useAreaGeometry = (version: GeoVersion | null) =>
  useQuery({
    queryKey: ['regional-statistics', 'geometry', version],
    queryFn: async (): Promise<AreaGeometry> => {
      const name = GEO_FILES[version as GeoVersion];
      const response = await fetch(`/geo/${name}.topo.json`);
      if (!response.ok) {
        throw new Error(`GET /geo/${name}.topo.json failed with ${response.status}`);
      }
      const topology = (await response.json()) as Topology;
      const object = topology.objects[name] as GeometryCollection<AreaProperties>;
      const collection = feature(topology, object) as FeatureCollection<Geometry, AreaProperties>;
      const byCode = new Map(collection.features.map(f => [f.properties.code, { feature: f, bbox: boundsOf(f.geometry) }]));
      return { version: version as GeoVersion, collection, byCode };
    },
    enabled: version !== null,
    staleTime: Infinity,
  });

export const useKommunNames = () =>
  useQuery({
    queryKey: ['regional-statistics', 'kommun-names'],
    queryFn: async () => {
      const response = await fetch('/geo/kommuner.topo.json');
      if (!response.ok) {
        throw new Error(`GET /geo/kommuner.topo.json failed with ${response.status}`);
      }
      const topology = (await response.json()) as Topology;
      const object = topology.objects.kommuner as GeometryCollection<{ code: string; name: string }>;
      return new Map(object.geometries.map(g => {
        const { code, name } = g.properties as { code: string; name: string };
        return [code, name];
      }));
    },
    staleTime: Infinity,
  });
