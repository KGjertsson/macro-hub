export interface MunicipalityCoalition {
  name: string;
  parties: { name: string; color: string; textColor?: string }[];
  supportParties?: { name: string; color: string; textColor?: string }[];
  description: string;
}

export interface MunicipalityVotingData {
  id: string;
  name: string;
  region: string;
  votes: {
    party: string;
    percentage: number;
    color: string;
  }[];
  winner: string;
  winnerColor: string;
}

// Municipality coalition data
export const getMunicipalityCoalition = (municipalityId: string): MunicipalityCoalition => {
  const coalitions: Record<string, MunicipalityCoalition> = {
    stockholm: {
      name: "Grönblått styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Miljöpartiet", color: "#83CF39" },
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Stockholms stad styrs av en rödgrön koalition med S, MP och V."
    },
    goteborg: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Miljöpartiet", color: "#83CF39" },
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Göteborgs stad styrs av en rödgrön koalition med S, MP och V."
    },
    malmo: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Liberalerna", color: "#006AB3" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Malmö stad styrs av S med stöd av L och MP."
    },
    uppsala: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Vänsterpartiet", color: "#DA291C" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Uppsala kommun styrs av en rödgrön majoritet."
    },
    vasteras: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Västerås stad styrs av S med stöd av V."
    },
    orebro: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Örebro kommun styrs av S och C."
    },
    linkoping: {
      name: "Alliansstyre",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Liberalerna", color: "#006AB3" },
        { name: "Kristdemokraterna", color: "#005B82" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Linköpings kommun styrs av en allianskoalition."
    },
    helsingborg: {
      name: "Alliansstyre",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Liberalerna", color: "#006AB3" },
        { name: "Kristdemokraterna", color: "#005B82" },
      ],
      description: "Helsingborgs stad styrs av M, L och KD."
    },
    jonkoping: {
      name: "Alliansstyre",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Kristdemokraterna", color: "#005B82" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Jönköpings kommun styrs av M, KD och C."
    },
    norrkoping: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Norrköpings kommun styrs av S och C."
    },
    lund: {
      name: "Alliansstyre",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Liberalerna", color: "#006AB3" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Lunds kommun styrs av M, L och C."
    },
    umea: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Umeå kommun styrs av S och MP."
    },
    gavle: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Gävle kommun styrs av S med stöd av V."
    },
    sundsvall: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Sundsvalls kommun styrs av S och C."
    },
    boras: {
      name: "Blocköverskridande",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      description: "Borås stad styrs av ett blocköverskridande samarbete."
    },
    eskilstuna: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Eskilstuna kommun styrs av S med stöd av V och MP."
    },
    halmstad: {
      name: "Alliansstyre",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Centerpartiet", color: "#009933" },
        { name: "Kristdemokraterna", color: "#005B82" },
      ],
      description: "Halmstads kommun styrs av M, C och KD."
    },
    vaxjo: {
      name: "Blocköverskridande",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Växjö kommun styrs av ett blocköverskridande samarbete."
    },
    karlstad: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Karlstads kommun styrs av S och C."
    },
    lulea: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Luleå kommun styrs av S med stöd av V."
    },
  };

  return coalitions[municipalityId] || {
    name: "Okänt styre",
    parties: [],
    description: "Information om kommunens styre saknas."
  };
};

