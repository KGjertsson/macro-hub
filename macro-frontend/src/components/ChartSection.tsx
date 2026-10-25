import {Area, AreaChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, TooltipProps, XAxis, YAxis} from "recharts";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";

const gdpData = [
    {year: "2019", value: 21.43},
    {year: "2020", value: 20.95},
    {year: "2021", value: 23.32},
    {year: "2022", value: 25.46},
    {year: "2023", value: 27.36},
    {year: "2024", value: 28.78},
];

const inflationData = [
    {month: "jan", value: 3.1},
    {month: "feb", value: 3.2},
    {month: "mar", value: 3.5},
    {month: "apr", value: 3.4},
    {month: "maj", value: 3.3},
    {month: "jun", value: 3.0},
    {month: "jul", value: 2.9},
    {month: "aug", value: 2.5},
    {month: "sep", value: 2.4},
    {month: "okt", value: 2.6},
    {month: "nov", value: 2.7},
    {month: "dec", value: 2.9},
];

const unemploymentData = [
    {month: "jan", value: 3.7},
    {month: "feb", value: 3.9},
    {month: "mar", value: 3.8},
    {month: "apr", value: 3.9},
    {month: "maj", value: 4.0},
    {month: "jun", value: 4.1},
    {month: "jul", value: 4.3},
    {month: "aug", value: 4.2},
    {month: "sep", value: 4.1},
    {month: "okt", value: 4.1},
    {month: "nov", value: 4.2},
    {month: "dec", value: 4.1},
];

const CustomTooltip = ({active, payload, label}: TooltipProps<number, string>) => {
    if (active && payload && payload.length) {
        return (
            <div className="glass-card rounded-lg p-3 border border-border">
                <p className="text-sm text-muted-foreground">{label}</p>
                <p className="text-lg font-mono font-bold text-primary">
                    {payload[0].value?.toLocaleString("sv-SE", {minimumFractionDigits: 2, maximumFractionDigits: 2})}
                </p>
            </div>
        );
    }
    return null;
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

                <Tabs defaultValue="gdp" className="opacity-0 animate-fade-in-up" style={{animationDelay: "300ms"}}>
                    <TabsList className="grid w-full max-w-md mx-auto grid-cols-3 mb-8 bg-muted/50">
                        <TabsTrigger value="gdp"
                                     className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                            BNP
                        </TabsTrigger>
                        <TabsTrigger value="inflation"
                                     className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                            Inflation
                        </TabsTrigger>
                        <TabsTrigger value="unemployment"
                                     className="data-[state=active]:bg-primary data-[state=active]:text-primary-foreground">
                            Arbetslöshet
                        </TabsTrigger>
                    </TabsList>

                    <div className="glass-card rounded-2xl p-6 md:p-8">
                        <TabsContent value="gdp" className="mt-0">
                            <div className="mb-6">
                                <h3 className="text-xl font-semibold mb-2">USA:s bruttonationalprodukt</h3>
                                <p className="text-sm text-muted-foreground">Årlig BNP i biljoner USD (2019–2024)</p>
                            </div>
                            <div className="h-[400px]">
                                <ResponsiveContainer width="100%" height="100%">
                                    <AreaChart data={gdpData}>
                                        <defs>
                                            <linearGradient id="gdpGradient" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="0%" stopColor="hsl(var(--primary))" stopOpacity={0.3}/>
                                                <stop offset="100%" stopColor="hsl(var(--primary))" stopOpacity={0}/>
                                            </linearGradient>
                                        </defs>
                                        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))"/>
                                        <XAxis dataKey="year" stroke="hsl(var(--muted-foreground))" fontSize={12}/>
                                        <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12}
                                               tickFormatter={(v) => `${v.toLocaleString("sv-SE")} bn`}/>
                                        <Tooltip content={<CustomTooltip/>}/>
                                        <Area
                                            type="monotone"
                                            dataKey="value"
                                            stroke="hsl(var(--primary))"
                                            strokeWidth={3}
                                            fill="url(#gdpGradient)"
                                        />
                                    </AreaChart>
                                </ResponsiveContainer>
                            </div>
                        </TabsContent>

                        <TabsContent value="inflation" className="mt-0">
                            <div className="mb-6">
                                <h3 className="text-xl font-semibold mb-2">Konsumentprisindex (KPI)</h3>
                                <p className="text-sm text-muted-foreground">Inflationstakt per månad i % (2024)</p>
                            </div>
                            <div className="h-[400px]">
                                <ResponsiveContainer width="100%" height="100%">
                                    <LineChart data={inflationData}>
                                        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))"/>
                                        <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" fontSize={12}/>
                                        <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12}
                                               tickFormatter={(v) => `${v.toLocaleString("sv-SE")} %`}/>
                                        <Tooltip content={<CustomTooltip/>}/>
                                        <Line
                                            type="monotone"
                                            dataKey="value"
                                            stroke="hsl(var(--warning))"
                                            strokeWidth={3}
                                            dot={{fill: "hsl(var(--warning))", strokeWidth: 2, r: 4}}
                                            activeDot={{r: 6, fill: "hsl(var(--warning))"}}
                                        />
                                    </LineChart>
                                </ResponsiveContainer>
                            </div>
                        </TabsContent>

                        <TabsContent value="unemployment" className="mt-0">
                            <div className="mb-6">
                                <h3 className="text-xl font-semibold mb-2">Arbetslöshet</h3>
                                <p className="text-sm text-muted-foreground">Arbetslöshet per månad i % (2024)</p>
                            </div>
                            <div className="h-[400px]">
                                <ResponsiveContainer width="100%" height="100%">
                                    <AreaChart data={unemploymentData}>
                                        <defs>
                                            <linearGradient id="unemploymentGradient" x1="0" y1="0" x2="0" y2="1">
                                                <stop offset="0%" stopColor="hsl(var(--chart-2))" stopOpacity={0.3}/>
                                                <stop offset="100%" stopColor="hsl(var(--chart-2))" stopOpacity={0}/>
                                            </linearGradient>
                                        </defs>
                                        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))"/>
                                        <XAxis dataKey="month" stroke="hsl(var(--muted-foreground))" fontSize={12}/>
                                        <YAxis stroke="hsl(var(--muted-foreground))" fontSize={12}
                                               tickFormatter={(v) => `${v.toLocaleString("sv-SE")} %`}/>
                                        <Tooltip content={<CustomTooltip/>}/>
                                        <Area
                                            type="monotone"
                                            dataKey="value"
                                            stroke="hsl(var(--chart-2))"
                                            strokeWidth={3}
                                            fill="url(#unemploymentGradient)"
                                        />
                                    </AreaChart>
                                </ResponsiveContainer>
                            </div>
                        </TabsContent>
                    </div>
                </Tabs>
            </div>
        </section>
    );
};

export default ChartSection;
