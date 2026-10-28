import { useState } from 'react';
import { Check, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Command, CommandEmpty, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import { Drawer, DrawerContent, DrawerTitle, DrawerTrigger } from '@/components/ui/drawer';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { useIsMobile } from '@/hooks/use-mobile';
import { cn } from '@/lib/utils';
import { SeriesConfig } from '@/models/SeriesConfig';
import SeriesCategoryPane from '@/components/dynamicchart/SeriesCategoryPane';
import { matchesAllWords } from '@/components/dynamicchart/seriesMetadata';

interface Props {
  configs: SeriesConfig[];
  selectedNames: string[];
  onToggle: (name: string) => void;
}

interface ContentProps extends Props {
  stacked: boolean;
}

const PickerContent = ({ configs, selectedNames, onToggle, stacked }: ContentProps) => {
  const [search, setSearch] = useState('');
  const categories = Array.from(new Set(configs.map((c) => c.category)));
  const [activeCategory, setActiveCategory] = useState(categories[0]);
  const isSelected = (name: string) => selectedNames.includes(name);

  return (
    <div className='flex min-h-0 flex-col'>
      {/* Only the search sits inside cmdk, which would otherwise swallow Enter on the checkboxes below */}
      <Command
        filter={(value, query, keywords) => (matchesAllWords([value, ...(keywords ?? [])].join(' '), query) ? 1 : 0)}
        className='h-auto rounded-none border-b border-border'
      >
        <CommandInput value={search} onValueChange={setSearch} placeholder='Sök serie, t.ex. "tysk 10"…' />
        {search && (
          <CommandList className={stacked ? 'max-h-[60vh]' : 'max-h-[420px]'}>
            <CommandEmpty>Ingen serie matchar sökningen.</CommandEmpty>
            {configs.map((config) => (
              <CommandItem
                key={config.name}
                value={config.displayName}
                onSelect={() => onToggle(config.name)}
                className='gap-2'
              >
                <Check className={cn('h-4 w-4 shrink-0', isSelected(config.name) ? 'opacity-100' : 'opacity-0')} />
                <span className='flex-1'>{config.displayName}</span>
                <span className='text-xs text-muted-foreground'>{config.category}</span>
              </CommandItem>
            ))}
          </CommandList>
        )}
      </Command>
      {!search && (
        <div className={cn('flex min-h-0', stacked ? 'flex-col' : 'h-[420px]')}>
          <nav
            aria-label='Kategorier'
            className={cn(
              'flex shrink-0 gap-1 p-2',
              stacked ? 'flex-wrap border-b border-border' : 'w-48 flex-col border-r border-border'
            )}
          >
            {categories.map((category) => {
              const inCategory = configs.filter((c) => c.category === category);
              const selectedCount = inCategory.filter((c) => isSelected(c.name)).length;
              return (
                <button
                  type='button'
                  key={category}
                  onClick={() => setActiveCategory(category)}
                  aria-pressed={category === activeCategory}
                  className={cn(
                    'flex items-center justify-between gap-2 rounded-md px-3 py-1.5 text-left text-sm transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring',
                    category === activeCategory ? 'bg-muted text-foreground' : 'text-muted-foreground hover:bg-muted/50'
                  )}
                >
                  <span>{category}</span>
                  <span className='text-xs text-muted-foreground'>
                    {selectedCount > 0 ? `${selectedCount}/${inCategory.length}` : inCategory.length}
                  </span>
                </button>
              );
            })}
          </nav>
          <div className={cn('min-w-0 flex-1 overflow-auto p-4', stacked && 'max-h-[55vh]')}>
            <SeriesCategoryPane
              key={activeCategory}
              configs={configs.filter((c) => c.category === activeCategory)}
              isSelected={isSelected}
              onToggle={onToggle}
            />
          </div>
        </div>
      )}
    </div>
  );
};

const trigger = (
  <Button type='button' variant='outline' size='sm' className='rounded-full'>
    <Plus className='h-4 w-4' />
    Lägg till serie
  </Button>
);

// A popover on desktop, a bottom drawer on phones; both stay open while several series are toggled
const SeriesPicker = (props: Props) => {
  const isMobile = useIsMobile();

  if (isMobile) {
    return (
      <Drawer>
        <DrawerTrigger asChild>
          {trigger}
        </DrawerTrigger>
        <DrawerContent className='max-h-[90vh]' aria-describedby={undefined}>
          <DrawerTitle className='px-4 pb-2 pt-3 text-base'>Lägg till serie</DrawerTitle>
          <PickerContent {...props} stacked />
        </DrawerContent>
      </Drawer>
    );
  }

  return (
    <Popover>
      <PopoverTrigger asChild>
        {trigger}
      </PopoverTrigger>
      <PopoverContent align='start' className='w-[min(720px,calc(100vw-2rem))] overflow-hidden p-0'>
        <PickerContent {...props} stacked={false} />
      </PopoverContent>
    </Popover>
  );
};

export default SeriesPicker;
