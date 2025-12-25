import Header from '@/components/Header';
import Footer from '@/components/Footer';
import { Globe, ExternalLink } from 'lucide-react';

const Sources = () => {
  const sources = [
    {
      name: 'Statistiska Centralbyrån (SCB)',
      description: 'Sveriges officiella statistikmyndighet med data om ekonomi, befolkning och samhälle.',
      url: 'https://www.scb.se',
    },
    {
      name: 'Riksbanken',
      description: 'Sveriges centralbank med data om räntor, inflation och penningpolitik.',
      url: 'https://www.riksbank.se',
    },
    {
      name: 'Riksdagen',
      description: 'Information om riksdagsledamöter, partier och politiska beslut.',
      url: 'https://www.riksdagen.se',
    },
    {
      name: 'Konjunkturinstitutet',
      description: 'Prognoser och analyser av svensk ekonomi.',
      url: 'https://www.konj.se',
    },
  ];

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />
      <main className="flex-1 container mx-auto px-4 pt-24 pb-12">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center gap-3 mb-8">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-primary">
              <Globe className="h-6 w-6 text-primary-foreground" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-foreground">Källor</h1>
              <p className="text-muted-foreground">Datakällor som används i Macro Hub</p>
            </div>
          </div>

          <div className="grid gap-4">
            {sources.map((source) => (
              <a
                key={source.name}
                href={source.url}
                target="_blank"
                rel="noopener noreferrer"
                className="group block p-6 rounded-lg border border-border bg-card hover:border-primary/50 hover:shadow-lg transition-all"
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="text-lg font-semibold text-foreground group-hover:text-primary transition-colors">
                      {source.name}
                    </h2>
                    <p className="text-muted-foreground mt-1">{source.description}</p>
                    <p className="text-sm text-primary mt-2">{source.url}</p>
                  </div>
                  <ExternalLink className="h-5 w-5 text-muted-foreground group-hover:text-primary transition-colors flex-shrink-0" />
                </div>
              </a>
            ))}
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default Sources;
