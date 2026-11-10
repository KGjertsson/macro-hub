import { useState } from 'react';
import { createPortal } from 'react-dom';
import {
  Announcements,
  closestCenter,
  CollisionDetection,
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  KeyboardCoordinateGetter,
  KeyboardSensor,
  Modifier,
  PointerSensor,
  pointerWithin,
  TouchSensor,
  useDraggable,
  useDroppable,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import { seatLabel } from '@/api/parliament';
import {
  BAR_IDS,
  BarId,
  hasMajority,
  majorityOf,
  moveParty,
  NO_PLACEMENTS,
  reconcilePlacements,
  StackSegment,
  stackSegments,
  sumSeats,
} from '@/components/coalitionPlacements';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { cn, textColorOn } from '@/lib/utils';
import { PartySeats } from '@/models/Parliament';

const PLOT_HEIGHT_PX = 400;
const MIN_LABELLED_SEGMENT_PX = 18;
const ACTIVATION_DISTANCE_PX = 4;

const BAR_NAMES: Record<BarId, string> = {
  a: 'Konstellation A',
  b: 'Konstellation B',
  outside: 'Utanför',
};

const SCREEN_READER_INSTRUCTIONS = {
  draggable:
    'Tryck på mellanslag eller Enter för att lyfta partiet. Flytta det mellan staplarna med vänster- och högerpil. ' +
    'Släpp med mellanslag eller Enter och avbryt med Escape.',
};

const collisionDetection: CollisionDetection = args =>
  args.pointerCoordinates ? pointerWithin(args) : closestCenter(args);

const BAR_STEP_BY_ARROW: Record<string, number> = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: 0, ArrowDown: 0 };

const moveBetweenBars: KeyboardCoordinateGetter = (event, { context, currentCoordinates }) => {
  const step = BAR_STEP_BY_ARROW[event.code];
  if (step === undefined) return undefined;
  event.preventDefault();
  const { collisionRect, droppableRects, over } = context;
  if (step === 0 || !collisionRect || !over) return undefined;
  const nextBar = BAR_IDS[BAR_IDS.indexOf(over.id as BarId) + step];
  const nextRect = nextBar && droppableRects.get(nextBar);
  return nextRect ? { x: nextRect.left + (nextRect.width - collisionRect.width) / 2, y: currentCoordinates.y } : undefined;
};

const centreOverlayOnPointer: Modifier = ({ activatorEvent, activeNodeRect, overlayNodeRect, transform }) => {
  if (!(activatorEvent instanceof MouseEvent) || !activeNodeRect || !overlayNodeRect) return transform;
  return {
    ...transform,
    x: transform.x + activatorEvent.clientX - activeNodeRect.left - overlayNodeRect.width / 2,
    y: transform.y + activatorEvent.clientY - activeNodeRect.top - overlayNodeRect.height / 2,
  };
};

const draggedCode = (data: { current?: Record<string, unknown> }) => data.current?.code as string | undefined;

const PartyChipContent = ({ party }: { party: PartySeats }) => (
  <>
    <span className='h-2.5 w-2.5 shrink-0 rounded-full' style={{ backgroundColor: party.color }} />
    <span className='min-w-0 flex-1 truncate text-left font-medium'>{seatLabel(party)}</span>
    <span className='tabular-nums text-muted-foreground'>{party.seats}</span>
  </>
);

const chipClassName = 'flex min-h-9 w-full items-center gap-1 rounded-md border border-border/50 bg-card px-1.5 text-xs sm:px-2';

const PartyChip = ({ party, isBeingDragged }: { party: PartySeats; isBeingDragged: boolean }) => {
  const { attributes, listeners, setNodeRef } = useDraggable({
    id: `chip:${party.code}`,
    data: { code: party.code },
    attributes: { roleDescription: 'flyttbart parti' },
  });
  return (
    <button
      ref={setNodeRef}
      type='button'
      aria-label={`${party.name}, ${party.seats} mandat`}
      className={cn(chipClassName, 'cursor-grab touch-none', isBeingDragged && 'opacity-40')}
      {...listeners}
      {...attributes}
    >
      <PartyChipContent party={party} />
    </button>
  );
};

