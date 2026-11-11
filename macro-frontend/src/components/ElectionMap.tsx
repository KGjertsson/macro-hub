import { useEffect, useRef, useState } from 'react';
import type * as maplibregl from 'maplibre-gl';
import type { ExpressionSpecification, GeoJSONSource, MapLayerMouseEvent } from 'maplibre-gl';
import { useQuery } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { fetchAreaCollection } from '@/api/regionalStatistics';
import { applySwedishPlaceNames, createAreaPopup, createSwedenMap, fitArea, popupContent, whenState } from '@/components/maplibre/swedenMap';
import { NO_DATA_COLOR } from '@/components/regionalmap/classification';

const SOURCE = 'areas';
const FILL_LAYER = 'areas-fill';
const BORDER_LAYER = 'areas-border';
const HOVER_LAYER = 'areas-hover';
const SELECTED_CASING_LAYER = 'areas-selected-casing';
const SELECTED_LAYER = 'areas-selected';
// A kommun fills the view at about this zoom; a closer one would hide where it lies
const FOCUS_MAX_ZOOM = 9;

const highlighted: ExpressionSpecification = ['any',
  ['boolean', ['feature-state', 'hover'], false],
  ['boolean', ['feature-state', 'selected'], false],
];

export interface ElectionAreaLabel {
  name: string;
  detail: string;
}

// A new object moves the map even when it names the same area again
export type ElectionMapFocus = { code: string };

interface ElectionMapProps {
  topoUrl: string;
  objectName: string;
  colorByCode: Record<string, string>;
  // Names from the election data; the boundary file uses other forms, e.g. "Norrbottens" for the län
  labelByCode: Record<string, ElectionAreaLabel>;
  selectedCode: string | null;
  onSelect: (code: string | null) => void;
  focus: ElectionMapFocus | null;
  ariaLabel: string;
}

const useElectionAreas = (topoUrl: string, objectName: string) =>
  useQuery({
    queryKey: ['election-geometry', topoUrl],
    queryFn: () => fetchAreaCollection<{ code: string; name: string }>(topoUrl, objectName),
    staleTime: Infinity,
  });

