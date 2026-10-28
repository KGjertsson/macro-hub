import { X } from 'lucide-react';
import { SeriesConfig } from '@/models/SeriesConfig';
import { SeriesStyle } from '@/components/dynamicchart/seriesMetadata';

interface SwatchProps {
  style: SeriesStyle;
}

// A short line sample, dashed like the chart line when the palette has wrapped around
export const SeriesSwatch = ({ style }: SwatchProps) => (
  <span
    aria-hidden='true'
    className='inline-block w-4 shrink-0 border-t-[3px]'
    style={{ borderColor: style.color, borderTopStyle: style.dashed ? 'dashed' : 'solid' }}
  />
);

interface Props {
  selected: SeriesConfig[];
  styles: Record<string, SeriesStyle>;
  onRemove: (name: string) => void;
}

// The chips double as the chart legend
const SeriesChips = ({ selected, styles, onRemove }: Props) => (
  <ul aria-label='Valda serier' className='contents'>
    {selected.map((config) => (
      <li
        key={config.name}
        className='flex min-w-0 max-w-full items-center gap-2 rounded-full border border-border bg-card py-1 pl-3 pr-1 text-sm'
      >
        <SeriesSwatch style={styles[config.name]} />
        <span className='truncate' title={config.displayName}>{config.displayName}</span>
        {config.unit && <span className='shrink-0 text-xs text-muted-foreground'>{config.unit}</span>}
        <button
          type='button'
          onClick={() => onRemove(config.name)}
          aria-label={`Ta bort ${config.displayName}`}
          className='shrink-0 rounded-full p-1 text-muted-foreground hover:bg-muted hover:text-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring'
        >
          <X className='h-3.5 w-3.5' />
        </button>
      </li>
    ))}
  </ul>
);

export default SeriesChips;
