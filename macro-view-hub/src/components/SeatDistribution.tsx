import { parties } from '@/data/parliamentData';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';

const SeatDistribution = () => {
  const totalSeats = 349;

  // Sort parties by seats for distribution
  const sortedParties = [...parties].sort((a, b) => b.seats - a.seats);

  // Arrange seats in semicircle rows (hemicycle)
  const arrangeInHemicycle = () => {
    const rows = 8;
    const centerX = 200;
    const centerY = 190;
    const minRadius = 50;
    const maxRadius = 170;
    const radiusStep = (maxRadius - minRadius) / (rows - 1);

    // Calculate seats per row (more seats in outer rows)
    const seatsPerRow: number[] = [];
    let totalCapacity = 0;

    for (let r = 0; r < rows; r++) {
      const radius = minRadius + r * radiusStep;
      const circumference = Math.PI * radius;
      const seatSize = 8;
      const seatsInRow = Math.floor(circumference / (seatSize + 2));
      seatsPerRow.push(seatsInRow);
      totalCapacity += seatsInRow;
    }

    // Scale to fit exactly totalSeats
    const scale = totalSeats / totalCapacity;
    const adjustedSeatsPerRow = seatsPerRow.map(s => Math.round(s * scale));

    // Adjust to match exactly totalSeats
    let diff = totalSeats - adjustedSeatsPerRow.reduce((a, b) => a + b, 0);
    for (let i = rows - 1; diff !== 0 && i >= 0; i--) {
      if (diff > 0) {
        adjustedSeatsPerRow[i]++;
        diff--;
      } else {
        adjustedSeatsPerRow[i]--;
        diff++;
      }
    }

    // First, generate all seat positions with their angles
    const allPositions: { x: number; y: number; angle: number; row: number }[] = [];

    for (let r = 0; r < rows; r++) {
      const radius = minRadius + r * radiusStep;
      const seatsInRow = adjustedSeatsPerRow[r];

      for (let s = 0; s < seatsInRow; s++) {
        const angleRange = Math.PI;
        const startAngle = Math.PI;
        const angle = startAngle + (s + 0.5) * (angleRange / seatsInRow);

        const x = centerX + radius * Math.cos(angle);
        const y = centerY + radius * Math.sin(angle);

        allPositions.push({ x, y, angle, row: r });
      }
    }

    // Sort all positions by angle (left to right)
    allPositions.sort((a, b) => a.angle - b.angle);

    // Now assign parties to seats from left to right
    const positioned: { x: number; y: number; color: string; partyName: string; partyShort: string }[] = [];
    let seatIndex = 0;

    for (const party of sortedParties) {
      for (let i = 0; i < party.seats && seatIndex < allPositions.length; i++) {
        const pos = allPositions[seatIndex];
        positioned.push({
          x: pos.x,
          y: pos.y,
          color: party.color,
          partyName: party.name,
          partyShort: party.shortName
        });
        seatIndex++;
      }
    }

    return positioned;
  };

  const positionedSeats = arrangeInHemicycle();

  return (
    <Card className='glass-card'>
      <CardHeader className='text-center'>
        <CardTitle className='text-2xl font-display'>Riksdag Seat Distribution</CardTitle>
        <p className='text-muted-foreground'>{totalSeats} Total Seats</p>
      </CardHeader>
      <CardContent>
        <div className='flex justify-center'>
          <TooltipProvider delayDuration={0}>
            <svg
              viewBox='0 0 400 220'
              className='w-full max-w-lg'
              style={{ overflow: 'visible' }}
            >
              {/* Individual seats */}
              {positionedSeats.map((seat, index) => (
                <Tooltip key={index}>
                  <TooltipTrigger asChild>
                    <circle
                      cx={seat.x}
                      cy={seat.y}
                      r={3.5}
                      fill={seat.color}
                      className='transition-all duration-200 cursor-pointer hover:opacity-80'
                      style={{
                        filter: 'drop-shadow(0 1px 2px rgba(0,0,0,0.2))'
                      }}
                    />
                  </TooltipTrigger>
                  <TooltipContent
                    side='top'
                    className='flex items-center gap-2'
                  >
                    <div
                      className='w-3 h-3 rounded-full'
                      style={{ backgroundColor: seat.color }}
                    />
                    <span className='font-medium'>{seat.partyName}</span>
                    <span className='text-muted-foreground'>({seat.partyShort})</span>
                  </TooltipContent>
                </Tooltip>
              ))}

              {/* Center podium decoration */}
              <rect
                x={185}
                y={195}
                width={30}
                height={15}
                rx={3}
                fill='hsl(var(--muted))'
                stroke='hsl(var(--border))'
                strokeWidth={1}
              />
            </svg>
          </TooltipProvider>
        </div>

        {/* Legend */}
        <div className='grid grid-cols-2 md:grid-cols-4 gap-4 mt-8 pt-6 border-t border-border/50'>
          {sortedParties.map(party => (
            <div key={party.id} className='flex items-center gap-2 group cursor-pointer'>
              <div
                className='w-4 h-4 rounded-full flex-shrink-0 transition-transform group-hover:scale-125'
                style={{ backgroundColor: party.color }}
              />
              <div className='min-w-0'>
                <p className='text-sm font-medium truncate'>{party.shortName}</p>
                <p className='text-xs text-muted-foreground'>
                  {party.seats} seats ({((party.seats / totalSeats) * 100).toFixed(1)}%)
                </p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default SeatDistribution;
