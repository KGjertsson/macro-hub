import { useMemo, useRef } from 'react';
import { SeriesStyle, styleForSlot } from '@/components/dynamicchart/seriesMetadata';

// Gives each selected series a palette slot that it keeps while others are added or removed;
// a newly added series takes the lowest free slot
export const useSeriesStyles = (names: string[]): Record<string, SeriesStyle> => {
  const slots = useRef(new Map<string, number>());
  // Series names never contain commas, so the joined key is a stable dependency that round-trips
  const key = names.join(',');

  return useMemo(() => {
    const selected = key ? key.split(',') : [];
    const current = slots.current;
    Array.from(current.keys())
      .filter((name) => !selected.includes(name))
      .forEach((name) => current.delete(name));
    selected
      .filter((name) => !current.has(name))
      .forEach((name) => {
        const taken = new Set(current.values());
        let slot = 0;
        while (taken.has(slot)) slot++;
        current.set(name, slot);
      });
    return Object.fromEntries(selected.map((name) => [name, styleForSlot(current.get(name) as number)]));
  }, [key]);
};
