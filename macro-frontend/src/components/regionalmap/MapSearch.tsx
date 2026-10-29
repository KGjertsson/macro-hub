import { useEffect, useMemo, useState } from 'react';
import { Loader2, MapPin, Search, Shapes } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { AddressHit, useAddressSearch } from '@/api/addressSearch';
import { AreaGeometry } from '@/api/regionalStatistics';

const DEBOUNCE_MS = 300;
const MAX_AREA_HITS = 8;

const useDebounced = <T,>(value: T, delayMs: number) => {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const timeout = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timeout);
  }, [value, delayMs]);
  return debounced;
};

interface MapSearchProps {
  geometry: AreaGeometry | null;
  areaLabel: string;
  onAddress: (hit: AddressHit) => void;
  onArea: (code: string) => void;
}

// One combobox for both addresses (Photon) and the loaded areas' names and codes
const MapSearch = ({ geometry, areaLabel, onAddress, onArea }: MapSearchProps) => {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebounced(query, DEBOUNCE_MS);
  const { data: addresses, isFetching } = useAddressSearch(debouncedQuery);

  const areaHits = useMemo(() => {
    const needle = query.trim().toLocaleLowerCase('sv');
    if (!geometry || needle.length < 2) {
      return [];
    }
    return geometry.collection.features
      .filter(f => f.properties.name.toLocaleLowerCase('sv').includes(needle) || f.properties.code.toLocaleLowerCase('sv').includes(needle))
      .slice(0, MAX_AREA_HITS)
      .map(f => f.properties);
  }, [geometry, query]);

  // Both checks, so a cleared box doesn't show the last results while the debounce catches up
  const addressHits = query.trim().length >= 3 && debouncedQuery.trim().length >= 3 ? addresses ?? [] : [];
  const searching = isFetching || query !== debouncedQuery;

  // Also on dismissal (outside click, Escape), so the next open starts empty
  const setOpenAndReset = (next: boolean) => {
    setOpen(next);
    if (!next) {
      setQuery('');
    }
  };
  const close = () => setOpenAndReset(false);

  return (
    <Popover open={open} onOpenChange={setOpenAndReset}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          aria-label={`Sök adress eller ${areaLabel}`}
          className="w-full justify-start text-muted-foreground font-normal">
          <Search className="mr-2 h-4 w-4 shrink-0 opacity-50" />
          Sök adress eller {areaLabel}…
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[var(--radix-popover-trigger-width)] min-w-[18rem] p-0" align="start">
        {/* Hits are filtered here and by Photon, so cmdk's own filtering is off */}
        <Command shouldFilter={false}>
          <CommandInput placeholder="T.ex. Drottninggatan 1, Stockholm" value={query} onValueChange={setQuery} />
          <CommandList>
            {query.trim().length >= 2 && !searching && areaHits.length === 0 && addressHits.length === 0 && (
              <CommandEmpty>Ingen träff.</CommandEmpty>
            )}
            {searching && query.trim().length >= 3 && (
              <div className="flex items-center gap-2 px-3 py-2 text-sm text-muted-foreground">
                <Loader2 className="h-4 w-4 animate-spin" />
                Söker…
              </div>
            )}
            {addressHits.length > 0 && (
              <CommandGroup heading="Adresser">
                {addressHits.map(hit => (
                  <CommandItem
                    key={hit.id}
                    value={`address-${hit.id}`}
                    onSelect={() => {
                      onAddress(hit);
                      close();
                    }}
                  >
                    <MapPin className="mr-2 h-4 w-4 shrink-0" />
                    <div className="min-w-0">
                      <div className="truncate">{hit.label}</div>
                      {hit.detail && <div className="truncate text-xs opacity-70">{hit.detail}</div>}
                    </div>
                  </CommandItem>
                ))}
              </CommandGroup>
            )}
            {areaHits.length > 0 && (
              <CommandGroup heading="Områden">
                {areaHits.map(area => (
                  <CommandItem
                    key={area.code}
                    value={`area-${area.code}`}
                    onSelect={() => {
                      onArea(area.code);
                      close();
                    }}
                  >
                    <Shapes className="mr-2 h-4 w-4 shrink-0" />
                    <span className="truncate">{area.name}</span>
                    <span className="ml-auto pl-2 text-xs opacity-70">{area.code}</span>
                  </CommandItem>
                ))}
              </CommandGroup>
            )}
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
};

export default MapSearch;
