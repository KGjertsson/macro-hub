import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import { cn } from "@/lib/utils";

interface IndicatorCardProps {
  title: string;
  value: string;
  change: number;
  unit?: string;
  period?: string;
  delay?: string;
}

const IndicatorCard = ({ title, value, change, unit = "%", period = "YoY", delay = "0" }: IndicatorCardProps) => {
  const isPositive = change > 0;
  const isNeutral = change === 0;

  const TrendIcon = isNeutral ? Minus : isPositive ? TrendingUp : TrendingDown;

  return (
    <div 
      className={cn(
        "glass-card rounded-xl p-6 opacity-0 animate-fade-in-up",
        "hover:border-primary/30 transition-all duration-300 hover:shadow-lg hover:shadow-primary/5"
      )}
      style={{ animationDelay: delay }}
    >
      <div className="flex items-start justify-between mb-4">
        <span className="text-sm font-medium text-muted-foreground">{title}</span>
        <div className={cn(
          "flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full",
          isNeutral && "bg-muted text-muted-foreground",
          isPositive && "bg-success/10 text-success",
          !isPositive && !isNeutral && "bg-destructive/10 text-destructive"
        )}>
          <TrendIcon className="h-3 w-3" />
          <span>{isPositive ? "+" : ""}{change.toFixed(2)}{unit}</span>
        </div>
      </div>
      
      <div className="flex items-baseline gap-2">
        <span className="data-value text-foreground">{value}</span>
        <span className="text-sm text-muted-foreground">{unit}</span>
      </div>
      
      <div className="mt-3 flex items-center gap-2">
        <div className="h-1 flex-1 bg-muted rounded-full overflow-hidden">
          <div 
            className={cn(
              "h-full rounded-full transition-all duration-1000",
              isPositive ? "bg-success" : "bg-destructive"
            )}
            style={{ width: `${Math.min(Math.abs(change) * 10, 100)}%` }}
          />
        </div>
        <span className="text-xs text-muted-foreground">{period}</span>
      </div>
    </div>
  );
};

export default IndicatorCard;