// One seat is about a pixel tall, too small to grab or to reach with a screen reader, so the chips are the route that always works and the segments stay out of the tab order
const PartySegment = ({
  segment: { party, seatsBelow },
  totalSeats,
  isBeingDragged,
}: {
  segment: StackSegment;
  totalSeats: number;
  isBeingDragged: boolean;
}) => {
  const { listeners, setNodeRef } = useDraggable({ id: `segment:${party.code}`, data: { code: party.code } });
  const isLabelled = (party.seats / totalSeats) * PLOT_HEIGHT_PX >= MIN_LABELLED_SEGMENT_PX;
  return (
    <Tooltip delayDuration={0}>
      <TooltipTrigger asChild>
        <div
          ref={setNodeRef}
          aria-hidden
          className={cn('absolute inset-x-0 cursor-grab touch-none', isBeingDragged && 'opacity-40')}
          style={{
            bottom: `${(seatsBelow / totalSeats) * 100}%`,
            height: `${(party.seats / totalSeats) * 100}%`,
          }}
          {...listeners}
        >
          {/* The gap is taken from the bottom, so that the top of a stack stays exact against the majority line */}
          <div
            className='absolute inset-x-0 top-0 flex items-center justify-center rounded-[2px] text-xs font-semibold'
            style={{
              bottom: seatsBelow > 0 ? 'min(2px, 35%)' : 0,
              backgroundColor: party.color,
              color: textColorOn(party.color),
            }}
          >
            {isLabelled && `${seatLabel(party)} ${party.seats}`}
          </div>
        </div>
      </TooltipTrigger>
      <TooltipContent side='top' className='flex items-center gap-2'>
        <span className='h-3 w-3 rounded-full' style={{ backgroundColor: party.color }} />
        <span className='font-medium'>{party.name}</span>
        <span className='text-muted-foreground'>{party.seats} mandat</span>
      </TooltipContent>
    </Tooltip>
  );
};

const SeatBar = ({
  bar,
  parties,
  segments,
  totalSeats,
  activeCode,
}: {
  bar: BarId;
  parties: readonly PartySeats[];
  segments: readonly StackSegment[];
  totalSeats: number;
  activeCode: string | undefined;
}) => {
  const { isOver, setNodeRef } = useDroppable({ id: bar });
  const seats = sumSeats(parties);
  return (
    <div ref={setNodeRef} className={cn('row-span-3 grid min-w-0 grid-rows-subgrid rounded-lg transition-colors', isOver && 'bg-muted/60')}>
      <div className='text-center'>
        <h3 className='text-xs font-semibold sm:text-sm'>{BAR_NAMES[bar]}</h3>
        <p>
          <span className='text-xl font-semibold tabular-nums'>{seats}</span>{' '}
          <span className='text-xs text-muted-foreground'>mandat</span>
        </p>
        <p className={cn('text-xs', hasMajority(seats, totalSeats) ? 'font-semibold' : 'text-muted-foreground')}>
          {hasMajority(seats, totalSeats) ? 'Majoritet' : 'Ingen majoritet'}
        </p>
      </div>
      <div className='relative shadow-[0_1px_0_hsl(var(--border))]' style={{ height: PLOT_HEIGHT_PX }}>
        <div className='absolute inset-y-0 inset-x-0 mx-auto max-w-32'>
          {segments.map(segment => (
            <PartySegment
              key={segment.party.code}
              segment={segment}
              totalSeats={totalSeats}
              isBeingDragged={activeCode === segment.party.code}
            />
          ))}
        </div>
      </div>
      <div className='grid grid-cols-1 content-start gap-1 lg:grid-cols-2'>
        {parties.map(party => (
          <PartyChip key={party.code} party={party} isBeingDragged={activeCode === party.code} />
        ))}
        {parties.length === 0 && <p className='col-span-full py-2 text-center text-xs text-muted-foreground'>Dra partier hit</p>}
      </div>
    </div>
  );
};

