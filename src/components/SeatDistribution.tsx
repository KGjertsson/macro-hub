import { parties } from '@/data/parliamentData';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

const SeatDistribution = () => {
  const totalSeats = 349;
  
  // Sort parties by seats for distribution
  const sortedParties = [...parties].sort((a, b) => b.seats - a.seats);
  
  // Generate individual seats with party colors
  const generateSeats = () => {
    const seats: { party: typeof parties[0]; row: number; angle: number }[] = [];
    
    // Create flat list of all seats with party info
    sortedParties.forEach(party => {
      for (let i = 0; i < party.seats; i++) {
        seats.push({ party, row: 0, angle: 0 });
      }
    });
    
    return seats;
  };

  const allSeats = generateSeats();
  
  // Arrange seats in semicircle rows (hemicycle)
  const arrangeInHemicycle = () => {
    const rows = 8; // Number of rows
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
    
    // Position each seat
    const positioned: { x: number; y: number; color: string; partyName: string; partyShort: string }[] = [];
    let seatIndex = 0;
    
    for (let r = 0; r < rows; r++) {
      const radius = minRadius + r * radiusStep;
      const seatsInRow = adjustedSeatsPerRow[r];
      
      for (let s = 0; s < seatsInRow && seatIndex < allSeats.length; s++) {
        // Angle from 180° to 360° (left to right semicircle)
        const angleRange = Math.PI;
        const startAngle = Math.PI;
        const angle = startAngle + (s + 0.5) * (angleRange / seatsInRow);
        
        const x = centerX + radius * Math.cos(angle);
        const y = centerY + radius * Math.sin(angle);
        
        positioned.push({
          x,
          y,
          color: allSeats[seatIndex].party.color,
          partyName: allSeats[seatIndex].party.name,
          partyShort: allSeats[seatIndex].party.shortName,
        });
        seatIndex++;
      }
    }
    
    return positioned;
  };

  const positionedSeats = arrangeInHemicycle();

  return (
    <Card className="glass-card">
      <CardHeader className="text-center">
        <CardTitle className="text-2xl font-display">Riksdag Seat Distribution</CardTitle>
        <p className="text-muted-foreground">{totalSeats} Total Seats</p>
      </CardHeader>
      <CardContent>
        <div className="flex justify-center">
          <svg 
            viewBox="0 0 400 220" 
            className="w-full max-w-lg"
            style={{ overflow: 'visible' }}
          >
            {/* Individual seats */}
            {positionedSeats.map((seat, index) => (
              <circle
                key={index}
                cx={seat.x}
                cy={seat.y}
                r={3.5}
                fill={seat.color}
                className="transition-all duration-200 hover:r-5 cursor-pointer"
                style={{
                  filter: 'drop-shadow(0 1px 2px rgba(0,0,0,0.2))',
                }}
              >
                <title>{seat.partyName} ({seat.partyShort})</title>
              </circle>
            ))}
            
            {/* Center podium decoration */}
            <rect
              x={185}
              y={195}
              width={30}
              height={15}
              rx={3}
              fill="hsl(var(--muted))"
              stroke="hsl(var(--border))"
              strokeWidth={1}
            />
          </svg>
        </div>

        {/* Legend */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-8 pt-6 border-t border-border/50">
          {sortedParties.map(party => (
            <div key={party.id} className="flex items-center gap-2 group cursor-pointer">
              <div 
                className="w-4 h-4 rounded-full flex-shrink-0 transition-transform group-hover:scale-125"
                style={{ backgroundColor: party.color }}
              />
              <div className="min-w-0">
                <p className="text-sm font-medium truncate">{party.shortName}</p>
                <p className="text-xs text-muted-foreground">
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
