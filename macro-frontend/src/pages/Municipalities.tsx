import { useState } from 'react';
import { Helmet } from 'react-helmet-async';
import Header from '@/components/Header';
import Footer from '@/components/Footer';
import SwedenMunicipalityMap from '@/components/SwedenMunicipalityMap';
import { MunicipalityVotingData, getMunicipalityCoalition } from '@/data/municipalityData';
import { Building2, Users } from 'lucide-react';

const Municipalities = () => {
  const [selectedMunicipality, setSelectedMunicipality] = useState<MunicipalityVotingData | null>(null);

  return (
    <>
      <Helmet>
        <title>Kommuner - Röstningsresultat per kommun | Macro Hub</title>
        <meta name="description" content="Utforska röstningsresultat för varje kommun i Sverige. Se hur olika kommuner röstade i valet med interaktiv karta." />
      </Helmet>
      
      <div className="min-h-screen bg-background">
        <Header />
        
        <main className="container mx-auto px-4 pt-24 pb-16">
          <div className="max-w-6xl mx-auto">
            <div className="text-center mb-12">
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 text-primary text-sm font-medium mb-4">
                <Building2 className="h-4 w-4" />
                Kommunala valresultat
              </div>
              <h1 className="text-4xl md:text-5xl font-bold text-foreground mb-4">
                Röstningsresultat per kommun
              </h1>
              <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                Klicka på en kommun för att se detaljerade röstningsresultat.
              </p>
            </div>

            <div className="grid lg:grid-cols-2 gap-8 items-start">
              <div className="bg-card rounded-2xl border border-border p-6 shadow-lg">
                <SwedenMunicipalityMap 
                  onMunicipalitySelect={setSelectedMunicipality}
                  selectedMunicipality={selectedMunicipality}
                />
              </div>

              <div className="bg-card rounded-2xl border border-border p-6 shadow-lg">
                {selectedMunicipality ? (
                  <div className="space-y-6">
                    <div className="flex items-center gap-3">
                      <div 
                        className="w-4 h-4 rounded-full"
                        style={{ backgroundColor: selectedMunicipality.winnerColor }}
                      />
                      <h2 className="text-2xl font-bold text-foreground">
                        {selectedMunicipality.name}
                      </h2>
                    </div>
                    
                    <p className="text-muted-foreground">
                      Region: <span className="font-semibold text-foreground">{selectedMunicipality.region}</span>
                    </p>
                    <p className="text-muted-foreground">
                      Vinnande parti: <span className="font-semibold text-foreground">{selectedMunicipality.winner}</span>
                    </p>

                    <div className="space-y-3">
                      <h3 className="text-lg font-semibold text-foreground">Partifördelning</h3>
                      {[...selectedMunicipality.votes]
                        .sort((a, b) => b.percentage - a.percentage)
                        .map((vote) => (
                          <div key={vote.party} className="space-y-1">
                            <div className="flex justify-between text-sm">
                              <span className="font-medium text-foreground">{vote.party}</span>
                              <span className="text-muted-foreground">{vote.percentage.toLocaleString('sv-SE')} %</span>
                            </div>
                            <div className="h-3 bg-muted rounded-full overflow-hidden">
                              <div
                                className="h-full rounded-full transition-all duration-500"
                                style={{
                                  width: `${Math.min(vote.percentage * 2.5, 100)}%`,
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
                    <Building2 className="h-12 w-12 text-muted-foreground/50 mb-4" />
                    <h2 className="text-xl font-semibold text-foreground mb-2">
                      Välj en kommun
                    </h2>
                    <p className="text-muted-foreground max-w-sm">
                      Klicka på en kommun på kartan för att se detaljerad röstningsstatistik.
                    </p>
                  </div>
                )}
              </div>
            </div>

            {selectedMunicipality && (
              <div className="mt-12 bg-card rounded-2xl border border-border p-6 shadow-lg">
                <div className="flex items-center gap-3 mb-4">
                  <Users className="h-6 w-6 text-primary" />
                  <h2 className="text-2xl font-bold text-foreground">
                    Kommunalt styre i {selectedMunicipality.name}
                  </h2>
                </div>
                {(() => {
                  const coalition = getMunicipalityCoalition(selectedMunicipality.id);
                  
                  // Map full party names to abbreviations used in vote data
                  const partyNameToAbbr: Record<string, string> = {
                    "Socialdemokraterna": "S",
                    "Moderaterna": "M",
                    "Sverigedemokraterna": "SD",
                    "Centerpartiet": "C",
                    "Vänsterpartiet": "V",
                    "Kristdemokraterna": "KD",
                    "Liberalerna": "L",
                    "Miljöpartiet": "MP",
                  };
                  
                  // Calculate combined vote percentage for coalition parties
                  const coalitionVotePercentage = coalition.parties.reduce((sum, party) => {
                    const abbr = partyNameToAbbr[party.name];
                    const voteData = selectedMunicipality.votes.find(v => v.party === abbr);
                    return sum + (voteData?.percentage || 0);
                  }, 0);
                  
                  // Include support parties if they exist
                  const supportVotePercentage = coalition.supportParties?.reduce((sum, party) => {
                    const abbr = partyNameToAbbr[party.name];
                    const voteData = selectedMunicipality.votes.find(v => v.party === abbr);
                    return sum + (voteData?.percentage || 0);
                  }, 0) || 0;
                  
                  const totalCoalitionPercentage = coalitionVotePercentage + supportVotePercentage;
                  
                  return (
                    <div className="grid md:grid-cols-2 gap-6">
                      <div className="space-y-4">
                        <div className="flex items-center gap-3 flex-wrap">
                          <div className="inline-flex items-center px-3 py-1 rounded-full bg-primary/10 text-primary text-sm font-medium">
                            {coalition.name}
                          </div>
                          <div className="inline-flex items-center px-3 py-1 rounded-full bg-muted text-foreground text-sm font-semibold">
                            {totalCoalitionPercentage.toLocaleString('sv-SE', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} % av rösterna
                          </div>
                        </div>
                        <p className="text-muted-foreground leading-relaxed">
                          {coalition.description}
                        </p>
                      </div>
                      <div className="space-y-3">
                        <h3 className="text-lg font-semibold text-foreground">Styrande partier</h3>
                        <div className="flex flex-wrap gap-2">
                          {coalition.parties.map((party) => (
                            <span 
                              key={party.name}
                              className="px-3 py-1.5 rounded-full text-sm font-medium"
                              style={{ 
                                backgroundColor: party.color, 
                                color: party.textColor || '#fff' 
                              }}
                            >
                              {party.name}
                            </span>
                          ))}
                        </div>
                        {coalition.supportParties && coalition.supportParties.length > 0 && (
                          <>
                            <h3 className="text-lg font-semibold text-foreground mt-4">Stödpartier</h3>
                            <div className="flex flex-wrap gap-2">
                              {coalition.supportParties.map((party) => (
                                <span 
                                  key={party.name}
                                  className="px-3 py-1.5 rounded-full text-sm font-medium"
                                  style={{ 
                                    backgroundColor: party.color, 
                                    color: party.textColor || '#fff' 
                                  }}
                                >
                                  {party.name}
                                </span>
                              ))}
                            </div>
                          </>
                        )}
                      </div>
                    </div>
                  );
                })()}
              </div>
            )}

            <div className="mt-8 grid grid-cols-2 md:grid-cols-4 gap-4">
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">290</div>
                <div className="text-sm text-muted-foreground">Kommuner</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">8</div>
                <div className="text-sm text-muted-foreground">Riksdagspartier</div>
              </div>
              <div className="bg-card rounded-xl border border-border p-4 text-center">
                <div className="text-3xl font-bold text-foreground">84,2 %</div>
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

export default Municipalities;