const ElectionMap = ({ topoUrl, objectName, colorByCode, labelByCode, selectedCode, onSelect, focus, ariaLabel }: ElectionMapProps) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<maplibregl.Map | null>(null);
  const [ready, setReady] = useState(false);
  const { data: areas, isError } = useElectionAreas(topoUrl, objectName);
  // Handlers are bound once, so they read the latest props through refs
  const colorRef = useRef(colorByCode);
  const labelRef = useRef(labelByCode);
  const onSelectRef = useRef(onSelect);
  const selectedRef = useRef(selectedCode);
  const hoveredRef = useRef<string | null>(null);
  colorRef.current = colorByCode;
  labelRef.current = labelByCode;
  onSelectRef.current = onSelect;
  selectedRef.current = selectedCode;

  useEffect(() => {
    const map = createSwedenMap(
      containerRef.current as HTMLDivElement,
      '<a href="https://www.val.se/" target="_blank" rel="noopener">Valmyndigheten</a> | <a href="https://www.scb.se/" target="_blank" rel="noopener">SCB</a>',
    );
    mapRef.current = map;

    const setHovered = (code: string | null) => {
      if (hoveredRef.current === code) {
        return;
      }
      if (hoveredRef.current) {
        map.setFeatureState({ source: SOURCE, id: hoveredRef.current }, { hover: false });
      }
      if (code) {
        map.setFeatureState({ source: SOURCE, id: code }, { hover: true });
      }
      hoveredRef.current = code;
    };

    const popup = createAreaPopup();

    map.on('load', () => {
      const firstLabel = applySwedishPlaceNames(map);
      map.addSource(SOURCE, {
        type: 'geojson',
        data: { type: 'FeatureCollection', features: [] },
        promoteId: 'code',
      });
      map.addLayer({
        id: FILL_LAYER,
        type: 'fill',
        source: SOURCE,
        paint: {
          'fill-color': ['coalesce', ['feature-state', 'color'], NO_DATA_COLOR],
          'fill-opacity': ['case', highlighted, 0.9, 0.65],
        },
      }, firstLabel);
      // Neighbours often share a party colour, so a light border keeps them apart
      map.addLayer({
        id: BORDER_LAYER,
        type: 'line',
        source: SOURCE,
        paint: {
          'line-color': '#ffffff',
          'line-opacity': 0.8,
          'line-width': ['interpolate', ['linear'], ['zoom'], 4, 0.5, 8, 1.2, 12, 2],
        },
      }, firstLabel);
      map.addLayer({
        id: HOVER_LAYER,
        type: 'line',
        source: SOURCE,
        paint: { 'line-color': '#0b0b0b', 'line-width': 1.5, 'line-opacity': whenState('hover') },
      }, firstLabel);
      map.addLayer({
        id: SELECTED_CASING_LAYER,
        type: 'line',
        source: SOURCE,
        paint: { 'line-color': '#ffffff', 'line-width': 5, 'line-opacity': whenState('selected') },
      }, firstLabel);
      map.addLayer({
        id: SELECTED_LAYER,
        type: 'line',
        source: SOURCE,
        paint: { 'line-color': '#0b0b0b', 'line-width': 2.5, 'line-opacity': whenState('selected') },
      }, firstLabel);
      setReady(true);
    });

    map.on('mousemove', FILL_LAYER, (event: MapLayerMouseEvent) => {
      const code = event.features?.[0]?.properties?.code as string | undefined;
      if (!code) {
        return;
      }
      const label = labelRef.current[code];
      map.getCanvas().style.cursor = colorRef.current[code] ? 'pointer' : '';
      setHovered(code);
      popup
        .setLngLat(event.lngLat)
        .setDOMContent(popupContent(label?.name ?? event.features?.[0]?.properties?.name ?? code, label?.detail ?? 'Saknar resultat'))
        .addTo(map);
    });
    map.on('mouseleave', FILL_LAYER, () => {
      map.getCanvas().style.cursor = '';
      setHovered(null);
      popup.remove();
    });
    map.on('click', FILL_LAYER, (event: MapLayerMouseEvent) => {
      const code = event.features?.[0]?.properties?.code as string | undefined;
      if (code && colorRef.current[code]) {
        onSelectRef.current(selectedRef.current === code ? null : code);
      }
    });

    return () => {
      popup.remove();
      map.remove();
      mapRef.current = null;
      setReady(false);
    };
  }, []);

  useEffect(() => {
    const map = mapRef.current;
    if (!ready || !map || !areas) {
      return;
    }
    (map.getSource(SOURCE) as GeoJSONSource).setData(areas.collection);
  }, [ready, areas]);

  useEffect(() => {
    const map = mapRef.current;
    if (!ready || !map || !areas) {
      return;
    }
    // Clearing drops the highlight flags too, so they are put back
    map.removeFeatureState({ source: SOURCE });
    Object.entries(colorByCode).forEach(([code, color]) => map.setFeatureState({ source: SOURCE, id: code }, { color }));
    if (selectedRef.current) {
      map.setFeatureState({ source: SOURCE, id: selectedRef.current }, { selected: true });
    }
    if (hoveredRef.current) {
      map.setFeatureState({ source: SOURCE, id: hoveredRef.current }, { hover: true });
    }
  }, [ready, areas, colorByCode]);

  useEffect(() => {
    const map = mapRef.current;
    if (!ready || !map || !selectedCode) {
      return;
    }
    map.setFeatureState({ source: SOURCE, id: selectedCode }, { selected: true });
    return () => {
      // The map may already be gone when the component unmounts
      if (mapRef.current) {
        map.setFeatureState({ source: SOURCE, id: selectedCode }, { selected: false });
      }
    };
  }, [ready, selectedCode]);

  // A focus asked for before the boundaries arrive is applied once they do, and only once
  const appliedFocus = useRef<ElectionMapFocus | null>(null);
  useEffect(() => {
    const map = mapRef.current;
    const bbox = focus && areas?.byCode.get(focus.code)?.bbox;
    if (!map || !focus || !bbox || appliedFocus.current === focus) {
      return;
    }
    appliedFocus.current = focus;
    fitArea(map, bbox, FOCUS_MAX_ZOOM);
  }, [focus, areas]);

  return (
    <div className="relative h-full w-full">
      <div ref={containerRef} className="h-full w-full rounded-xl overflow-hidden" role="region" aria-label={ariaLabel} />
      {isError ? (
        <div className="absolute inset-0 flex items-center justify-center bg-background/80 text-sm text-muted-foreground">
          Kartan kunde inte laddas.
        </div>
      ) : (!ready || !areas) && (
        <div className="absolute top-3 left-3 flex items-center gap-2 rounded-md bg-background/90 px-3 py-1.5 text-sm text-foreground shadow">
          <Loader2 className="h-4 w-4 animate-spin" />
          Laddar…
        </div>
      )}
    </div>
  );
};

export default ElectionMap;
