import { useEffect, useRef, useState } from 'react';
import * as maplibregl from 'maplibre-gl';
import type { ExpressionSpecification, GeoJSONSource, MapLayerMouseEvent } from 'maplibre-gl';
import type { BBox } from 'geojson';
import { Loader2 } from 'lucide-react';
import { AreaGeometry } from '@/api/regionalStatistics';
import { RegionalIndicator } from '@/models/RegionalStatistics';
import { applySwedishPlaceNames, createAreaPopup, createSwedenMap, fitArea, popupContent, whenState } from '@/components/maplibre/swedenMap';
import { borderColor, Classification, formatValue, NO_DATA_COLOR } from './classification';

const SOURCE = 'areas';
const FILL_LAYER = 'areas-fill';
const BORDER_LAYER = 'areas-border';
const HOVER_LAYER = 'areas-hover';
const SELECTED_CASING_LAYER = 'areas-selected-casing';
const SELECTED_LAYER = 'areas-selected';
// A new object moves the map even when it points at the same place again
export type MapFocus =
  | { kind: 'bounds'; bbox: BBox }
  | { kind: 'point'; lngLat: [number, number] };

export interface MapLayerData {
  geometry: AreaGeometry;
  values: Record<string, number>;
  classification: Classification;
  indicator: RegionalIndicator;
}

interface RegionalMapProps {
  // Null while values and boundaries for a new selection load; the map keeps what it shows until then
  data: MapLayerData | null;
  loading: boolean;
  selectedCode: string | null;
  onSelect: (code: string) => void;
  focus: MapFocus | null;
  marker: [number, number] | null;
  ariaLabel: string;
}

