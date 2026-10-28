import { Switch } from '@/components/ui/switch';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group';
import { SAMPLE_STRATEGY, TIME_FRAME } from '@/models/Constants';

const timeFrameOptions: { value: TIME_FRAME; label: string; title: string }[] = [
  { value: TIME_FRAME.OneMonth, label: '1M', title: 'En månad' },
  { value: TIME_FRAME.OneYear, label: '1Å', title: 'Ett år' },
  { value: TIME_FRAME.FiveYear, label: '5Å', title: 'Fem år' },
  { value: TIME_FRAME.TenYear, label: '10Å', title: 'Tio år' },
  { value: TIME_FRAME.All, label: 'Allt', title: 'Allt' }
];

const sampleStrategyOptions: { value: SAMPLE_STRATEGY; label: string }[] = [
  { value: SAMPLE_STRATEGY.Day, label: 'Dag' },
  { value: SAMPLE_STRATEGY.Month, label: 'Månad' },
  { value: SAMPLE_STRATEGY.Year, label: 'År' }
];

interface SegmentedProps<T extends number> {
  label: string;
  value: T;
  options: { value: T; label: string; title?: string }[];
  onChange: (value: T) => void;
}

const Segmented = <T extends number>({ label, value, options, onChange }: SegmentedProps<T>) => (
  <div className='flex items-center gap-2'>
    <span className='text-sm text-muted-foreground'>{label}</span>
    <ToggleGroup
      type='single'
      variant='outline'
      size='sm'
      aria-label={label}
      value={String(value)}
      // Radix lets the active item be clicked off; keep one option selected
      onValueChange={(next) => next && onChange(Number(next) as T)}
      className='gap-0'
    >
      {options.map((option) => (
        <ToggleGroupItem
          key={option.value}
          value={String(option.value)}
          title={option.title}
          className='rounded-none first:rounded-l-md last:rounded-r-md [&:not(:first-child)]:-ml-px'
        >
          {option.label}
        </ToggleGroupItem>
      ))}
    </ToggleGroup>
  </div>
);

interface Props {
  timeFrame: TIME_FRAME;
  sampleStrategy: SAMPLE_STRATEGY;
  indexed: boolean;
  onTimeFrameChange: (timeFrame: TIME_FRAME) => void;
  onSampleStrategyChange: (sampleStrategy: SAMPLE_STRATEGY) => void;
  onIndexedChange: (indexed: boolean) => void;
}

const ChartControls = ({
  timeFrame,
  sampleStrategy,
  indexed,
  onTimeFrameChange,
  onSampleStrategyChange,
  onIndexedChange
}: Props) => (
  <div className='flex flex-wrap items-center gap-x-6 gap-y-3'>
    <Segmented label='Tidsram' value={timeFrame} options={timeFrameOptions} onChange={onTimeFrameChange} />
    <Segmented
      label='Upplösning'
      value={sampleStrategy}
      options={sampleStrategyOptions}
      onChange={onSampleStrategyChange}
    />
    <label className='flex cursor-pointer items-center gap-2 text-sm text-muted-foreground'>
      <Switch checked={indexed} onCheckedChange={onIndexedChange} />
      Index = 100 vid start
    </label>
  </div>
);

export default ChartControls;
