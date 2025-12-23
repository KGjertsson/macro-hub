import { parties } from '@/data/parliamentData';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

const SeatDistribution = () => {
  const totalSeats = 349;
  
  // Sort parties by seats for better visual distribution (largest in center)
  const sortedParties = [...parties].sort((a, b) => b.seats - a.seats);
  
  // Calculate percentages and angles for arc segments
  const getArcSegments = () => {
    let currentAngle = 0;
    const arcStart = 180; // Start from top
    const arcEnd = 360; // End at bottom
    const totalArc = Math.abs(arcEnd - arcStart); // 180 degrees
    
    return sortedParties.map(party => {
      const percentage = party.seats / totalSeats;
      const arcLength = totalArc * percentage;
      const startAngle = arcStart + currentAngle;
      const endAngle = startAngle + arcLength;
      currentAngle += arcLength;
      
      return {
        party,
        percentage,
        startAngle,
        endAngle,
        midAngle: (startAngle + endAngle) / 2,
      };
    });
  };

  const segments = getArcSegments();
  const radius = 140;
  const strokeWidth = 40;
  const centerX = 200;
  const centerY = 180;

  const polarToCartesian = (angle: number, r: number) => {
    const radians = (angle * Math.PI) / 180;
    return {
      x: centerX + r * Math.cos(radians),
      y: centerY + r * Math.sin(radians),
    };
  };

  const createArcPath = (startAngle: number, endAngle: number, r: number) => {
    const start = polarToCartesian(startAngle, r);
    const end = polarToCartesian(endAngle, r);
    const largeArcFlag = Math.abs(startAngle - endAngle) > 180 ? 1 : 0;
    
    return `M ${start.x} ${start.y} A ${r} ${r} 0 ${largeArcFlag} 1 ${end.x} ${end.y}`;
  };

  return (
    <Card className="glass-card">
      <CardHeader className="text-center">
        <CardTitle className="text-2xl font-display">Riksdag Seat Distribution</CardTitle>
        <p className="text-muted-foreground">{totalSeats} Total Seats</p>
      </CardHeader>
      <CardContent>
        <div className="flex justify-center">
          <svg 
            viewBox="0 0 250 360" 
            className="w-full max-w-xs"
            style={{ overflow: 'visible' }}
          >
            {/* Arc segments */}
            {segments.map((segment, index) => (
              <g key={segment.party.id} className="transition-all duration-300">
                <path
                  d={createArcPath(segment.startAngle, segment.endAngle, radius)}
                  fill="none"
                  stroke={segment.party.color}
                  strokeWidth={strokeWidth}
                  strokeLinecap="butt"
                  className="transition-all duration-300 hover:opacity-80 cursor-pointer"
                  style={{
                    filter: 'drop-shadow(0 2px 4px rgba(0,0,0,0.2))',
                  }}
                >
                  <title>{segment.party.name}: {segment.party.seats} seats ({(segment.percentage * 100).toFixed(1)}%)</title>
                </path>
              </g>
            ))}
            
            {/* Center decoration */}
            <circle 
              cx={centerX} 
              cy={centerY} 
              r={radius - strokeWidth - 10} 
              fill="none"
              stroke="hsl(var(--border))"
              strokeWidth="1"
              strokeDasharray="4 4"
              opacity="0.3"
            />
            
            {/* Center text */}
            <text 
              x={centerX} 
              y={centerY - 10} 
              textAnchor="middle" 
              className="fill-foreground text-2xl font-bold font-display"
            >
              {totalSeats}
            </text>
            <text 
              x={centerX} 
              y={centerY + 12} 
              textAnchor="middle" 
              className="fill-muted-foreground text-xs"
            >
              seats
            </text>
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