const RegionalMap = ({ data, loading, selectedCode, onSelect, focus, marker, ariaLabel }: RegionalMapProps) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<maplibregl.Map | null>(null);
  const [ready, setReady] = useState(false);
  // Handlers are bound once, so they read the latest props through refs
  const dataRef = useRef(data);
  const onSelectRef = useRef(onSelect);
  const selectedRef = useRef(selectedCode);
  const hoveredRef = useRef<string | null>(null);
  const popupRef = useRef<maplibregl.Popup | null>(null);
  dataRef.current = data;
  onSelectRef.current = onSelect;
  selectedRef.current = selectedCode;

  useEffect(() => {
    const map = createSwedenMap(
      containerRef.current as HTMLDivElement,
      '<a href="https://www.scb.se/" target="_blank" rel="noopener">SCB</a>',
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
    popupRef.current = popup;

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
        paint: { 'fill-color': NO_DATA_COLOR, 'fill-opacity': 0.5 },
      }, firstLabel);
      map.addLayer({
        id: BORDER_LAYER,
        type: 'line',
        source: SOURCE,
        paint: {
          'line-color': borderColor(NO_DATA_COLOR),
          // Faint at country zoom, where lines of thousands of small areas would otherwise grey out the fills.
          // A line under 1 px still draws at over half strength, so width alone cannot thin them
          'line-opacity': ['interpolate', ['linear'], ['zoom'], 5, 0.12, 7, 0.3, 8, 0.7],
          'line-width': ['interpolate', ['linear'], ['zoom'], 5, 0.2, 8, 0.4, 10, 0.7, 14, 1],
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
      const current = dataRef.current;
      const code = event.features?.[0]?.properties?.code as string | undefined;
      if (!current || !code) {
        return;
      }
      map.getCanvas().style.cursor = 'pointer';
      setHovered(code);
      const name = current.geometry.byCode.get(code)?.feature.properties.name ?? code;
      const value = current.values[code];
      popup
        .setLngLat(event.lngLat)
        .setDOMContent(popupContent(name, value === undefined ? 'Saknar data' : formatValue(value, current.indicator)))
        .addTo(map);
    });
    map.on('mouseleave', FILL_LAYER, () => {
      map.getCanvas().style.cursor = '';
      setHovered(null);
      popup.remove();
    });
    map.on('click', FILL_LAYER, (event: MapLayerMouseEvent) => {
      const code = event.features?.[0]?.properties?.code as string | undefined;
      if (code) {
        onSelectRef.current(code);
      }
    });

    return () => {
      popup.remove();
      map.remove();
      mapRef.current = null;
      popupRef.current = null;
      setReady(false);
    };
  }, []);

  const geometry = data?.geometry;
  useEffect(() => {
    const map = mapRef.current;
    if (!ready || !map || !geometry) {
      return;
    }
    // The hovered area may not exist in the new geometry, and the popup would show its old name until the next mousemove
    if (hoveredRef.current) {
      map.setFeatureState({ source: SOURCE, id: hoveredRef.current }, { hover: false });
      hoveredRef.current = null;
    }
    popupRef.current?.remove();
    map.getCanvas().style.cursor = '';
    (map.getSource(SOURCE) as GeoJSONSource).setData(geometry.collection);
  }, [ready, geometry]);

  const classification = data?.classification;
  useEffect(() => {
    const map = mapRef.current;
    if (!ready || !map || !classification) {
      return;
    }
    // Clearing drops the highlight flags too, so they are put back
    map.removeFeatureState({ source: SOURCE });
    classification.classByCode.forEach((cls, code) => map.setFeatureState({ source: SOURCE, id: code }, { cls }));
    if (selectedRef.current) {
      map.setFeatureState({ source: SOURCE, id: selectedRef.current }, { selected: true });
    }
    if (hoveredRef.current) {
      map.setFeatureState({ source: SOURCE, id: hoveredRef.current }, { hover: true });
    }
    const classIndex: ExpressionSpecification = ['coalesce', ['feature-state', 'cls'], -1];
    // A match needs at least one branch, so a year without values is painted as no data
    map.setPaintProperty(FILL_LAYER, 'fill-color', classification.classes.length === 0 ? NO_DATA_COLOR : [
      'match', classIndex,
      ...classification.classes.flatMap((c, i) => [i, c.color]),
      NO_DATA_COLOR,
    ] as ExpressionSpecification);
    map.setPaintProperty(BORDER_LAYER, 'line-color', classification.classes.length === 0 ? borderColor(NO_DATA_COLOR) : [
      'match', classIndex,
      ...classification.classes.flatMap((c, i) => [i, borderColor(c.color)]),
      borderColor(NO_DATA_COLOR),
    ] as ExpressionSpecification);
    // Lighter at street level so the roads under the areas show through
    map.setPaintProperty(FILL_LAYER, 'fill-opacity', [
      'interpolate', ['linear'], ['zoom'],
      9, ['case', ['>=', classIndex, 0], 0.85, 0.5],
      14, ['case', ['>=', classIndex, 0], 0.55, 0.3],
    ]);
  }, [ready, classification, geometry]);

  useEffect(() => {
    const map = mapRef.current;
    if (!ready || !map) {
      return;
    }
    if (!selectedCode) {
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

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !focus) {
      return;
    }
    if (focus.kind === 'bounds') {
      fitArea(map, focus.bbox, 14);
    } else {
      map.flyTo({ center: focus.lngLat, zoom: 14 });
    }
  }, [focus]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map || !marker) {
      return;
    }
    const pin = new maplibregl.Marker({ color: '#0b0b0b' }).setLngLat(marker).addTo(map);
    return () => {
      pin.remove();
    };
  }, [marker]);

  return (
    <div className="relative h-full w-full">
      <div ref={containerRef} className="h-full w-full" role="region" aria-label={ariaLabel} />
      {loading && (
        <div className="absolute top-3 left-3 flex items-center gap-2 rounded-md bg-background/90 px-3 py-1.5 text-sm text-foreground shadow">
          <Loader2 className="h-4 w-4 animate-spin" />
          Laddar…
        </div>
      )}
    </div>
  );
};

export default RegionalMap;
