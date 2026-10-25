import { useState } from 'react';
import { RegionVotingData, regionVotingData } from '@/data/regionData';

interface SwedenMapProps {
  onRegionSelect: (region: RegionVotingData | null) => void;
  selectedRegion: RegionVotingData | null;
}

const SwedenMap = ({ onRegionSelect, selectedRegion }: SwedenMapProps) => {
  const [hoveredRegion, setHoveredRegion] = useState<string | null>(null);

  const getRegionData = (id: string) => {
    return regionVotingData.find(r => r.id === id);
  };

  const getRegionColor = (id: string) => {
    const region = getRegionData(id);
    if (!region) return 'hsl(var(--muted))';
    
    const isActive = selectedRegion?.id === id || hoveredRegion === id;
    if (isActive) {
      return region.winnerColor;
    }
    return `${region.winnerColor}80`;
  };

  const handleRegionClick = (id: string) => {
    const region = getRegionData(id);
    if (region) {
      onRegionSelect(selectedRegion?.id === id ? null : region);
    }
  };

  const regionPaths: { id: string; path: string; label: string; labelX: number; labelY: number }[] = [
    {
      id: "norrbotten",
      path: "M280,20 L340,25 L360,60 L380,100 L370,140 L350,180 L320,200 L280,190 L250,160 L240,120 L250,80 L260,40 Z",
      label: "Norrbotten",
      labelX: 300,
      labelY: 110
    },
    {
      id: "vasterbotten",
      path: "M250,160 L280,190 L320,200 L340,230 L330,270 L290,290 L250,280 L220,250 L210,210 L220,180 Z",
      label: "Västerbotten",
      labelX: 270,
      labelY: 230
    },
    {
      id: "jamtland",
      path: "M180,230 L220,250 L250,280 L260,320 L240,360 L200,380 L160,360 L140,320 L150,280 L160,250 Z",
      label: "Jämtland",
      labelX: 200,
      labelY: 310
    },
    {
      id: "vasternorrland",
      path: "M250,280 L290,290 L320,320 L330,360 L310,400 L270,410 L240,390 L230,350 L240,320 Z",
      label: "Västernorrland",
      labelX: 280,
      labelY: 350
    },
    {
      id: "gavleborg",
      path: "M240,390 L270,410 L300,430 L310,470 L290,500 L250,510 L220,490 L210,450 L220,420 Z",
      label: "Gävleborg",
      labelX: 260,
      labelY: 450
    },
    {
      id: "dalarna",
      path: "M160,360 L200,380 L230,410 L230,450 L210,490 L170,500 L140,480 L120,440 L130,400 Z",
      label: "Dalarna",
      labelX: 175,
      labelY: 440
    },
    {
      id: "varmland",
      path: "M100,460 L140,480 L170,500 L180,540 L160,580 L120,590 L90,570 L70,530 L80,490 Z",
      label: "Värmland",
      labelX: 125,
      labelY: 530
    },
    {
      id: "orebro",
      path: "M170,500 L210,490 L240,520 L250,560 L230,590 L190,600 L160,580 L160,540 Z",
      label: "Örebro",
      labelX: 200,
      labelY: 550
    },
    {
      id: "vastmanland",
      path: "M240,520 L280,510 L310,530 L320,570 L300,600 L260,610 L230,590 L230,550 Z",
      label: "Västmanland",
      labelX: 275,
      labelY: 560
    },
    {
      id: "uppsala",
      path: "M300,500 L340,490 L370,520 L380,560 L360,600 L320,610 L290,590 L290,540 Z",
      label: "Uppsala",
      labelX: 335,
      labelY: 550
    },
    {
      id: "stockholm",
      path: "M340,590 L380,580 L410,610 L420,650 L400,690 L360,700 L330,670 L320,630 Z",
      label: "Stockholm",
      labelX: 370,
      labelY: 640
    },
    {
      id: "sodermanland",
      path: "M280,610 L320,600 L350,630 L360,670 L340,710 L300,720 L270,690 L260,650 Z",
      label: "Södermanland",
      labelX: 310,
      labelY: 660
    },
    {
      id: "ostergotland",
      path: "M220,640 L260,650 L290,680 L300,720 L280,760 L240,770 L210,740 L200,700 Z",
      label: "Östergötland",
      labelX: 250,
      labelY: 710
    },
    {
      id: "jonkoping",
      path: "M160,700 L200,700 L230,730 L240,770 L220,810 L180,820 L150,790 L140,750 Z",
      label: "Jönköping",
      labelX: 190,
      labelY: 760
    },
    {
      id: "kronoberg",
      path: "M180,820 L220,810 L250,840 L260,880 L240,910 L200,920 L170,890 L160,850 Z",
      label: "Kronoberg",
      labelX: 210,
      labelY: 865
    },
    {
      id: "kalmar",
      path: "M260,760 L300,750 L330,780 L350,830 L340,880 L300,900 L260,880 L250,820 Z",
      label: "Kalmar",
      labelX: 300,
      labelY: 825
    },
    {
      id: "gotland",
      path: "M380,720 L410,730 L420,770 L410,810 L380,820 L360,790 L365,750 Z",
      label: "Gotland",
      labelX: 390,
      labelY: 770
    },
    {
      id: "blekinge",
      path: "M220,920 L260,910 L290,940 L290,970 L260,990 L230,980 L210,950 Z",
      label: "Blekinge",
      labelX: 255,
      labelY: 955
    },
    {
      id: "skane",
      path: "M140,900 L200,920 L230,950 L240,1000 L210,1040 L160,1050 L120,1020 L100,970 L110,930 Z",
      label: "Skåne",
      labelX: 170,
      labelY: 980
    },
    {
      id: "halland",
      path: "M80,780 L120,790 L150,830 L160,880 L140,920 L100,930 L70,900 L60,850 L65,810 Z",
      label: "Halland",
      labelX: 110,
      labelY: 860
    },
    {
      id: "vastragotaland",
      path: "M60,620 L100,610 L140,640 L160,700 L150,760 L100,780 L60,760 L40,700 L45,660 Z",
      label: "Västra Götaland",
      labelX: 100,
      labelY: 700
    }
  ];

  return (
    <svg
      viewBox="0 0 450 1100"
      className="w-full h-full max-h-[70vh]"
      style={{ filter: 'drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1))' }}
    >
      <defs>
        <filter id="glow">
          <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
          <feMerge>
            <feMergeNode in="coloredBlur"/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
      </defs>
      
      {regionPaths.map(({ id, path }) => (
        <path
          key={id}
          d={path}
          fill={getRegionColor(id)}
          stroke="hsl(var(--border))"
          strokeWidth={selectedRegion?.id === id ? 3 : 1.5}
          className="cursor-pointer transition-all duration-300"
          style={{
            filter: selectedRegion?.id === id || hoveredRegion === id ? 'url(#glow)' : 'none'
          }}
          onMouseEnter={() => setHoveredRegion(id)}
          onMouseLeave={() => setHoveredRegion(null)}
          onClick={() => handleRegionClick(id)}
        />
      ))}
      
      {regionPaths.map(({ id, labelX, labelY, label }) => (
        <text
          key={`label-${id}`}
          x={labelX}
          y={labelY}
          textAnchor="middle"
          className="pointer-events-none text-[10px] font-medium fill-foreground"
          style={{ 
            opacity: hoveredRegion === id || selectedRegion?.id === id ? 1 : 0.7,
            textShadow: '0 1px 2px rgba(255,255,255,0.8)'
          }}
        >
          {label}
        </text>
      ))}
    </svg>
  );
};

export default SwedenMap;
