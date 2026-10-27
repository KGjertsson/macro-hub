import { useEffect, useMemo, useRef, useState } from 'react';
import type { MouseEvent, WheelEvent } from 'react';
import { useQuery } from '@tanstack/react-query';
import { geoPath, geoTransverseMercator } from 'd3-geo';
import { select } from 'd3-selection';
import { zoom, zoomIdentity } from 'd3-zoom';
import type { ZoomBehavior } from 'd3-zoom';
import { Maximize, Minus, Plus } from 'lucide-react';
import { feature } from 'topojson-client';
import type { Feature, FeatureCollection, Geometry } from 'geojson';
import type { GeometryCollection, Topology } from 'topojson-specification';
import { Button } from '@/components/ui/button';

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
const MAX_ZOOM = 16;
const ZOOM_STEP = 2;
const WHEEL_HINT_MS = 1500;
const IS_MAC = typeof navigator !== 'undefined' && /Mac|iPhone|iPad/.test(navigator.userAgent);

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
  const [showWheelHint, setShowWheelHint] = useState(false);
  const { data: areas, isError } = useAreas(topoUrl, objectName);
  const svgRef = useRef<SVGSVGElement>(null);
  const zoomGroupRef = useRef<SVGGElement>(null);
  const zoomRef = useRef<ZoomBehavior<SVGSVGElement, unknown> | null>(null);
  const wheelHintTimeout = useRef<ReturnType<typeof setTimeout>>();

  const layout = useMemo(() => {
    if (!areas) {
      return null;
    }
    const fitted = projection().fitWidth(WIDTH, areas);
    const path = geoPath(fitted);
    const [, [, maxY]] = path.bounds(areas);
    return { path, height: Math.ceil(maxY) };
  }, [areas]);

  useEffect(() => {
    if (!layout || !svgRef.current) {
      return;
    }
    const svg = select(svgRef.current);
    const behavior = zoom<SVGSVGElement, unknown>()
      .scaleExtent([1, MAX_ZOOM])
      .translateExtent([[0, 0], [WIDTH, layout.height]])
      // Hand jitter during a click should not count as a pan, which would swallow the selection
      .clickDistance(4)
      // A plain wheel scrolls the page; only Ctrl/⌘ + wheel (and trackpad pinch, which sets ctrlKey) zooms
      .filter(event => (event.type === 'wheel' ? event.ctrlKey || event.metaKey : !event.ctrlKey) && !event.button)
      .on('zoom', event => {
        zoomGroupRef.current?.setAttribute('transform', event.transform.toString());
      });
    svg.call(behavior).call(behavior.transform, zoomIdentity);
    zoomRef.current = behavior;
    return () => {
      svg.on('.zoom', null);
      zoomRef.current = null;
    };
  }, [layout]);

  useEffect(() => () => clearTimeout(wheelHintTimeout.current), []);

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

  const handleClick = (event: MouseEvent, area: Feature<Geometry, AreaProperties>) => {
    const code = area.properties.code;
    // The second click of a double-click zooms in rather than deselecting
    if (!colorByCode[code] || event.detail > 1) {
      return;
    }
    onSelect(selectedCode === code ? null : code);
  };

  const handleWheel = (event: WheelEvent) => {
    if (event.ctrlKey || event.metaKey) {
      return;
    }
    setShowWheelHint(true);
    clearTimeout(wheelHintTimeout.current);
    wheelHintTimeout.current = setTimeout(() => setShowWheelHint(false), WHEEL_HINT_MS);
  };

  const scaleBy = (factor: number) => {
    if (zoomRef.current && svgRef.current) {
      zoomRef.current.scaleBy(select(svgRef.current), factor);
    }
  };

  const resetZoom = () => {
    if (zoomRef.current && svgRef.current) {
      zoomRef.current.transform(select(svgRef.current), zoomIdentity);
    }
  };

  return (
    <div className="space-y-2">
      <p className="h-6 text-center text-sm font-medium text-foreground" aria-live="polite">
        {highlighted ? nameOf(highlighted) : ' '}
      </p>
      <div className="relative">
        <svg
          ref={svgRef}
          viewBox={`0 0 ${WIDTH} ${layout.height}`}
          className="w-full h-auto max-h-[70vh]"
          role="img"
          aria-label={ariaLabel}
          style={{ filter: 'drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1))' }}
          onWheel={handleWheel}
        >
          <g ref={zoomGroupRef}>
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
                  vectorEffect="non-scaling-stroke"
                  className={colorByCode[code] ? 'cursor-pointer transition-colors duration-200' : undefined}
                  onMouseEnter={() => setHoveredCode(code)}
                  onMouseLeave={() => setHoveredCode(null)}
                  onClick={event => handleClick(event, area)}
                >
                  <title>{nameOf(area)}</title>
                </path>
              );
            })}
          </g>
        </svg>
        <div className="absolute top-2 right-2 flex flex-col gap-1">
          <Button variant="outline" size="icon" className="h-8 w-8" aria-label="Zooma in" onClick={() => scaleBy(ZOOM_STEP)}>
            <Plus className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="icon" className="h-8 w-8" aria-label="Zooma ut" onClick={() => scaleBy(1 / ZOOM_STEP)}>
            <Minus className="h-4 w-4" />
          </Button>
          <Button variant="outline" size="icon" className="h-8 w-8" aria-label="Återställ" onClick={resetZoom}>
            <Maximize className="h-4 w-4" />
          </Button>
        </div>
        <div
          className={`pointer-events-none absolute inset-0 flex items-center justify-center rounded-xl bg-background/60 transition-opacity duration-300 ${showWheelHint ? 'opacity-100' : 'opacity-0'}`}
          aria-hidden="true"
        >
          <p className="rounded-md bg-foreground/80 px-3 py-1.5 text-sm font-medium text-background">
            Håll {IS_MAC ? '⌘' : 'Ctrl'} för att zooma
          </p>
        </div>
      </div>
    </div>
  );
};

export default ChoroplethMap;
