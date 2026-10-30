import {Minus, TrendingDown, TrendingUp} from "lucide-react";
import {cn} from "@/lib/utils";

interface IndicatorCardProps {
    title: string;
    // null when the series has no data; the card then shows a dash and the message instead of a period
    value: number | null;
    // The observation before; the change is shown against it
    previous: number | null;
    unit?: string;
    period?: string;
    message?: string;
    delay?: string;
}

// As many decimals as the source gives, at most two (4,2 % stays 4,2 %, 1,75 % stays 1,75 %)
const decimalsOf = (value: number) => Math.min((String(value).split(".")[1] ?? "").length, 2);

// A change in a percentage is shown in procentenheter
const changeUnit = (unit: string) => (unit.startsWith("%") ? " pe" : ` ${unit}`);

const IndicatorCard = ({title, value, previous, unit = "", period, message, delay = "0"}: IndicatorCardProps) => {
    const decimals = Math.max(value === null ? 0 : decimalsOf(value), previous === null ? 0 : decimalsOf(previous));
    // Rounded to the shown decimals, so floating-point noise (4,0 - 4,1 = -0,0999…) does not leak into the badge
    const change = value === null || previous === null
        ? null
        : Math.round((value - previous) * 10 ** decimals) / 10 ** decimals;
    const isPositive = change !== null && change > 0;
    const isNeutral = change === null || change === 0;

    const TrendIcon = isNeutral ? Minus : isPositive ? TrendingUp : TrendingDown;

    const format = (n: number, signDisplay: "auto" | "exceptZero" = "auto") =>
        n.toLocaleString("sv-SE", {minimumFractionDigits: decimals, maximumFractionDigits: decimals, signDisplay});

    return (
        <div
            className={cn(
                "glass-card rounded-xl p-6 opacity-0 animate-fade-in-up",
                "hover:border-primary/30 transition-all duration-300 hover:shadow-lg hover:shadow-primary/5"
            )}
            style={{animationDelay: delay}}
        >
            <div className="flex items-start justify-between gap-2 mb-4">
                <span className="text-sm font-medium text-muted-foreground">{title}</span>
                {change !== null && (
                    <div
                        className={cn(
                            "flex shrink-0 items-center gap-1 text-xs font-medium px-2 py-1 rounded-full",
                            isNeutral && "bg-muted text-muted-foreground",
                            isPositive && "bg-success/10 text-success",
                            !isPositive && !isNeutral && "bg-destructive/10 text-destructive"
                        )}
                        title="Förändring mot föregående observation"
                    >
                        <TrendIcon className="h-3 w-3"/>
                        <span>{format(change, "exceptZero")}{changeUnit(unit)}</span>
                    </div>
                )}
            </div>

            <div className="flex items-baseline gap-2">
                <span className="data-value text-foreground">{value === null ? "–" : format(value)}</span>
                {value !== null && <span className="text-sm text-muted-foreground">{unit}</span>}
            </div>

            <div className="mt-3 flex items-center gap-2">
                <div className="h-1 flex-1 bg-muted rounded-full overflow-hidden">
                    <div
                        className={cn(
                            "h-full rounded-full transition-all duration-1000",
                            isNeutral ? "bg-muted-foreground" : isPositive ? "bg-success" : "bg-destructive"
                        )}
                        style={{width: `${Math.min(Math.abs(change ?? 0) * 10, 100)}%`}}
                    />
                </div>
                <span className="text-xs text-muted-foreground">{value === null ? message : period}</span>
            </div>
        </div>
    );
};

export default IndicatorCard;
