import { useEffect, useRef, useState } from 'react';
import * as maplibregl from 'maplibre-gl';
import type { ExpressionSpecification, GeoJSONSource, LngLatBoundsLike, MapLayerMouseEvent } from 'maplibre-gl';
import workerUrl from 'maplibre-gl/dist/maplibre-gl-worker.mjs?url';
import 'maplibre-gl/dist/maplibre-gl.css';
import type { BBox } from 'geojson';
import { Loader2 } from 'lucide-react';
import { AreaGeometry } from '@/api/regionalStatistics';
import { RegionalIndicator } from '@/models/RegionalStatistics';
import { Classification, formatValue, NO_DATA_COLOR } from './classification';

// Bundling moves the library away from its worker file, so point it at the copy Vite emits
maplibregl.setWorkerUrl(workerUrl);

const STYLE_URL = 'https://tiles.openfreemap.org/styles/positron';
const SWEDEN_BOUNDS: LngLatBoundsLike = [[10.5, 55.0], [24.3, 69.1]];
const SOURCE = 'areas';
const FILL_LAYER = 'areas-fill';
const HOVER_LAYER = 'areas-hover';
const SELECTED_CASING_LAYER = 'areas-selected-casing';
const SELECTED_LAYER = 'areas-selected';
// Highlights use feature state; changing a layer filter would make MapLibre re-parse the whole source
const whenState = (flag: string): ExpressionSpecification => ['case', ['boolean', ['feature-state', flag], false], 1, 0];

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

const popupContent = (name: string, value: string) => {
  const root = document.createElement('div');
  // The popup is white, so it needs dark ink regardless of the page's text colour
  root.className = 'text-sm text-neutral-900';
  const title = document.createElement('div');
  title.className = 'font-semibold';
  title.textContent = name;
  const body = document.createElement('div');
  body.textContent = value;
  root.append(title, body);
  return root;
};

const RegionalMap = ({ data, loading, selectedCode, onSelect, focus, marker, ariaLabel }: RegionalMapProps) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<maplibregl.Map | null>(null);
  const [ready, setReady] = useState(false);
  // Handlers are bound once, so they read the latest props through refs
  const dataRef = useRef(data);
  const onSelectRef = useRef(onSelect);
  const selectedRef = useRef(selectedCode);
  const hoveredRef = useRef<string | null>(null);
  dataRef.current = data;
  onSelectRef.current = onSelect;
  selectedRef.current = selectedCode;

  useEffect(() => {
    const map = new maplibregl.Map({
      container: containerRef.current as HTMLDivElement,
      style: STYLE_URL,
      bounds: SWEDEN_BOUNDS,
      fitBoundsOptions: { padding: 16 },
      attributionControl: {
        customAttribution: '<a href="https://www.scb.se/" target="_blank" rel="noopener">SCB</a>',
      },
    });
    map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'top-right');
    map.dragRotate.disable();
    map.touchZoomRotate.disableRotation();
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

    const popup = new maplibregl.Popup({ closeButton: false, closeOnClick: false, offset: 8, className: 'regional-map-popup' });

    map.on('load', () => {
      const layers = map.getStyle().layers;
      // The style labels places with their English names ("Sweden"); prefer the Swedish ones
      layers
        .filter(layer => layer.type === 'symbol' && JSON.stringify(layer.layout?.['text-field'] ?? '').includes('name_en'))
        .forEach(layer => map.setLayoutProperty(layer.id, 'text-field', ['coalesce', ['get', 'name:sv'], ['get', 'name:latin'], ['get', 'name']]));
      // Areas go under the basemap's labels so place names stay readable
      const firstLabel = layers.find(layer => layer.type === 'symbol')?.id;
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
        id: 'areas-border',
        type: 'line',
        source: SOURCE,
        paint: {
          'line-color': '#ffffff',
          'line-opacity': 0.7,
          'line-width': ['interpolate', ['linear'], ['zoom'], 5, 0.2, 10, 0.6, 14, 1.2],
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
      setReady(false);
    };
  }, []);

  const geometry = data?.geometry;
  useEffect(() => {
    const map = mapRef.current;
    if (!ready || !map || !geometry) {
      return;
    }
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
      const [minX, minY, maxX, maxY] = focus.bbox;
      map.fitBounds([[minX, minY], [maxX, maxY]], { padding: 60, maxZoom: 14 });
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
