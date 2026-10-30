import {Info} from 'lucide-react';
import {Badge} from '@/components/ui/badge';
import {cn} from '@/lib/utils';

/** Marks placeholder content (invented texts) so it is not mistaken for real news. */
export const ExampleBadge = ({className}: { className?: string }) => (
    <Badge
        variant='outline'
        className={cn('border-warning/50 bg-warning/10 text-warning uppercase tracking-wide', className)}
    >
        Exempel
    </Badge>
);

export const ExampleNote = ({id, className}: { id?: string; className?: string }) => (
    <p id={id} className={cn('flex items-center gap-1.5 text-xs text-warning', className)}>
        <Info className='h-3.5 w-3.5 shrink-0' aria-hidden='true'/>
        <span>Exempelinnehåll – texterna är påhittade och visar hur sektionen kan se ut.</span>
    </p>
);
