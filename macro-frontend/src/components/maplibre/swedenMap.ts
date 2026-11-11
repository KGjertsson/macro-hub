import * as maplibregl from 'maplibre-gl';
import type { ExpressionSpecification, LngLatBoundsLike } from 'maplibre-gl';
import workerUrl from 'maplibre-gl/dist/maplibre-gl-worker.mjs?url';
import 'maplibre-gl/dist/maplibre-gl.css';
import type { BBox } from 'geojson';

// Bundling moves the library away from its worker file, so point it at the copy Vite emits
maplibregl.setWorkerUrl(workerUrl);

const STYLE_URL = 'https://tiles.openfreemap.org/styles/positron';
const SWEDEN_BOUNDS: LngLatBoundsLike = [[10.5, 55.0], [24.3, 69.1]];

// Highlights use feature state; changing a layer filter would make MapLibre re-parse the whole source
export const whenState = (flag: string): ExpressionSpecification => ['case', ['boolean', ['feature-state', flag], false], 1, 0];

export const createSwedenMap = (container: HTMLDivElement, customAttribution: string) => {
  const map = new maplibregl.Map({
    container,
    style: STYLE_URL,
    bounds: SWEDEN_BOUNDS,
    fitBoundsOptions: { padding: 16 },
    attributionControl: { customAttribution },
  });
  map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'top-right');
  map.dragRotate.disable();
  map.touchZoomRotate.disableRotation();
  return map;
};

// Areas go under the first label layer, which this returns, so place names stay readable
export const applySwedishPlaceNames = (map: maplibregl.Map) => {
  const layers = map.getStyle().layers;
  // The style labels places with their English names ("Sweden")
  layers
    .filter(layer => layer.type === 'symbol' && JSON.stringify(layer.layout?.['text-field'] ?? '').includes('name_en'))
    .forEach(layer => map.setLayoutProperty(layer.id, 'text-field', ['coalesce', ['get', 'name:sv'], ['get', 'name:latin'], ['get', 'name']]));
  return layers.find(layer => layer.type === 'symbol')?.id;
};

export const fitArea = (map: maplibregl.Map, [minX, minY, maxX, maxY]: BBox, maxZoom: number) =>
  map.fitBounds([[minX, minY], [maxX, maxY]], { padding: 60, maxZoom });

export const createAreaPopup = () =>
  new maplibregl.Popup({ closeButton: false, closeOnClick: false, offset: 8, className: 'area-map-popup' });

export const popupContent = (name: string, value: string) => {
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
