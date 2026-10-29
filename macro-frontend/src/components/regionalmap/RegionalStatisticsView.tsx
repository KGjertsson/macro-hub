import { useEffect, useMemo, useRef, useState } from 'react';
import { geoContains } from 'd3-geo';
import { MapPinned } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group';
import { AddressHit } from '@/api/addressSearch';
import { AreaGeometry, useAreaGeometry, useKommunNames, useRegionalIndicators, useRegionalValues } from '@/api/regionalStatistics';
import { AREA_LEVELS, AreaLevel, geoVersionLabel, RegionalIndicator } from '@/models/RegionalStatistics';
import AreaPanel from './AreaPanel';
import { classify } from './classification';
import MapLegend from './MapLegend';
import MapSearch from './MapSearch';
import RegionalMap, { MapFocus, MapLayerData } from './RegionalMap';
import { useRegionalMapSearchParams } from './useRegionalMapSearchParams';

const DEFAULT_INDICATOR = 'population';

const hasData = (indicator: RegionalIndicator, level: AreaLevel) => indicator.availability[level].length > 0;

const containingArea = (geometry: AreaGeometry, [x, y]: [number, number]) =>
  geometry.collection.features.find(f => {
    const [minX, minY, maxX, maxY] = geometry.byCode.get(f.properties.code)!.bbox;
    return x >= minX && x <= maxX && y >= minY && y <= maxY && geoContains(f, [x, y]);
  });

// Categories in the order their first indicator appears (the backend sorts by sort_order)
const groupByCategory = (indicators: RegionalIndicator[]) => {
  const groups = new Map<string, RegionalIndicator[]>();
  indicators.forEach(indicator => groups.set(indicator.category, [...(groups.get(indicator.category) ?? []), indicator]));
  return [...groups.entries()];
};

