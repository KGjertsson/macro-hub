import { parties } from '@/data/parliamentData';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

const SeatDistribution = () => {
  const totalSeats = 349;
  
  // Create a hemicycle-style visualization
  const renderSeats = () => {
    const rows = 8;
    const seatsPerRow = [25, 35, 42, 48, 52, 55, 48, 44];
    let seatIndex = 0;
    const allSeats: { party: typeof parties[0]; row: number; position: number }[] = [];
    
    // Distribute seats by party
    parties.forEach(party => {
      for (let i = 0; i < party.seats; i++) {
        allSeats.push({ party, row: 0, position: seatIndex });
        seatIndex++;
      }
    });

    // Render rows
    return (
      <div className="flex flex-col items-center gap-1 py-8">
        {seatsPerRow.map((count, rowIndex) => {
          const startIdx = seatsPerRow.slice(0, rowIndex).reduce((a, b) => a + b, 0);
          const rowSeats = allSeats.slice(startIdx, startIdx + count);
          const arcRadius = 120 + rowIndex * 28;
          
          return (
            <div 
              key={rowIndex} 
              className="flex justify-center gap-0.5"
              style={{ 
                width: `${arcRadius * 2}px`,
                transform: `translateY(${rowIndex * -2}px)`
              }}
            >
              {rowSeats.map((seat, idx) => (
                <div
                  key={idx}
                  className="w-2.5 h-2.5 md:w-3 md:h-3 rounded-full transition-transform hover:scale-150 cursor-pointer"
                  style={{ backgroundColor: seat.party.color }}
                  title={`${seat.party.name} (${seat.party.shortName})`}
                />
              ))}
            </div>
          );
        })}
      </div>
    );
  };

  return (
    <Card className="glass-card">
      <CardHeader className="text-center">
        <CardTitle className="text-2xl font-display">Riksdag Seat Distribution</CardTitle>
        <p className="text-muted-foreground">{totalSeats} Total Seats</p>
      </CardHeader>
      <CardContent>
        <div className="relative">
          {renderSeats()}
          
          {/* Speaker podium */}
          <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-16 h-8 bg-muted rounded-t-lg flex items-center justify-center">
            <span className="text-xs text-muted-foreground">Speaker</span>
          </div>
        </div>

        {/* Legend */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-8 pt-6 border-t border-border/50">
          {parties.map(party => (
            <div key={party.id} className="flex items-center gap-2">
              <div 
                className="w-4 h-4 rounded-full flex-shrink-0"
                style={{ backgroundColor: party.color }}
              />
              <div className="min-w-0">
                <p className="text-sm font-medium truncate">{party.shortName}</p>
                <p className="text-xs text-muted-foreground">{party.seats} seats</p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default SeatDistribution;
