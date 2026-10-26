import { ReactNode, useMemo, useState } from 'react';
import { Check, ChevronDown, ChevronsUpDown, Users } from 'lucide-react';
import ChoroplethMap from '@/components/ChoroplethMap';
import { Button } from '@/components/ui/button';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { useLocalElectionDetail, useLocalElections } from '@/api/localElections';
import { LocalElectionDetail, LocalElectionKind, LocalElectionSummary, LocalPartyResult } from '@/models/LocalElection';
import { cn, textColorOn } from '@/lib/utils';

export interface LocalElectionLabels {
  areaSingular: string;
  searchPlaceholder: string;
  pickTitle: string;
  pickHint: string;
  governanceTitle: (name: string) => string;
}

interface LocalElectionViewProps {
  kind: LocalElectionKind;
  topoUrl: string;
  objectName: string;
  icon: ReactNode;
  labels: LocalElectionLabels;
}

const formatPercent = (value: number) =>
  `${value.toLocaleString('sv-SE', { minimumFractionDigits: 1, maximumFractionDigits: 2 })} %`;

const AreaSearch = ({ areas, selectedCode, onSelect, labels }: {
  areas: LocalElectionSummary[];
  selectedCode: string | null;
  onSelect: (code: string) => void;
  labels: LocalElectionLabels;
}) => {
  const [open, setOpen] = useState(false);
  const selected = areas.find(a => a.code === selectedCode);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" role="combobox" aria-expanded={open} className="w-full justify-between">
          {selected?.name ?? labels.searchPlaceholder}
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[var(--radix-popover-trigger-width)] p-0">
        <Command>
          <CommandInput placeholder={labels.searchPlaceholder} />
          <CommandList>
            <CommandEmpty>Ingen träff.</CommandEmpty>
            <CommandGroup>
              {areas.map(area => (
                <CommandItem
                  key={area.code}
                  value={`${area.name} ${area.code}`}
                  onSelect={() => {
                    onSelect(area.code);
                    setOpen(false);
                  }}
                >
                  <Check className={cn('mr-2 h-4 w-4', area.code === selectedCode ? 'opacity-100' : 'opacity-0')} />
                  {area.name}
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
};

const PartyRow = ({ party, maxShare }: { party: LocalPartyResult; maxShare: number }) => {
  // Some local lists have no abbreviation in Valmyndigheten's files, so the full name is shown instead
  const abbreviation = party.abbreviation?.trim();

  return (
    <div className="space-y-1">
      <div className="flex justify-between gap-4 text-sm">
        <span className="font-medium text-foreground truncate" title={party.name}>
          {abbreviation ? (
            <>
              {abbreviation}
              <span className="ml-2 font-normal text-muted-foreground hidden sm:inline">{party.name}</span>
            </>
          ) : (
            party.name
          )}
        </span>
        <span className="text-muted-foreground whitespace-nowrap">
          {formatPercent(party.voteShare)} · {party.seats} mandat
        </span>
      </div>
      <div className="h-3 bg-muted rounded-full overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-500"
          style={{ width: `${(party.voteShare / maxShare) * 100}%`, backgroundColor: party.color }}
        />
      </div>
    </div>
  );
};

const ResultPanel = ({ summary }: { summary: LocalElectionSummary }) => {
  const [showOthers, setShowOthers] = useState(false);
  const maxShare = Math.max(...summary.parties.map(p => p.voteShare), 1);
  // Parties without seats are folded away; a kommun often has a dozen lists with a fraction of a percent
  const seated = summary.parties.filter(p => p.seats > 0);
  const others = summary.parties.filter(p => p.seats === 0);

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <div className="w-4 h-4 rounded-full" style={{ backgroundColor: summary.largestPartyColor }} />
        <h2 className="text-2xl font-bold text-foreground">{summary.name}</h2>
      </div>

      <div className="grid grid-cols-3 gap-3 text-center">
        <div>
          <div className="text-xl font-bold text-foreground">{summary.largestParty ?? '–'}</div>
          <div className="text-xs text-muted-foreground">Största parti</div>
        </div>
        <div>
          <div className="text-xl font-bold text-foreground">{summary.totalSeats}</div>
          <div className="text-xs text-muted-foreground">Mandat</div>
        </div>
        <div>
          <div className="text-xl font-bold text-foreground">
            {summary.turnout !== null ? formatPercent(summary.turnout) : '–'}
          </div>
          <div className="text-xs text-muted-foreground">Valdeltagande</div>
        </div>
      </div>

      <div className="space-y-3">
        <h3 className="text-lg font-semibold text-foreground">Partifördelning {summary.year}</h3>
        {seated.map(party => <PartyRow key={party.code} party={party} maxShare={maxShare} />)}
        {others.length > 0 && (
          <Collapsible open={showOthers} onOpenChange={setShowOthers} className="space-y-3">
            <CollapsibleTrigger asChild>
              <button className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground transition-colors">
                {showOthers ? 'Dölj' : 'Visa'} partier utan mandat ({others.length},{' '}
                {formatPercent(others.reduce((sum, p) => sum + p.voteShare, 0))})
                <ChevronDown className={cn('h-4 w-4 transition-transform', showOthers && 'rotate-180')} />
              </button>
            </CollapsibleTrigger>
            <CollapsibleContent className="space-y-3">
              {others.map(party => <PartyRow key={party.code} party={party} maxShare={maxShare} />)}
            </CollapsibleContent>
          </Collapsible>
        )}
      </div>
    </div>
  );
};

const PartyChip = ({ name, color }: { name: string; color: string }) => (
  <span className="px-3 py-1.5 rounded-full text-sm font-medium" style={{ backgroundColor: color, color: textColorOn(color) }}>
    {name}
  </span>
);

const GovernanceSection = ({ detail, title }: { detail: LocalElectionDetail; title: string }) => {
  const governance = detail.governance;
  if (!governance) {
    return null;
  }
  const partyByAbbr = new Map(detail.summary.parties.map(p => [p.abbreviation, p]));
  const share = (abbrs: string[]) => abbrs.reduce((sum, abbr) => sum + (partyByAbbr.get(abbr)?.voteShare ?? 0), 0);
  const chip = (abbr: string) => {
    const party = partyByAbbr.get(abbr);
    return <PartyChip key={abbr} name={party?.name ?? abbr} color={party?.color ?? '#9CA3AF'} />;
  };

  return (
    <div className="mt-12 bg-card rounded-2xl border border-border p-6 shadow-lg">
      <div className="flex items-center gap-3 mb-4">
        <Users className="h-6 w-6 text-primary" />
        <h2 className="text-2xl font-bold text-foreground">{title}</h2>
      </div>
      <div className="grid md:grid-cols-2 gap-6">
        <div className="space-y-4">
          <div className="flex items-center gap-3 flex-wrap">
            <div className="inline-flex items-center px-3 py-1 rounded-full bg-primary/10 text-primary text-sm font-medium">
              {governance.coalitionName}
            </div>
            <div className="inline-flex items-center px-3 py-1 rounded-full bg-muted text-foreground text-sm font-semibold">
              {formatPercent(share([...governance.governingParties, ...governance.supportParties]))} av rösterna
            </div>
          </div>
          {governance.description && (
            <p className="text-muted-foreground leading-relaxed">{governance.description}</p>
          )}
        </div>
        <div className="space-y-3">
          <h3 className="text-lg font-semibold text-foreground">Styrande partier</h3>
          <div className="flex flex-wrap gap-2">{governance.governingParties.map(chip)}</div>
          {governance.supportParties.length > 0 && (
            <>
              <h3 className="text-lg font-semibold text-foreground mt-4">Stödpartier</h3>
              <div className="flex flex-wrap gap-2">{governance.supportParties.map(chip)}</div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

const ElectedSection = ({ detail }: { detail: LocalElectionDetail }) => {
  const [open, setOpen] = useState(false);
  const total = detail.elected.reduce((sum, party) => sum + party.members.length, 0);
  if (total === 0) {
    return null;
  }

  return (
    <Collapsible open={open} onOpenChange={setOpen} className="mt-8 bg-card rounded-2xl border border-border p-6 shadow-lg">
      <CollapsibleTrigger asChild>
        <button className="flex w-full items-center justify-between text-left">
          <span className="text-2xl font-bold text-foreground">Valda ledamöter ({total})</span>
          <ChevronDown className={cn('h-5 w-5 text-muted-foreground transition-transform', open && 'rotate-180')} />
        </button>
      </CollapsibleTrigger>
      <CollapsibleContent className="mt-6 grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {detail.elected.map(party => (
          <div key={party.partyCode} className="space-y-2">
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full" style={{ backgroundColor: party.color }} />
              <h3 className="font-semibold text-foreground">
                {party.name} ({party.members.length})
              </h3>
            </div>
            <ul className="space-y-1 text-sm text-muted-foreground">
              {party.members.map(member => (
                <li key={`${member.order}-${member.name}`} title={member.electedOn ?? undefined}>
                  {member.name}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </CollapsibleContent>
    </Collapsible>
  );
};

const LocalElectionView = ({ kind, topoUrl, objectName, icon, labels }: LocalElectionViewProps) => {
  const [selectedCode, setSelectedCode] = useState<string | null>(null);
  const { data: areas, isLoading, isError } = useLocalElections(kind);
  const { data: detail } = useLocalElectionDetail(kind, selectedCode);

  const colorByCode = useMemo(
    () => Object.fromEntries((areas ?? []).map(a => [a.code, a.largestPartyColor])),
    [areas],
  );
  const nameByCode = useMemo(
    () => Object.fromEntries((areas ?? []).map(a => [a.code, a.name])),
    [areas],
  );
  const selected = areas?.find(a => a.code === selectedCode);

  if (isError) {
    return <p className="text-center text-muted-foreground">Valresultaten kunde inte hämtas.</p>;
  }
  if (!isLoading && areas?.length === 0) {
    return <p className="text-center text-muted-foreground">Inga valresultat har hämtats ännu.</p>;
  }

  return (
    <>
      <div className="grid lg:grid-cols-2 gap-8 items-start">
        <div className="bg-card rounded-2xl border border-border p-6 shadow-lg space-y-4">
          <AreaSearch areas={areas ?? []} selectedCode={selectedCode} onSelect={setSelectedCode} labels={labels} />
          <ChoroplethMap
            topoUrl={topoUrl}
            objectName={objectName}
            colorByCode={colorByCode}
            nameByCode={nameByCode}
            selectedCode={selectedCode}
            onSelect={setSelectedCode}
            ariaLabel={`Karta över Sverige färgad efter största parti per ${labels.areaSingular}`}
          />
        </div>

        <div className="bg-card rounded-2xl border border-border p-6 shadow-lg">
          {selected ? (
            <ResultPanel key={selected.code} summary={selected} />
          ) : (
            <div className="flex flex-col items-center justify-center h-full min-h-[400px] text-center">
              <div className="text-muted-foreground/50 mb-4">{icon}</div>
              <h2 className="text-xl font-semibold text-foreground mb-2">{labels.pickTitle}</h2>
              <p className="text-muted-foreground max-w-sm">{labels.pickHint}</p>
            </div>
          )}
          <p className="mt-6 text-xs text-muted-foreground">Källa: Valmyndigheten</p>
        </div>
      </div>

      {selected && detail && (
        <>
          <GovernanceSection detail={detail} title={labels.governanceTitle(selected.name)} />
          <ElectedSection key={selected.code} detail={detail} />
        </>
      )}
    </>
  );
};

export default LocalElectionView;
