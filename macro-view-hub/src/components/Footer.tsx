import { BarChart3 } from "lucide-react";

const Footer = () => {
  return (
    <footer className="border-t border-border/50 bg-card/50">
      <div className="container mx-auto px-4 py-12">
        <div className="grid md:grid-cols-4 gap-8">
          <div className="md:col-span-2">
            <div className="flex items-center gap-3 mb-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-primary">
                <BarChart3 className="h-5 w-5 text-primary-foreground" />
              </div>
              <span className="text-xl font-semibold tracking-tight">MacroVue</span>
            </div>
            <p className="text-sm text-muted-foreground max-w-sm">
              Your comprehensive platform for macroeconomic data visualization and analysis. 
              Track global economic indicators in real-time.
            </p>
          </div>

          <div>
            <h4 className="font-semibold mb-4">Data</h4>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li><a href="#" className="hover:text-foreground transition-colors">GDP</a></li>
              <li><a href="#" className="hover:text-foreground transition-colors">Inflation</a></li>
              <li><a href="#" className="hover:text-foreground transition-colors">Employment</a></li>
              <li><a href="#" className="hover:text-foreground transition-colors">Interest Rates</a></li>
            </ul>
          </div>

          <div>
            <h4 className="font-semibold mb-4">Resources</h4>
            <ul className="space-y-2 text-sm text-muted-foreground">
              <li><a href="#" className="hover:text-foreground transition-colors">Documentation</a></li>
              <li><a href="#" className="hover:text-foreground transition-colors">API Access</a></li>
              <li><a href="#" className="hover:text-foreground transition-colors">Research</a></li>
              <li><a href="#" className="hover:text-foreground transition-colors">Contact</a></li>
            </ul>
          </div>
        </div>

        <div className="mt-12 pt-8 border-t border-border/50 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-sm text-muted-foreground">
            © 2024 MacroVue. All rights reserved.
          </p>
          <p className="text-xs text-muted-foreground">
            Data for illustrative purposes only. Not financial advice.
          </p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
