import { Link } from 'react-router-dom';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { User, Calendar, Building2 } from 'lucide-react';
import { Politician, getPartyById } from '@/data/parliamentData';

interface PoliticianCardProps {
  politician: Politician;
}

const PoliticianCard = ({ politician }: PoliticianCardProps) => {
  const party = getPartyById(politician.party);

  return (
    <Link to={`/parliament/politician/${politician.id}`}>
      <Card className="glass-card hover:border-primary/50 transition-all duration-300 hover:-translate-y-1 cursor-pointer group">
        <CardContent className="p-6">
          <div className="flex items-start gap-4">
            {/* Avatar placeholder */}
            <div 
              className="w-16 h-16 rounded-full flex items-center justify-center flex-shrink-0"
              style={{ backgroundColor: party?.color + '30', borderColor: party?.color, borderWidth: 2 }}
            >
              <User className="w-8 h-8" style={{ color: party?.color }} />
            </div>

            <div className="flex-1 min-w-0">
              <h3 className="font-semibold text-lg group-hover:text-primary transition-colors truncate">
                {politician.name}
              </h3>
              <p className="text-muted-foreground text-sm">{politician.role}</p>
              
              <div className="flex items-center gap-2 mt-2">
                <Badge 
                  variant="outline" 
                  className="text-xs"
                  style={{ borderColor: party?.color, color: party?.color }}
                >
                  {party?.shortName}
                </Badge>
              </div>

              <div className="flex items-center gap-4 mt-3 text-xs text-muted-foreground">
                <span className="flex items-center gap-1">
                  <Building2 className="w-3 h-3" />
                  {politician.constituency}
                </span>
                <span className="flex items-center gap-1">
                  <Calendar className="w-3 h-3" />
                  {politician.yearsInParliament} years
                </span>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-border/50">
            <p className="text-xs text-muted-foreground mb-2">Top Achievement</p>
            <p className="text-sm truncate">{politician.achievements[0]}</p>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
};

export default PoliticianCard;
