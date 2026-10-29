import { RegionalIndicator } from '@/models/RegionalStatistics';
import { Classification, formatNumber, NO_DATA_COLOR } from './classification';

interface MapLegendProps {
  classification: Classification;
  indicator: RegionalIndicator;
  year: number;
}

const Swatch = ({ color, faded }: { color: string; faded?: boolean }) => (
  <span className="inline-block h-3 w-5 shrink-0 rounded-sm" style={{ backgroundColor: color, opacity: faded ? 0.5 : 1 }} />
);

const MapLegend = ({ classification, indicator, year }: MapLegendProps) => {
  const format = (value: number) => formatNumber(value, indicator.decimals);

  return (
    <div className="rounded-lg border border-border bg-background/90 p-3 text-xs text-foreground shadow-lg backdrop-blur">
      <div className="mb-2 font-medium">
        {indicator.label} {year}
        {indicator.unit !== 'antal' && <span className="font-normal text-muted-foreground"> ({indicator.unit})</span>}
      </div>
      <ul className="space-y-1 tabular-nums">
        {/* Highest class on top */}
        {[...classification.classes].reverse().map(c => (
          <li key={`${c.min}-${c.max}`} className="flex items-center gap-2">
            <Swatch color={c.color} />
            {c.min === c.max ? format(c.min) : `${format(c.min)} – ${format(c.max)}`}
          </li>
        ))}
        <li className="flex items-center gap-2 text-muted-foreground">
          <Swatch color={NO_DATA_COLOR} faded />
          Saknar data
        </li>
      </ul>
    </div>
  );
};

export default MapLegend;
