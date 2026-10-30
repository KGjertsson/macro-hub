import {AlertTriangle, CheckCircle, TrendingUp} from "lucide-react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {ExampleBadge, ExampleNote} from "@/components/ExampleContent";

const insights = [
    {
        title: "Fed:s räntebesked",
        description: "Federal Reserve behåller räntan i intervallet 4,25–4,50 % och signalerar möjliga sänkningar under 2025.",
        type: "neutral",
        date: "18 dec 2024",
        icon: TrendingUp,
    },
    {
        title: "Motståndskraftig arbetsmarknad",
        description: "Sysselsättningen är fortsatt stark med 227 000 nya jobb i november, över förväntningarna.",
        type: "positive",
        date: "6 dec 2024",
        icon: CheckCircle,
    },
    {
        title: "Inflationsbevakning",
        description: "Kärninflationen enligt PCE på 2,8 % ligger kvar över Fed:s mål på 2 %, vilket kräver fortsatt vaksamhet.",
        type: "warning",
        date: "27 nov 2024",
        icon: AlertTriangle,
    },
];

const InsightsSection = () => {
    return (
        <section
            id="insights"
            className="py-20 bg-gradient-glow"
            aria-labelledby="insights-heading"
            aria-describedby="insights-example-note"
        >
            <div className="container mx-auto px-4">
                <div className="text-center mb-12 opacity-0 animate-fade-in-up" style={{animationDelay: "100ms"}}>
                    <div className="flex items-center justify-center gap-3 mb-4">
                        <h2 id="insights-heading" className="text-3xl md:text-4xl font-bold">
                            Marknads<span className="text-gradient">insikter</span>
                        </h2>
                        <ExampleBadge/>
                    </div>
                    <p className="text-muted-foreground max-w-2xl mx-auto">
                        Håll dig uppdaterad med den senaste ekonomiska utvecklingen och analysen
                    </p>
                    <ExampleNote id="insights-example-note" className="justify-center mt-2"/>
                </div>

                <div className="grid md:grid-cols-3 gap-6">
                    {insights.map((insight, index) => (
                        <Card
                            key={insight.title}
                            className="glass-card border-border/50 opacity-0 animate-fade-in-up"
                            style={{animationDelay: `${(index + 2) * 100}ms`}}
                        >
                            <CardHeader className="pb-3">
                                <div className="flex items-start justify-between">
                                    <div className={`
                    flex h-10 w-10 items-center justify-center rounded-lg
                    ${insight.type === "positive" ? "bg-success/10 text-success" : ""}
                    ${insight.type === "warning" ? "bg-warning/10 text-warning" : ""}
                    ${insight.type === "neutral" ? "bg-primary/10 text-primary" : ""}
                  `}>
                                        <insight.icon className="h-5 w-5"/>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <ExampleBadge/>
                                        <Badge variant="outline" className="text-xs text-muted-foreground">
                                            {insight.date}
                                        </Badge>
                                    </div>
                                </div>
                                <CardTitle className="text-lg mt-4">
                                    {insight.title}
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-sm text-muted-foreground leading-relaxed">
                                    {insight.description}
                                </p>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default InsightsSection;
