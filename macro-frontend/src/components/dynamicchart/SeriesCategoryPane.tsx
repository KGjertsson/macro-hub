import { useId, useState } from 'react';
import { Checkbox } from '@/components/ui/checkbox';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group';
import { SeriesConfig } from '@/models/SeriesConfig';
import {
  buildPickerGroups,
  CheckboxListGroup,
  formatMaturity,
  MaturityGridGroup,
  VariantListGroup
} from '@/components/dynamicchart/seriesMetadata';

interface SelectionProps {
  isSelected: (name: string) => boolean;
  onToggle: (name: string) => void;
}

interface SeriesCheckboxProps extends SelectionProps {
  config: SeriesConfig;
  label: string;
}

const SeriesCheckbox = ({ config, label, isSelected, onToggle }: SeriesCheckboxProps) => {
  const id = useId();
  return (
    <div className='flex items-center gap-2 py-1'>
      <Checkbox id={id} checked={isSelected(config.name)} onCheckedChange={() => onToggle(config.name)} />
      <label htmlFor={id} className='cursor-pointer text-sm' title={config.displayName}>{label}</label>
    </div>
  );
};

const GroupHeading = ({ children }: { children: string }) => (
  <h3 className='mb-1 text-xs font-semibold uppercase tracking-wide text-muted-foreground'>{children}</h3>
);

const CheckboxList = ({ group, ...selection }: { group: CheckboxListGroup } & SelectionProps) => (
  <section>
    <GroupHeading>{group.subcategory}</GroupHeading>
    {group.items.map(({ label, config }) => (
      <SeriesCheckbox key={config.name} config={config} label={label} {...selection} />
    ))}
  </section>
);

const MaturityGrid = ({ group, isSelected, onToggle }: { group: MaturityGridGroup } & SelectionProps) => (
  <section>
    <GroupHeading>{group.subcategory}</GroupHeading>
    <table className='text-sm'>
      <thead>
        <tr>
          <th scope='col'><span className='sr-only'>Land</span></th>
          {group.maturities.map((months) => (
            <th key={months} scope='col' className='px-2 pb-1 text-center text-xs font-medium text-muted-foreground'>
              {formatMaturity(months)}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {group.rows.map((row) => (
          <tr key={row.country}>
            <th scope='row' className='py-1 pr-3 text-left font-normal'>{row.country}</th>
            {row.cells.map((config, index) => (
              <td key={group.maturities[index]} className='px-2 py-1 text-center'>
                {config && (
                  <Checkbox
                    checked={isSelected(config.name)}
                    onCheckedChange={() => onToggle(config.name)}
                    aria-label={config.displayName}
                    title={config.displayName}
                  />
                )}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  </section>
);

const VariantList = ({ group, isSelected, onToggle }: { group: VariantListGroup } & SelectionProps) => {
  const [variant, setVariant] = useState(group.variants[0].subcategory);
  return (
    <section>
      <ToggleGroup
        type='single'
        variant='outline'
        size='sm'
        value={variant}
        onValueChange={(value) => value && setVariant(value)}
        aria-label='Mått'
        className='mb-2 justify-start'
      >
        {group.variants.map((v) => {
          const selectedCount = group.rows.filter((row) => isSelected(row.configs[v.subcategory].name)).length;
          return (
            <ToggleGroupItem key={v.subcategory} value={v.subcategory} title={v.subcategory}>
              {v.label}
              {selectedCount > 0 && <span className='ml-1 text-xs opacity-70'>({selectedCount})</span>}
            </ToggleGroupItem>
          );
        })}
      </ToggleGroup>
      {group.rows.map((row) => (
        <SeriesCheckbox
          key={row.country}
          config={row.configs[variant]}
          label={row.labels[variant]}
          isSelected={isSelected}
          onToggle={onToggle}
        />
      ))}
    </section>
  );
};

interface Props extends SelectionProps {
  configs: SeriesConfig[];
}

const SeriesCategoryPane = ({ configs, ...selection }: Props) => (
  <div className='flex flex-col gap-4'>
    {buildPickerGroups(configs).map((group) => {
      switch (group.kind) {
        case 'grid':
          return <MaturityGrid key={group.subcategory} group={group} {...selection} />;
        case 'variants':
          return <VariantList key='variants' group={group} {...selection} />;
        default:
          return <CheckboxList key={group.subcategory} group={group} {...selection} />;
      }
    })}
  </div>
);

export default SeriesCategoryPane;
