import { useState } from 'react';
import { MunicipalityVotingData, municipalityVotingData } from '@/data/municipalityData';

interface SwedenMunicipalityMapProps {
  onMunicipalitySelect: (municipality: MunicipalityVotingData | null) => void;
  selectedMunicipality: MunicipalityVotingData | null;
}

const SwedenMunicipalityMap = ({ onMunicipalitySelect, selectedMunicipality }: SwedenMunicipalityMapProps) => {
  const [hoveredMunicipality, setHoveredMunicipality] = useState<string | null>(null);

  const getMunicipalityData = (id: string) => {
    return municipalityVotingData.find(m => m.id === id);
  };

  const getMunicipalityColor = (id: string) => {
    const municipality = getMunicipalityData(id);
    if (!municipality) return 'hsl(var(--muted))';
    
    const isActive = selectedMunicipality?.id === id || hoveredMunicipality === id;
    if (isActive) {
      return municipality.winnerColor;
    }
    return `${municipality.winnerColor}80`;
  };

  const handleMunicipalityClick = (id: string) => {
    const municipality = getMunicipalityData(id);
    if (municipality) {
      onMunicipalitySelect(selectedMunicipality?.id === id ? null : municipality);
    }
  };

  // Major municipalities positioned on the map
  const municipalityPoints: { id: string; x: number; y: number; label: string }[] = [
    { id: "lulea", x: 310, y: 100, label: "Luleå" },
    { id: "umea", x: 280, y: 240, label: "Umeå" },
    { id: "sundsvall", x: 290, y: 370, label: "Sundsvall" },
    { id: "gavle", x: 260, y: 450, label: "Gävle" },
    { id: "uppsala", x: 335, y: 540, label: "Uppsala" },
    { id: "vasteras", x: 270, y: 560, label: "Västerås" },
    { id: "stockholm", x: 370, y: 620, label: "Stockholm" },
    { id: "eskilstuna", x: 290, y: 640, label: "Eskilstuna" },
    { id: "orebro", x: 200, y: 550, label: "Örebro" },
    { id: "karlstad", x: 125, y: 530, label: "Karlstad" },
    { id: "linkoping", x: 250, y: 710, label: "Linköping" },
    { id: "norrkoping", x: 290, y: 690, label: "Norrköping" },
    { id: "jonkoping", x: 185, y: 760, label: "Jönköping" },
    { id: "goteborg", x: 80, y: 700, label: "Göteborg" },
    { id: "boras", x: 120, y: 740, label: "Borås" },
    { id: "vaxjo", x: 210, y: 865, label: "Växjö" },
    { id: "halmstad", x: 100, y: 850, label: "Halmstad" },
    { id: "helsingborg", x: 130, y: 950, label: "Helsingborg" },
    { id: "lund", x: 150, y: 990, label: "Lund" },
    { id: "malmo", x: 170, y: 1020, label: "Malmö" },
  ];

  return (
    <svg
      viewBox="0 0 450 1100"
      className="w-full h-full max-h-[70vh]"
      style={{ filter: 'drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1))' }}
    >
      <defs>
        <filter id="municipalityGlow">
          <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
          <feMerge>
            <feMergeNode in="coloredBlur"/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
      </defs>
      
      {/* Sweden outline - simplified */}
      <path
        d="M280,20 L340,25 L360,60 L380,100 L370,140 L350,180 L320,200 L340,230 L330,270 L320,320 L330,360 L310,400 L300,430 L310,470 L290,500 L340,490 L370,520 L380,560 L410,610 L420,650 L400,690 L380,720 L410,730 L420,770 L410,810 L380,820 L360,790 L360,700 L330,670 L340,710 L300,720 L280,760 L300,750 L330,780 L350,830 L340,880 L300,900 L290,940 L290,970 L260,990 L240,1000 L210,1040 L160,1050 L120,1020 L100,970 L110,930 L80,780 L60,760 L40,700 L60,620 L90,570 L70,530 L80,490 L100,460 L120,440 L130,400 L140,320 L160,360 L160,250 L180,230 L210,210 L220,180 L240,120 L250,80 L260,40 Z"
        fill="hsl(var(--muted)/0.3)"
        stroke="hsl(var(--border))"
        strokeWidth={1}
      />
      
      {/* Municipality points */}
      {municipalityPoints.map(({ id, x, y }) => (
        <circle
          key={id}
          cx={x}
          cy={y}
          r={selectedMunicipality?.id === id || hoveredMunicipality === id ? 18 : 14}
          fill={getMunicipalityColor(id)}
          stroke="hsl(var(--border))"
          strokeWidth={selectedMunicipality?.id === id ? 3 : 1.5}
          className="cursor-pointer transition-all duration-300"
          style={{
            filter: selectedMunicipality?.id === id || hoveredMunicipality === id ? 'url(#municipalityGlow)' : 'none'
          }}
          onMouseEnter={() => setHoveredMunicipality(id)}
          onMouseLeave={() => setHoveredMunicipality(null)}
          onClick={() => handleMunicipalityClick(id)}
        />
      ))}
      
      {/* Municipality labels */}
      {municipalityPoints.map(({ id, x, y, label }) => (
        <text
          key={`label-${id}`}
          x={x}
          y={y + 30}
          textAnchor="middle"
          className="pointer-events-none text-[9px] font-medium fill-foreground"
          style={{ 
            opacity: hoveredMunicipality === id || selectedMunicipality?.id === id ? 1 : 0.7,
            textShadow: '0 1px 2px rgba(255,255,255,0.8)'
          }}
        >
          {label}
        </text>
      ))}
    </svg>
  );
};

export default SwedenMunicipalityMap;
