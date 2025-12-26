import { useState } from 'react';
import { Helmet } from 'react-helmet-async';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import SwedenMap from '@/components/SwedenMap';
import { RegionVotingData } from '@/data/regionData';
import { MapPin } from 'lucide-react';

const Regions = () => {
  const [selectedRegion, setSelectedRegion] = useState<RegionVotingData | null>(null);

  return (
    <>
      <Helmet>
        <title>Regioner - Röstningsresultat per län | MacroVue</title>
        <meta name="description" content="Utforska röstningsresultat för varje region i Sverige. Se hur olika län röstade i valet med interaktiv karta." />
      </Helmet>
      
      <div className="min-h-screen bg-background">
        <Header />
        
        <main className="container mx-auto px-4 pt-24 pb-16">
          <div className="max-w-6xl mx-auto">
            <div className="text-center mb-12">
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4">
                <MapPin className="h-4 w-4" />
                Regionala valresultat
              </div>
              <h1 className="text-4xl md:text-5xl font-bold text-foreground mb-4">
                Röstningsresultat per region
              </h1>
              <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                Klicka på en region för att se detaljerade röstningsresultat för det länet.
              </p>
            </div>

            <div className="grid lg:grid-cols-2 gap-8 items-start">
              <div className="bg-card rounded-2xl border border-border p-6 shadow-lg">
                <SwedenMap 
                  onRegionSelect={setSelectedRegion}
                  selectedRegion={selectedRegion}
                />
              </div>

              <div className="bg-card rounded-2xl border border-border p-6 shadow-lg">
                {selectedRegion ? (
                  <div className="space-y-6">
                    <div className="flex items-center gap-3">
                      <div 
                        className="w-4 h-4 rounded-full"
                        style={{ backgroundColor: selectedRegion.winnerColor }}
                      />
                      <h2 className="text-2xl font-bold text-foreground">
                        {selectedRegion.name}
                      </h2>
                    </div>
                    
                    <p className="text-muted-foreground">
                      Vinnande parti: <span className="font-semibold text-foreground">{selectedRegion.winner}</span>
                    </p>

                    <div className="space-y-3">
                      <h3 className="text-lg font-semibold text-foreground">Partifördelning</h3>
                      {selectedRegion.votes
                        .sort((a, b) => b.percentage - a.percentage)
                        .map((vote) => (
                          <div key={vote.party} className="space-y-1">
                            <div className="flex justify-between text-sm">
                              <span className="font-medium text-foreground">{vote.party}</span>
                              <span className="text-muted-foreground">{vote.percentage}%</span>
                            </div>
                            <div className="h-3 bg-muted rounded-full overflow-hidden">
                              <div
                                className="h-full rounded-full transition-all duration-500"
                                style={{
                                  width: `${vote.percentage * 2.5}%`,
                                  backgroundColor: vote.color
                                }}
                              />
                            </div>
                          </div>
                        ))}
                    </div>
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center h-full min-h-[400px] text-center">
                    <MapPin className="h-12 w-12 text-muted-foreground/50 mb-4" />
                    <h2 className="text-xl font-semibold text-foreground mb-2">
                      Välj en region
                    </h2>
                    <p className="text-muted-foreground max-w-sm">
                      Klicka på en region på kartan för att se detaljerad röstningsstatistik för det länet.
                    </p>
                  </div>
                )}
              </div>
            </div>

            <div className="mt-12 bg-card rounded-2xl border border-border p-6 shadow-lg">
              <h2 className="text-2xl font-bold text-foreground mb-4">Nuvarande regeringskoalition</h2>
              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <p className="text-muted-foreground leading-relaxed">
                    Sveriges nuvarande regering är en koalitionsregering bestående av <span className="font-semibold text-foreground">Moderaterna</span>, <span className="font-semibold text-foreground">Kristdemokraterna</span> och <span className="font-semibold text-foreground">Liberalerna</span>, med stöd av <span className="font-semibold text-foreground">Sverigedemokraterna</span> genom Tidöavtalet.
                  </p>
                  <p className="text-muted-foreground leading-relaxed">
                    Regeringen tillträdde i oktober 2022 efter riksdagsvalet samma år. Statsminister är Ulf Kristersson (M), som leder den första högerregeringen på åtta år.
                  </p>
                </div>
                <div className="space-y-3">
                  <h3 className="text-lg font-semibold text-foreground">Regeringspartier</h3>
                  <div className="flex flex-wrap gap-2">
                    <span className="px-3 py-1.5 rounded-full text-sm font-medium text-white" style={{ backgroundColor: '#52BDEC' }}>Moderaterna</span>
                    <span className="px-3 py-1.5 rounded-full text-sm font-medium text-white" style={{ backgroundColor: '#005B82' }}>Kristdemokraterna</span>
                    <span className="px-3 py-1.5 rounded-full text-sm font-medium text-white" style={{ backgroundColor: '#006AB3' }}>Liberalerna</span>
                  </div>
                  <h3 className="text-lg font-semibold text-foreground mt-4">Stödparti</h3>
                  <div className="flex flex-wrap gap-2">
                    <span className="px-3 py-1.5 rounded-full text-sm font-medium text-white" style={{ backgroundColor: '#DDDD00', color: '#000' }}>Sverigedemokraterna</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-8 grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">21</div>
                <div className="text-sm text-muted-foreground">Regioner</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">8</div>
                <div className="text-sm text-muted-foreground">Riksdagspartier</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">84.2%</div>
                <div className="text-sm text-muted-foreground">Valdeltagande</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold" style={{ color: '#E8112d' }}>S</div>
                <div className="text-sm text-muted-foreground">Största parti</div>
              </div>
            </div>
          </div>
        </main>

        <Footer />
      </div>
    </>
  );
};

export default Regions;
