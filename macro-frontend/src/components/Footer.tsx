import {BarChart3} from "lucide-react";
import {Link} from "react-router-dom";

const Footer = () => {
    return (
        <footer className="border-t border-border/50 bg-card/50">
            <div className="container mx-auto px-4 py-12">
                <div className="grid md:grid-cols-4 gap-8">
                    <div className="md:col-span-2">
                        <div className="flex items-center gap-3 mb-4">
                            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-primary">
                                <BarChart3 className="h-5 w-5 text-primary-foreground"/>
                            </div>
                            <span className="text-xl font-semibold tracking-tight">Macro Hub</span>
                        </div>
                        <p className="text-sm text-muted-foreground max-w-sm">
                            Din kompletta plattform för visualisering och analys av makroekonomisk data och
                            riksdagsinformation. Följ ekonomiska indikatorer och politiska trender i realtid.
                        </p>
                    </div>

                    <div>
                        <h4 className="font-semibold mb-4">Data</h4>
                        <ul className="space-y-2 text-sm text-muted-foreground">
                            <li><Link to="/macroindicator" className="hover:text-foreground transition-colors">Makroindikatorer</Link></li>
                            <li><Link to="/parliament" className="hover:text-foreground transition-colors">Riksdagen</Link></li>
                            <li><Link to="/regions" className="hover:text-foreground transition-colors">Regioner</Link></li>
                            <li><Link to="/municipalities" className="hover:text-foreground transition-colors">Kommuner</Link></li>
                        </ul>
                    </div>

                    <div>
                        <h4 className="font-semibold mb-4">Resurser</h4>
                        <ul className="space-y-2 text-sm text-muted-foreground">
                            <li><Link to="/sources" className="hover:text-foreground transition-colors">Källor</Link></li>
                            <li><Link to="/contact" className="hover:text-foreground transition-colors">Kontakt</Link></li>
                        </ul>
                    </div>
                </div>

                <div
                    className="mt-12 pt-8 border-t border-border/50 flex flex-col md:flex-row justify-between items-center gap-4">
                    <p className="text-sm text-muted-foreground">
                        © {new Date().getFullYear()} GjertssonChen AB
                    </p>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
