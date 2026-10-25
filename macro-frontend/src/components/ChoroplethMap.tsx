import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { geoPath, geoTransverseMercator } from 'd3-geo';
import { feature } from 'topojson-client';
import type { Feature, FeatureCollection, Geometry } from 'geojson';
import type { GeometryCollection, Topology } from 'topojson-specification';

interface AreaProperties {
  code: string;
  name: string;
}

interface ChoroplethMapProps {
  topoUrl: string;
  objectName: string;
  colorByCode: Record<string, string>;
  // Names from the election data; the boundary file uses other forms, e.g. "Norrbottens" for the län
  nameByCode: Record<string, string>;
  selectedCode: string | null;
  onSelect: (code: string | null) => void;
  ariaLabel: string;
}

const WIDTH = 450;
const NO_DATA_COLOR = 'hsl(var(--muted))';
// Light enough to separate neighbours that share a party colour
const BORDER_COLOR = 'hsl(var(--foreground) / 0.45)';

// SWEREF 99 TM is a transverse Mercator centred on 15°E, so the map keeps the familiar shape of Sweden
const projection = () => geoTransverseMercator().rotate([-15, 0]);

const useAreas = (topoUrl: string, objectName: string) =>
  useQuery({
    queryKey: ['topology', topoUrl],
    queryFn: async () => {
      const response = await fetch(topoUrl);
      if (!response.ok) {
        throw new Error(`GET ${topoUrl} failed with ${response.status}`);
      }
      const topology = (await response.json()) as Topology;
      const object = topology.objects[objectName] as GeometryCollection<AreaProperties>;
      return feature(topology, object) as FeatureCollection<Geometry, AreaProperties>;
    },
    staleTime: Infinity,
  });

const ChoroplethMap = ({ topoUrl, objectName, colorByCode, nameByCode, selectedCode, onSelect, ariaLabel }: ChoroplethMapProps) => {
  const [hoveredCode, setHoveredCode] = useState<string | null>(null);
  const { data: areas, isError } = useAreas(topoUrl, objectName);

  const layout = useMemo(() => {
    if (!areas) {
      return null;
    }
    const fitted = projection().fitWidth(WIDTH, areas);
    const path = geoPath(fitted);
    const [, [, maxY]] = path.bounds(areas);
    return { path, height: Math.ceil(maxY) };
  }, [areas]);

  if (isError) {
    return <p className="text-sm text-muted-foreground text-center py-12">Kartan kunde inte laddas.</p>;
  }
  if (!areas || !layout) {
    return <div className="w-full aspect-[9/20] max-h-[70vh] rounded-xl bg-muted animate-pulse" />;
  }

  const fillFor = (code: string) => {
    const color = colorByCode[code];
    if (!color) {
      return NO_DATA_COLOR;
    }
    return selectedCode === code || hoveredCode === code ? color : `${color}80`;
  };

  // The selected area is drawn last so its thicker outline is not hidden by its neighbours
  const ordered = [...areas.features].sort((a, b) =>
    Number(a.properties.code === selectedCode) - Number(b.properties.code === selectedCode));
  const highlighted = areas.features.find(f => f.properties.code === (hoveredCode ?? selectedCode));
  const nameOf = (area: Feature<Geometry, AreaProperties>) => nameByCode[area.properties.code] ?? area.properties.name;

  const handleClick = (area: Feature<Geometry, AreaProperties>) => {
    const code = area.properties.code;
    if (!colorByCode[code]) {
      return;
    }
    onSelect(selectedCode === code ? null : code);
  };

  return (
    <div className="space-y-2">
      <p className="h-6 text-center text-sm font-medium text-foreground" aria-live="polite">
        {highlighted ? nameOf(highlighted) : ' '}
      </p>
      <svg
        viewBox={`0 0 ${WIDTH} ${layout.height}`}
        className="w-full h-auto max-h-[70vh]"
        role="img"
        aria-label={ariaLabel}
        style={{ filter: 'drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1))' }}
      >
        {ordered.map(area => {
          const code = area.properties.code;
          const isSelected = selectedCode === code;
          return (
            <path
              key={code}
              d={layout.path(area) ?? undefined}
              fill={fillFor(code)}
              stroke={isSelected ? 'hsl(var(--foreground))' : BORDER_COLOR}
              strokeWidth={isSelected ? 1.5 : 0.6}
              className={colorByCode[code] ? 'cursor-pointer transition-colors duration-200' : undefined}
              onMouseEnter={() => setHoveredCode(code)}
              onMouseLeave={() => setHoveredCode(null)}
              onClick={() => handleClick(area)}
            >
              <title>{nameOf(area)}</title>
            </path>
          );
        })}
      </svg>
    </div>
  );
};

export default ChoroplethMap;