const RegionalStatisticsView = () => {
  const [state, update] = useRegionalMapSearchParams();
  const { data: indicators, isLoading, isError } = useRegionalIndicators();
  const { data: kommunNames } = useKommunNames();
  const [focus, setFocus] = useState<MapFocus | null>(null);
  const [marker, setMarker] = useState<[number, number] | null>(null);
  // An address picked before the boundaries are in; its area is selected when they arrive
  const [pendingAddress, setPendingAddress] = useState<[number, number] | null>(null);
  const focusedFromUrl = useRef(false);

  const withData = (indicators ?? []).filter(i => AREA_LEVELS.some(level => hasData(i, level)));
  const indicator = withData.find(i => i.id === state.indicator)
    ?? withData.find(i => i.id === DEFAULT_INDICATOR)
    ?? withData[0];
  // An indicator without data at the requested level (e.g. a RegSO-only one) is shown at the other level
  const level: AreaLevel = !indicator || hasData(indicator, state.level)
    ? state.level
    : AREA_LEVELS.find(l => hasData(indicator, l)) ?? state.level;
  const years = indicator?.availability[level] ?? [];
  const year = years.some(y => y.year === state.year) ? state.year : null;

  const valuesQuery = useRegionalValues(indicator?.id ?? null, level, year);
  const values = valuesQuery.data ?? null;
  const geometryQuery = useAreaGeometry(values?.geoVersion ?? null);
  const geometry = geometryQuery.data?.version === values?.geoVersion ? geometryQuery.data : undefined;

  // Values may still be the previous selection's while the new ones load, so the legend follows the values
  const shownIndicator = indicators?.find(i => i.id === values?.indicator);
  const layerData = useMemo<MapLayerData | null>(() => {
    if (!values || !geometry || !shownIndicator) {
      return null;
    }
    return { geometry, values: values.values, classification: classify(values.values), indicator: shownIndicator };
  }, [values, geometry, shownIndicator]);

  const kommunByCode = useMemo(
    () => new Map((geometry?.collection.features ?? []).map(f => [f.properties.code, f.properties.kommunkod])),
    [geometry],
  );

  const selectedArea = state.area ? geometry?.byCode.get(state.area) : undefined;

  // A shared link with an area zooms to it once the first boundaries are in
  useEffect(() => {
    if (!focusedFromUrl.current && geometry) {
      focusedFromUrl.current = true;
      if (selectedArea) {
        setFocus({ kind: 'bounds', bbox: selectedArea.bbox });
      }
    }
  }, [geometry, selectedArea]);

  // Write coerced values (unknown indicator or year, a RegSO-only indicator at DeSO) back, so a copied link matches the page
  useEffect(() => {
    if (!indicator) {
      return;
    }
    const indicatorCoerced = state.indicator !== null && state.indicator !== indicator.id;
    const yearCoerced = state.year !== null && state.year !== year;
    if (indicatorCoerced || yearCoerced || state.level !== level) {
      update({ indicator: indicator.id, level, year, area: state.level === level ? state.area : null });
    }
  }, [indicator, level, year, state, update]);

  useEffect(() => {
    if (pendingAddress && geometry) {
      setPendingAddress(null);
      update({ area: containingArea(geometry, pendingAddress)?.properties.code ?? null });
    }
  }, [pendingAddress, geometry, update]);

  const selectIndicator = (id: string) => {
    const next = withData.find(i => i.id === id);
    if (!next) {
      return;
    }
    const nextLevel = hasData(next, level) ? level : AREA_LEVELS.find(l => hasData(next, l)) ?? level;
    update({
      indicator: id,
      level: nextLevel,
      year: next.availability[nextLevel].some(y => y.year === year) ? year : null,
      area: nextLevel === level ? state.area : null,
    });
  };

  const selectLevel = (nextLevel: AreaLevel) => {
    if (!nextLevel || nextLevel === level) {
      return;
    }
    setMarker(null);
    update({
      level: nextLevel,
      year: indicator?.availability[nextLevel].some(y => y.year === year) ? year : null,
      area: null,
    });
  };

  const selectAddress = (hit: AddressHit) => {
    setMarker(hit.lngLat);
    setFocus({ kind: 'point', lngLat: hit.lngLat });
    if (geometry) {
      setPendingAddress(null);
      update({ area: containingArea(geometry, hit.lngLat)?.properties.code ?? null });
    } else {
      setPendingAddress(hit.lngLat);
    }
  };

  const selectArea = (code: string) => {
    const area = geometry?.byCode.get(code);
    setMarker(null);
    if (area) {
      setFocus({ kind: 'bounds', bbox: area.bbox });
    }
    update({ area: code });
  };

  if (isError) {
    return <p className="text-center text-muted-foreground">Statistiken kunde inte hämtas.</p>;
  }
  if (!isLoading && withData.length === 0) {
    return <p className="text-center text-muted-foreground">Ingen områdesstatistik har hämtats ännu.</p>;
  }

  const loading = isLoading || valuesQuery.isFetching || (values !== null && !geometry);
  const areaLabel = level === 'DeSO' ? 'DeSO-område' : 'RegSO-område';

  return (
    <div className="space-y-6">
      <div className="bg-card rounded-2xl border border-border p-4 shadow-lg space-y-4">
        <div className="grid gap-3 md:grid-cols-[1fr_auto] lg:grid-cols-[minmax(0,1fr)_auto_minmax(0,18rem)_auto]">
          <MapSearch geometry={geometry ?? null} areaLabel={areaLabel} onAddress={selectAddress} onArea={selectArea} />
          <ToggleGroup
            type="single"
            variant="outline"
            value={level}
            onValueChange={value => selectLevel(value as AreaLevel)}
            aria-label="Områdesindelning"
            className="justify-start"
          >
            {AREA_LEVELS.map(l => (
              <ToggleGroupItem key={l} value={l} disabled={!indicator || !hasData(indicator, l)} className="px-4">
                {l}
              </ToggleGroupItem>
            ))}
          </ToggleGroup>
          <Select value={indicator?.id ?? ''} onValueChange={selectIndicator}>
            <SelectTrigger aria-label="Statistik">
              <SelectValue placeholder="Välj statistik" />
            </SelectTrigger>
            <SelectContent>
              {groupByCategory(indicators ?? []).map(([category, members]) => (
                <SelectGroup key={category}>
                  <SelectLabel>{category}</SelectLabel>
                  {members.map(i => (
                    <SelectItem key={i.id} value={i.id} disabled={!withData.includes(i)}>
                      {i.label}
                      {withData.includes(i) && !hasData(i, 'DeSO') && (
                        <span className="ml-2 text-xs text-muted-foreground">endast RegSO</span>
                      )}
                      {!withData.includes(i) && <span className="ml-2 text-xs text-muted-foreground">ingen data</span>}
                    </SelectItem>
                  ))}
                </SelectGroup>
              ))}
            </SelectContent>
          </Select>
          <div className="flex items-center gap-3">
            <Select
              value={String(year ?? years[0]?.year ?? '')}
              onValueChange={value => update({ year: Number(value) })}
              disabled={years.length === 0}
            >
              <SelectTrigger className="w-28" aria-label="År">
                <SelectValue placeholder="År" />
              </SelectTrigger>
              <SelectContent>
                {years.map(y => (
                  <SelectItem key={y.year} value={String(y.year)}>
                    {y.year}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {values && (
              <Badge variant="outline" className="whitespace-nowrap" title="Områdesindelningen som värdena avser">
                {geoVersionLabel(values.geoVersion)}
              </Badge>
            )}
          </div>
        </div>
        {indicator?.description && <p className="text-sm text-muted-foreground">{indicator.description}</p>}
      </div>

      <div className="grid lg:grid-cols-3 gap-6 items-start">
        <div className="lg:col-span-2 relative bg-card rounded-2xl border border-border shadow-lg overflow-hidden">
          <div className="h-[60vh] sm:h-[70vh] min-h-[360px] sm:min-h-[420px]">
            <RegionalMap
              data={layerData}
              loading={loading}
              selectedCode={state.area}
              onSelect={code => {
                setMarker(null);
                update({ area: code });
              }}
              focus={focus}
              marker={marker}
              ariaLabel={`Karta över Sverige per ${areaLabel}`}
            />
          </div>
          {/* Below the map on phones, where an overlay would hide most of it */}
          {layerData && values && (
            <div className="p-3 sm:p-0 sm:absolute sm:bottom-3 sm:left-3 sm:max-w-[16rem]">
              <MapLegend classification={layerData.classification} indicator={layerData.indicator} year={values.year} />
            </div>
          )}
        </div>

        <div className="bg-card rounded-2xl border border-border p-6 shadow-lg">
          {selectedArea && layerData && values ? (
            <AreaPanel
              area={selectedArea.feature.properties}
              kommunName={kommunNames?.get(selectedArea.feature.properties.kommunkod)}
              level={values.level}
              indicator={layerData.indicator}
              year={values.year}
              geoVersion={values.geoVersion}
              values={values.values}
              kommunByCode={kommunByCode}
              onClose={() => {
                setMarker(null);
                update({ area: null });
              }}
            />
          ) : (
            <div className="flex flex-col items-center justify-center min-h-[300px] text-center">
              <MapPinned className="h-12 w-12 text-muted-foreground/50 mb-4" />
              <h2 className="text-xl font-semibold text-foreground mb-2">Välj ett område</h2>
              <p className="text-muted-foreground max-w-sm">
                {state.area && geometry
                  ? `Området ${state.area} finns inte i indelningen ${geoVersionLabel(geometry.version)}. Välj ett annat område på kartan.`
                  : 'Klicka på ett område på kartan eller sök efter en adress för att se värdet och hur området placerar sig i kommunen och i Sverige.'}
              </p>
            </div>
          )}
          <p className="mt-6 text-xs text-muted-foreground">Källa: SCB (CC0). Karta: OpenFreeMap, © OpenStreetMap.</p>
        </div>
      </div>
    </div>
  );
};

export default RegionalStatisticsView;
