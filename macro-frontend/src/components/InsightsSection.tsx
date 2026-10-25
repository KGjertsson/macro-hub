import {AlertTriangle, ArrowRight, CheckCircle, TrendingUp} from "lucide-react";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";

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
        <section id="insights" className="py-20 bg-gradient-glow">
            <div className="container mx-auto px-4">
                <div className="text-center mb-12 opacity-0 animate-fade-in-up" style={{animationDelay: "100ms"}}>
                    <h2 className="text-3xl md:text-4xl font-bold mb-4">
                        Marknads<span className="text-gradient">insikter</span>
                    </h2>
                    <p className="text-muted-foreground max-w-2xl mx-auto">
                        Håll dig uppdaterad med den senaste ekonomiska utvecklingen och analysen
                    </p>
                </div>

                <div className="grid md:grid-cols-3 gap-6">
                    {insights.map((insight, index) => (
                        <Card
                            key={insight.title}
                            className="glass-card border-border/50 hover:border-primary/30 transition-all duration-300 cursor-pointer group opacity-0 animate-fade-in-up"
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
                                    <Badge variant="outline" className="text-xs text-muted-foreground">
                                        {insight.date}
                                    </Badge>
                                </div>
                                <CardTitle className="text-lg mt-4 group-hover:text-primary transition-colors">
                                    {insight.title}
                                </CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-sm text-muted-foreground leading-relaxed">
                                    {insight.description}
                                </p>
                                <div
                                    className="mt-4 flex items-center text-sm text-primary opacity-0 group-hover:opacity-100 transition-opacity">
                                    <span>Läs mer</span>
                                    <ArrowRight
                                        className="h-4 w-4 ml-1 group-hover:translate-x-1 transition-transform"/>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default InsightsSection;
