import {Link} from "react-router-dom";
import {ArrowRight} from "lucide-react";
import {Area, AreaChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, TooltipProps, XAxis, YAxis} from "recharts";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Skeleton} from "@/components/ui/skeleton";
import {useSeriesConfigs} from "@/api/chartData";
import {TrendDefinition, TrendFrequency, TrendPoint, useTrendSeries} from "@/api/trends";

type Trend = TrendDefinition & {
    tab: string;
    title: string;
    kind: "area" | "line";
    color: string;
    fractionDigits: number;
    // Fit the Y axis to the data instead of starting at 0, for a series whose values lie far from 0
    fitYAxis?: boolean;
};

const TRENDS: Trend[] = [
    {
        name: "FEDGPD",
        frequency: "fiscalYear",
        tab: "BNP USA",
        title: "USA:s bruttonationalprodukt",
        kind: "area",
        color: "hsl(var(--primary))",
        fractionDigits: 0,
        fitYAxis: true,
    },
    {
        name: "FEDCPI",
        frequency: "month",
        tab: "KPI USA (median)",
        title: "Konsumentprisindex USA (median)",
        kind: "line",
        color: "hsl(var(--warning))",
        fractionDigits: 2,
    },
    {
        name: "FEDUnemploymentRate",
        frequency: "month",
        tab: "Arbetslöshet USA",
        title: "Arbetslöshet USA",
        kind: "area",
        color: "hsl(var(--chart-2))",
        fractionDigits: 1,
    },
];

const FREQUENCY_TEXT: Record<TrendFrequency, string> = {
    fiscalYear: "Årlig (budgetår)",
    month: "Månadsvis",
};

// Axis ticks show the year for a yearly series and the month otherwise ("2025", "sep 2026"); the tooltip spells
// the month out ("september 2026")
const formatDate = (date: Date, frequency: TrendFrequency, month: "short" | "long") =>
    new Intl.DateTimeFormat("sv-SE", {
        month: frequency === "month" ? month : undefined,
        year: "numeric",
        timeZone: "UTC",
    }).format(date).replace(".", "");

const formatNumber = (value: number, minimumFractionDigits: number, maximumFractionDigits: number) =>
    value.toLocaleString("sv-SE", {minimumFractionDigits, maximumFractionDigits});

const withUnit = (text: string, unit: string | undefined) => (unit ? `${text} ${unit}` : text);

type ChartRow = {
    axisLabel: string;
    tooltipLabel: string;
    value: number;
};

const toRows = (points: TrendPoint[], frequency: TrendFrequency): ChartRow[] =>
    points.map(point => ({
        axisLabel: formatDate(point.date, frequency, "short"),
        tooltipLabel: formatDate(point.date, frequency, "long"),
        value: point.value,
    }));

// The data range padded by 5 % on each side and widened to whole steps of 1, 2, 2.5 or 5 times a power of ten:
// the smallest such step that needs at most six intervals, with a tick on every step
const MAX_INTERVALS = 6;

const fittedAxis = (values: number[]) => {
    const min = Math.min(...values);
    const max = Math.max(...values);
    const padding = (max - min) * 0.05 || Math.abs(max) * 0.05 || 1;
    const magnitude = Math.pow(10, Math.floor(Math.log10((max - min + 2 * padding) / MAX_INTERVALS)));
    const bounds = (step: number) =>
        [Math.floor((min - padding) / step) * step, Math.ceil((max + padding) / step) * step] as [number, number];
    const step = [1, 2, 2.5, 5, 10, 20]
        .map(factor => factor * magnitude)
        .find(candidate => {
            const [lower, upper] = bounds(candidate);
            return Math.round((upper - lower) / candidate) <= MAX_INTERVALS;
        }) ?? 20 * magnitude;
    const [lower, upper] = bounds(step);
    const ticks = Array.from({length: Math.round((upper - lower) / step) + 1},
        // toPrecision drops float noise such as 5.1000000000000005
        (_, index) => Number((lower + index * step).toPrecision(12)));
    return {domain: [lower, upper] as [number, number], ticks};
};

const subtitle = (points: TrendPoint[], frequency: TrendFrequency, unit: string | undefined) => {
    const first = points[0].date.getUTCFullYear();
    const last = points[points.length - 1].date.getUTCFullYear();
    const range = first === last ? `${first}` : `${first}–${last}`;
    return [FREQUENCY_TEXT[frequency], unit, range].filter(Boolean).join(", ");
};