export const municipalityVotingData: MunicipalityVotingData[] = [
  {
    id: "stockholm",
    name: "Stockholm",
    region: "Stockholm",
    votes: [
      { party: "S", percentage: 25.8, color: "#E8112d" },
      { party: "M", percentage: 24.2, color: "#52BDEC" },
      { party: "SD", percentage: 11.5, color: "#DDDD00" },
      { party: "C", percentage: 6.8, color: "#009933" },
      { party: "V", percentage: 11.2, color: "#DA291C" },
      { party: "KD", percentage: 4.8, color: "#000077" },
      { party: "L", percentage: 7.2, color: "#006AB3" },
      { party: "MP", percentage: 7.1, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "goteborg",
    name: "Göteborg",
    region: "Västra Götaland",
    votes: [
      { party: "S", percentage: 28.5, color: "#E8112d" },
      { party: "M", percentage: 19.2, color: "#52BDEC" },
      { party: "SD", percentage: 15.8, color: "#DDDD00" },
      { party: "C", percentage: 5.4, color: "#009933" },
      { party: "V", percentage: 12.1, color: "#DA291C" },
      { party: "KD", percentage: 5.2, color: "#000077" },
      { party: "L", percentage: 5.8, color: "#006AB3" },
      { party: "MP", percentage: 6.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "malmo",
    name: "Malmö",
    region: "Skåne",
    votes: [
      { party: "S", percentage: 32.1, color: "#E8112d" },
      { party: "M", percentage: 16.5, color: "#52BDEC" },
      { party: "SD", percentage: 20.2, color: "#DDDD00" },
      { party: "C", percentage: 4.2, color: "#009933" },
      { party: "V", percentage: 10.8, color: "#DA291C" },
      { party: "KD", percentage: 3.9, color: "#000077" },
      { party: "L", percentage: 5.1, color: "#006AB3" },
      { party: "MP", percentage: 5.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "uppsala",
    name: "Uppsala",
    region: "Uppsala",
    votes: [
      { party: "S", percentage: 27.2, color: "#E8112d" },
      { party: "M", percentage: 20.5, color: "#52BDEC" },
      { party: "SD", percentage: 14.8, color: "#DDDD00" },
      { party: "C", percentage: 7.5, color: "#009933" },
      { party: "V", percentage: 11.2, color: "#DA291C" },
      { party: "KD", percentage: 4.5, color: "#000077" },
      { party: "L", percentage: 6.8, color: "#006AB3" },
      { party: "MP", percentage: 6.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "vasteras",
    name: "Västerås",
    region: "Västmanland",
    votes: [
      { party: "S", percentage: 32.8, color: "#E8112d" },
      { party: "M", percentage: 16.2, color: "#52BDEC" },
      { party: "SD", percentage: 19.5, color: "#DDDD00" },
      { party: "C", percentage: 5.8, color: "#009933" },
      { party: "V", percentage: 9.5, color: "#DA291C" },
      { party: "KD", percentage: 5.2, color: "#000077" },
      { party: "L", percentage: 4.8, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "orebro",
    name: "Örebro",
    region: "Örebro",
    votes: [
      { party: "S", percentage: 31.5, color: "#E8112d" },
      { party: "M", percentage: 17.8, color: "#52BDEC" },
      { party: "SD", percentage: 18.2, color: "#DDDD00" },
      { party: "C", percentage: 6.5, color: "#009933" },
      { party: "V", percentage: 9.8, color: "#DA291C" },
      { party: "KD", percentage: 5.5, color: "#000077" },
      { party: "L", percentage: 4.9, color: "#006AB3" },
      { party: "MP", percentage: 4.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "linkoping",
    name: "Linköping",
    region: "Östergötland",
    votes: [
      { party: "S", percentage: 25.8, color: "#E8112d" },
      { party: "M", percentage: 22.5, color: "#52BDEC" },
      { party: "SD", percentage: 17.2, color: "#DDDD00" },
      { party: "C", percentage: 6.8, color: "#009933" },
      { party: "V", percentage: 8.5, color: "#DA291C" },
      { party: "KD", percentage: 6.2, color: "#000077" },
      { party: "L", percentage: 6.5, color: "#006AB3" },
      { party: "MP", percentage: 5.1, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "helsingborg",
    name: "Helsingborg",
    region: "Skåne",
    votes: [
      { party: "S", percentage: 23.2, color: "#E8112d" },
      { party: "M", percentage: 24.8, color: "#52BDEC" },
      { party: "SD", percentage: 22.5, color: "#DDDD00" },
      { party: "C", percentage: 4.5, color: "#009933" },
      { party: "V", percentage: 7.2, color: "#DA291C" },
      { party: "KD", percentage: 5.8, color: "#000077" },
      { party: "L", percentage: 5.5, color: "#006AB3" },
      { party: "MP", percentage: 4.8, color: "#83CF39" },
    ],
    winner: "M",
    winnerColor: "#52BDEC"
  },
  {
    id: "jonkoping",
    name: "Jönköping",
    region: "Jönköping",
    votes: [
      { party: "S", percentage: 22.5, color: "#E8112d" },
      { party: "M", percentage: 21.2, color: "#52BDEC" },
      { party: "SD", percentage: 18.8, color: "#DDDD00" },
      { party: "C", percentage: 7.5, color: "#009933" },
      { party: "V", percentage: 5.8, color: "#DA291C" },
      { party: "KD", percentage: 14.2, color: "#000077" },
      { party: "L", percentage: 4.8, color: "#006AB3" },
      { party: "MP", percentage: 3.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "norrkoping",
    name: "Norrköping",
    region: "Östergötland",
    votes: [
      { party: "S", percentage: 30.2, color: "#E8112d" },
      { party: "M", percentage: 16.8, color: "#52BDEC" },
      { party: "SD", percentage: 22.5, color: "#DDDD00" },
      { party: "C", percentage: 5.5, color: "#009933" },
      { party: "V", percentage: 8.2, color: "#DA291C" },
      { party: "KD", percentage: 5.8, color: "#000077" },
      { party: "L", percentage: 4.5, color: "#006AB3" },
      { party: "MP", percentage: 4.8, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "lund",
    name: "Lund",
    region: "Skåne",
    votes: [
      { party: "S", percentage: 22.8, color: "#E8112d" },
      { party: "M", percentage: 23.5, color: "#52BDEC" },
      { party: "SD", percentage: 12.5, color: "#DDDD00" },
      { party: "C", percentage: 7.2, color: "#009933" },
      { party: "V", percentage: 10.5, color: "#DA291C" },
      { party: "KD", percentage: 4.8, color: "#000077" },
      { party: "L", percentage: 8.2, color: "#006AB3" },
      { party: "MP", percentage: 8.8, color: "#83CF39" },
    ],
    winner: "M",
    winnerColor: "#52BDEC"
  },
  {
    id: "umea",
    name: "Umeå",
    region: "Västerbotten",
    votes: [
      { party: "S", percentage: 33.5, color: "#E8112d" },
      { party: "M", percentage: 15.2, color: "#52BDEC" },
      { party: "SD", percentage: 13.8, color: "#DDDD00" },
      { party: "C", percentage: 8.5, color: "#009933" },
      { party: "V", percentage: 12.8, color: "#DA291C" },
      { party: "KD", percentage: 3.5, color: "#000077" },
      { party: "L", percentage: 4.8, color: "#006AB3" },
      { party: "MP", percentage: 6.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "gavle",
    name: "Gävle",
    region: "Gävleborg",
    votes: [
      { party: "S", percentage: 35.2, color: "#E8112d" },
      { party: "M", percentage: 14.5, color: "#52BDEC" },
      { party: "SD", percentage: 20.8, color: "#DDDD00" },
      { party: "C", percentage: 5.8, color: "#009933" },
      { party: "V", percentage: 9.5, color: "#DA291C" },
      { party: "KD", percentage: 4.2, color: "#000077" },
      { party: "L", percentage: 4.1, color: "#006AB3" },
      { party: "MP", percentage: 4.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "sundsvall",
    name: "Sundsvall",
    region: "Västernorrland",
    votes: [
      { party: "S", percentage: 34.8, color: "#E8112d" },
      { party: "M", percentage: 15.2, color: "#52BDEC" },
      { party: "SD", percentage: 18.5, color: "#DDDD00" },
      { party: "C", percentage: 7.8, color: "#009933" },
      { party: "V", percentage: 9.2, color: "#DA291C" },
      { party: "KD", percentage: 4.5, color: "#000077" },
      { party: "L", percentage: 3.8, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "boras",
    name: "Borås",
    region: "Västra Götaland",
    votes: [
      { party: "S", percentage: 26.5, color: "#E8112d" },
      { party: "M", percentage: 21.8, color: "#52BDEC" },
      { party: "SD", percentage: 19.2, color: "#DDDD00" },
      { party: "C", percentage: 5.8, color: "#009933" },
      { party: "V", percentage: 7.5, color: "#DA291C" },
      { party: "KD", percentage: 8.2, color: "#000077" },
      { party: "L", percentage: 4.5, color: "#006AB3" },
      { party: "MP", percentage: 4.8, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "eskilstuna",
    name: "Eskilstuna",
    region: "Södermanland",
    votes: [
      { party: "S", percentage: 33.5, color: "#E8112d" },
      { party: "M", percentage: 15.2, color: "#52BDEC" },
      { party: "SD", percentage: 22.8, color: "#DDDD00" },
      { party: "C", percentage: 4.8, color: "#009933" },
      { party: "V", percentage: 8.5, color: "#DA291C" },
      { party: "KD", percentage: 4.8, color: "#000077" },
      { party: "L", percentage: 4.2, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "halmstad",
    name: "Halmstad",
    region: "Halland",
    votes: [
      { party: "S", percentage: 24.8, color: "#E8112d" },
      { party: "M", percentage: 23.5, color: "#52BDEC" },
      { party: "SD", percentage: 19.8, color: "#DDDD00" },
      { party: "C", percentage: 6.8, color: "#009933" },
      { party: "V", percentage: 6.5, color: "#DA291C" },
      { party: "KD", percentage: 7.2, color: "#000077" },
      { party: "L", percentage: 5.2, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "vaxjo",
    name: "Växjö",
    region: "Kronoberg",
    votes: [
      { party: "S", percentage: 25.2, color: "#E8112d" },
      { party: "M", percentage: 20.8, color: "#52BDEC" },
      { party: "SD", percentage: 18.5, color: "#DDDD00" },
      { party: "C", percentage: 8.2, color: "#009933" },
      { party: "V", percentage: 7.8, color: "#DA291C" },
      { party: "KD", percentage: 8.5, color: "#000077" },
      { party: "L", percentage: 4.8, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "karlstad",
    name: "Karlstad",
    region: "Värmland",
    votes: [
      { party: "S", percentage: 30.2, color: "#E8112d" },
      { party: "M", percentage: 18.5, color: "#52BDEC" },
      { party: "SD", percentage: 17.8, color: "#DDDD00" },
      { party: "C", percentage: 8.2, color: "#009933" },
      { party: "V", percentage: 9.5, color: "#DA291C" },
      { party: "KD", percentage: 5.2, color: "#000077" },
      { party: "L", percentage: 4.8, color: "#006AB3" },
      { party: "MP", percentage: 4.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "lulea",
    name: "Luleå",
    region: "Norrbotten",
    votes: [
      { party: "S", percentage: 38.5, color: "#E8112d" },
      { party: "M", percentage: 12.8, color: "#52BDEC" },
      { party: "SD", percentage: 16.2, color: "#DDDD00" },
      { party: "C", percentage: 6.5, color: "#009933" },
      { party: "V", percentage: 12.5, color: "#DA291C" },
      { party: "KD", percentage: 3.8, color: "#000077" },
      { party: "L", percentage: 3.5, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
];
