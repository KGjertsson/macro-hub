import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { AreaProperties, AreaLevel, GeoVersion, geoVersionLabel, RegionalIndicator } from '@/models/RegionalStatistics';
import { formatNumber, formatValue } from './classification';

interface AreaPanelProps {
  area: AreaProperties;
  kommunName: string | undefined;
  level: AreaLevel;
  indicator: RegionalIndicator;
  year: number;
  geoVersion: GeoVersion;
  values: Record<string, number>;
  kommunByCode: Map<string, string>;
  onClose: () => void;
}

// 1 is the highest value; ties share the better rank
const rankOf = (value: number, others: number[]) => 1 + others.filter(other => other > value).length;

const Rank = ({ label, rank, total }: { label: string; rank: number; total: number }) => (
  <div className="rounded-xl bg-muted/50 p-4">
    <div className="text-xs text-muted-foreground">{label}</div>
    <div className="mt-1 text-2xl font-bold text-foreground tabular-nums">
      {rank}
      <span className="text-base font-normal text-muted-foreground"> av {formatNumber(total, 0)}</span>
    </div>
  </div>
);

const AreaPanel = ({ area, kommunName, level, indicator, year, geoVersion, values, kommunByCode, onClose }: AreaPanelProps) => {
  const value = values[area.code];
  const all = Object.values(values);
  const inKommun = Object.entries(values)
    .filter(([code]) => kommunByCode.get(code) === area.kommunkod)
    .map(([, v]) => v);

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <div className="text-xs font-medium uppercase tracking-wide text-primary">{level}</div>
          <h2 className="text-2xl font-bold text-foreground break-words">{area.name}</h2>
          <p className="text-sm text-muted-foreground">
            Kommun: {kommunName ?? area.kommunkod} · {area.code}
          </p>
        </div>
        <Button variant="ghost" size="icon" onClick={onClose} aria-label="Avmarkera område">
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div>
        <div className="text-sm text-muted-foreground">
          {indicator.label} {year}
        </div>
        <div className="text-4xl font-bold text-foreground">
          {value === undefined ? 'Saknar data' : formatValue(value, indicator)}
        </div>
      </div>

      {value !== undefined && (
        <div className="grid grid-cols-2 gap-3">
          <Rank label={`Plats i ${kommunName ?? 'kommunen'}`} rank={rankOf(value, inKommun)} total={inKommun.length} />
          <Rank label="Plats i Sverige" rank={rankOf(value, all)} total={all.length} />
        </div>
      )}
      {value !== undefined && (
        <p className="text-xs text-muted-foreground">
          Plats 1 har det högsta värdet bland områden med data. Indelning: {geoVersionLabel(geoVersion)}.
        </p>
      )}
    </div>
  );
};

export default AreaPanel;