const TrendChart = ({trend}: { trend: Trend }) => {
    const {isPending, error, data: points} = useTrendSeries(trend);
    // Only for the unit; until or without it the values are shown without one
    const {data: configs} = useSeriesConfigs();
    const unit = configs?.find(config => config.name === trend.name)?.unit;

    if (isPending) {
        return (
            <>
                <div className="mb-6">
                    <h3 className="text-xl font-semibold mb-2">{trend.title}</h3>
                    <Skeleton className="h-5 w-56"/>
                </div>
                <Skeleton className="h-[400px] w-full rounded-xl"/>
            </>
        );
    }

    // A failed background refetch keeps the data of the last successful one, so the chart stays
    if (!points || points.length === 0) {
        return (
            <>
                <div className="mb-6">
                    <h3 className="text-xl font-semibold mb-2">{trend.title}</h3>
                </div>
                <div className="h-[400px] flex items-center justify-center text-sm text-muted-foreground">
                    {error ? "Kunde inte hämta data" : "Ingen data ännu"}
                </div>
            </>
        );
    }

    const rows = toRows(points, trend.frequency);
    const tickFormatter = (value: number) => withUnit(formatNumber(value, 0, 1), unit);
    // Room for the longest tick, e.g. "30 000 mdr USD"
    const yAxisWidth = unit && unit.length > 2 ? 100 : 60;
    const yAxis = trend.fitYAxis ? fittedAxis(points.map(point => point.value)) : undefined;

    const tooltip = ({active, payload}: TooltipProps<number, string>) => {
        if (!active || !payload || payload.length === 0 || payload[0].value === undefined) {
            return null;
        }
        const row = payload[0].payload as ChartRow;
        return (
            <div className="glass-card rounded-lg p-3 border border-border">
                <p className="text-sm text-muted-foreground">{row.tooltipLabel}</p>
                <p className="text-lg font-mono font-bold text-primary">
                    {withUnit(formatNumber(payload[0].value, trend.fractionDigits, trend.fractionDigits), unit)}
                </p>
            </div>
        );
    };

    const gradientId = `${trend.name}Gradient`;
    const axes = (
        <>
            <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))"/>
            <XAxis dataKey="axisLabel" stroke="hsl(var(--muted-foreground))" fontSize={12} minTickGap={24}/>
            <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12} width={yAxisWidth}
                   tickFormatter={tickFormatter} domain={yAxis?.domain} ticks={yAxis?.ticks}/>
            <Tooltip content={tooltip}/>
        </>
    );

    return (
        <>
            <div className="mb-6">
                <h3 className="text-xl font-semibold mb-2">{trend.title}</h3>
                <p className="text-sm text-muted-foreground">{subtitle(points, trend.frequency, unit)}</p>
            </div>
            <div className="h-[400px]">
                <ResponsiveContainer width="100%" height="100%">
                    {trend.kind === "area" ? (
                        <AreaChart data={rows}>
                            <defs>
                                <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="0%" stopColor={trend.color} stopOpacity={0.3}/>
                                    <stop offset="100%" stopColor={trend.color} stopOpacity={0}/>
                                </linearGradient>
                            </defs>
                            {axes}
                            <Area
                                type="monotone"
                                dataKey="value"
                                stroke={trend.color}
                                strokeWidth={3}
                                fill={`url(#${gradientId})`}
                                // Fill down to the bottom of the axis, also when it does not start at 0
                                baseValue={trend.fitYAxis ? "dataMin" : undefined}
                            />
                        </AreaChart>
                    ) : (
                        <LineChart data={rows}>
                            {axes}
                            <Line
                                type="monotone"
                                dataKey="value"
                                stroke={trend.color}
                                strokeWidth={3}
                                // A dot per month would crowd ten years of observations
                                dot={false}
                                activeDot={{r: 6, fill: trend.color}}
                            />
                        </LineChart>
                    )}
                </ResponsiveContainer>
            </div>
        </>
    );
};

const ChartSection = () => {
    return (
        <section id="charts" className="py-20">
            <div className="container mx-auto px-4">
                <div className="text-center mb-12 opacity-0 animate-fade-in-up" style={{animationDelay: "200ms"}}>
                    <h2 className="text-3xl md:text-4xl font-bold mb-4">
                        Ekonomiska <span className="text-gradient">trender</span>
                    </h2>
                    <p className="text-muted-foreground max-w-2xl mx-auto">
                        Visualisera viktiga makroekonomiska indikatorer över tid med interaktiva diagram
                    </p>
                </div>

                <Tabs defaultValue={TRENDS[0].name} className="opacity-0 animate-fade-in-up"
                      style={{animationDelay: "300ms"}}>
                    <TabsList className="grid w-full max-w-xl mx-auto grid-cols-3 mb-8 bg-muted/50 h-auto">
                        {TRENDS.map(trend => (
                            <TabsTrigger key={trend.name} value={trend.name}
                                         className="whitespace-normal data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                                {trend.tab}
                            </TabsTrigger>
                        ))}
                    </TabsList>

                    <div className="glass-card rounded-2xl p-6 md:p-8">
                        {TRENDS.map(trend => (
                            <TabsContent key={trend.name} value={trend.name} className="mt-0">
                                <TrendChart trend={trend}/>
                            </TabsContent>
                        ))}
                    </div>
                </Tabs>

                <div className="mt-6 text-center">
                    <Link to="/macroindicator"
                          className="inline-flex items-center gap-1 text-sm text-primary hover:underline">
                        Utforska fler serier
                        <ArrowRight className="h-4 w-4"/>
                    </Link>
                </div>
            </div>
        </section>
    );
};

export default ChartSection;