const CoalitionBuilder = ({ parties }: { parties: PartySeats[] }) => {
  const [storedPlacements, setPlacements] = useState(NO_PLACEMENTS);
  const [activeCode, setActiveCode] = useState<string>();
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: ACTIVATION_DISTANCE_PX } }),
    useSensor(TouchSensor, { activationConstraint: { distance: ACTIVATION_DISTANCE_PX } }),
    useSensor(KeyboardSensor, { coordinateGetter: moveBetweenBars }),
  );

  const seated = parties.filter(party => party.seats > 0);
  const placements = reconcilePlacements(storedPlacements, seated.map(party => party.code));
  if (placements !== storedPlacements) setPlacements(placements);

  const totalSeats = sumSeats(seated);
  const activeParty = seated.find(party => party.code === activeCode);
  const partyName = (data: { current?: Record<string, unknown> }) =>
    seated.find(party => party.code === draggedCode(data))?.name ?? 'Partiet';

  const announcements: Announcements = {
    onDragStart: ({ active }) => `${partyName(active.data)} har lyfts.`,
    onDragOver: ({ active, over }) =>
      over ? `${partyName(active.data)} är över ${BAR_NAMES[over.id as BarId]}.` : `${partyName(active.data)} är inte över någon stapel.`,
    onDragEnd: ({ active, over }) =>
      over ? `${partyName(active.data)} släpptes i ${BAR_NAMES[over.id as BarId]}.` : `${partyName(active.data)} släpptes utanför staplarna.`,
    onDragCancel: ({ active }) => `Flytten av ${partyName(active.data)} avbröts.`,
  };

  const handleDragStart = ({ active }: DragStartEvent) => setActiveCode(draggedCode(active.data));

  const handleDragEnd = ({ active, over }: DragEndEvent) => {
    setActiveCode(undefined);
    const code = draggedCode(active.data);
    if (code && over) setPlacements(current => moveParty(current, code, over.id as BarId));
  };

  if (totalSeats === 0) return null;

  return (
    <Card className='glass-card'>
      <CardHeader className='flex-row flex-wrap items-start justify-between gap-4 space-y-0'>
        <div>
          <CardTitle className='text-2xl font-display'>Prova konstellationer</CardTitle>
          <p className='mt-1 text-muted-foreground'>Dra partierna mellan staplarna och se hur många mandat de får tillsammans.</p>
        </div>
        <Button variant='outline' size='sm' onClick={() => setPlacements(NO_PLACEMENTS)}>
          Återställ
        </Button>
      </CardHeader>
      <CardContent>
        <p className='mb-4 flex items-center gap-2 text-sm'>
          <span className='w-8 border-t border-dotted border-foreground' />
          Majoritet: {majorityOf(totalSeats)} mandat
        </p>
        <DndContext
          sensors={sensors}
          collisionDetection={collisionDetection}
          accessibility={{ announcements, screenReaderInstructions: SCREEN_READER_INSTRUCTIONS }}
          onDragStart={handleDragStart}
          onDragEnd={handleDragEnd}
          onDragCancel={() => setActiveCode(undefined)}
        >
          <div className='relative grid grid-cols-3 grid-rows-[auto_auto_auto] gap-x-2 gap-y-2 sm:gap-x-4'>
            {BAR_IDS.map(bar => (
              <SeatBar
                key={bar}
                bar={bar}
                parties={placements[bar].flatMap(code => seated.filter(party => party.code === code))}
                segments={stackSegments(placements[bar], seated)}
                totalSeats={totalSeats}
                activeCode={activeCode}
              />
            ))}
            {/* Absolutely positioned, the grid area of the bars' plot row is this element's containing block */}
            <div className='pointer-events-none absolute inset-0 z-10 col-span-full row-start-2 row-end-3'>
              <div
                className='absolute inset-x-0 translate-y-1/2 border-t border-dotted border-white mix-blend-difference'
                style={{ bottom: '50%' }}
              />
            </div>
          </div>
          {/* In the card, backdrop-filter would make the overlay's fixed position relative to the card rather than the viewport */}
          {createPortal(
            <DragOverlay dropAnimation={null} modifiers={[centreOverlayOnPointer]}>
              {activeParty && (
                <div className={cn(chipClassName, 'w-28 cursor-grabbing shadow-lg')}>
                  <PartyChipContent party={activeParty} />
                </div>
              )}
            </DragOverlay>,
            document.body,
          )}
        </DndContext>
      </CardContent>
    </Card>
  );
};

export default CoalitionBuilder;
