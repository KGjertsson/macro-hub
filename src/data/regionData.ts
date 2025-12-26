export interface RegionCoalition {
  name: string;
  parties: { name: string; color: string; textColor?: string }[];
  supportParties?: { name: string; color: string; textColor?: string }[];
  description: string;
}

export interface RegionVotingData {
  id: string;
  name: string;
  votes: {
    party: string;
    percentage: number;
    color: string;
  }[];
  winner: string;
  winnerColor: string;
}

// Regional coalition data - reflects actual regional government coalitions
export const getRegionCoalition = (regionId: string): RegionCoalition => {
  const coalitions: Record<string, RegionCoalition> = {
    stockholm: {
      name: "Blågrönt styre",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Kristdemokraterna", color: "#005B82" },
        { name: "Liberalerna", color: "#006AB3" },
        { name: "Miljöpartiet", color: "#83CF39" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Region Stockholm styrs av en bred koalition kallad Blågrönt styre, bestående av M, KD, L, MP och C."
    },
    vastragotaland: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Vänsterpartiet", color: "#DA291C" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Västra Götalandsregionen styrs av en rödgrön koalition med S, V och MP."
    },
    skane: {
      name: "Sverigedemokraterna & Allians",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Sverigedemokraterna", color: "#DDDD00", textColor: "#000" },
        { name: "Kristdemokraterna", color: "#005B82" },
      ],
      description: "Region Skåne styrs av M, SD och KD i en högerkoalition sedan 2022."
    },
    ostergotland: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Region Östergötland styrs av S och C med stöd av V."
    },
    uppsala: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Vänsterpartiet", color: "#DA291C" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Region Uppsala styrs av en rödgrön majoritet med S, V och MP."
    },
    jonkoping: {
      name: "Alliansstyre",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Kristdemokraterna", color: "#005B82" },
        { name: "Centerpartiet", color: "#009933" },
        { name: "Liberalerna", color: "#006AB3" },
      ],
      description: "Region Jönköping styrs av en traditionell allianskoalition med M, KD, C och L."
    },
    kronoberg: {
      name: "Blocköverskridande",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Region Kronoberg styrs av en bred blocköverskridande koalition med S, M och C."
    },
    kalmar: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Region Kalmar styrs av S tillsammans med C och MP."
    },
    gotland: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Miljöpartiet", color: "#83CF39" },
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Region Gotland styrs av en rödgrön koalition med S, MP och V."
    },
    blekinge: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Region Blekinge styrs av S med stöd av V och MP."
    },
    halland: {
      name: "Alliansstyre",
      parties: [
        { name: "Moderaterna", color: "#52BDEC" },
        { name: "Kristdemokraterna", color: "#005B82" },
        { name: "Liberalerna", color: "#006AB3" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Region Halland styrs av en allianskoalition med M, KD, L och C."
    },
    varmland: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Region Värmland styrs av S tillsammans med C."
    },
    orebro: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Vänsterpartiet", color: "#DA291C" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Region Örebro styrs av en rödgrön koalition."
    },
    vastmanland: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Region Västmanland styrs av S med stöd av V."
    },
    dalarna: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Region Dalarna styrs av S tillsammans med C."
    },
    gavleborg: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Region Gävleborg styrs av S med stöd av V."
    },
    vasternorrland: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Region Västernorrland styrs av S och C."
    },
    jamtland: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Miljöpartiet", color: "#83CF39" },
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Region Jämtland Härjedalen styrs av en rödgrön koalition."
    },
    vasterbotten: {
      name: "Rödgrönt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Vänsterpartiet", color: "#DA291C" },
        { name: "Miljöpartiet", color: "#83CF39" },
      ],
      description: "Region Västerbotten styrs av S, V och MP."
    },
    norrbotten: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
      ],
      supportParties: [
        { name: "Vänsterpartiet", color: "#DA291C" },
      ],
      description: "Region Norrbotten styrs av S med stöd av V."
    },
    sodermanland: {
      name: "Socialdemokratiskt styre",
      parties: [
        { name: "Socialdemokraterna", color: "#E8112d" },
        { name: "Centerpartiet", color: "#009933" },
      ],
      description: "Region Sörmland styrs av S tillsammans med C."
    },
  };

  return coalitions[regionId] || {
    name: "Okänt styre",
    parties: [],
    description: "Information om regionens styre saknas."
  };
};

export const regionVotingData: RegionVotingData[] = [
  {
    id: "stockholm",
    name: "Stockholm",
    votes: [
      { party: "S", percentage: 24.5, color: "#E8112d" },
      { party: "M", percentage: 23.1, color: "#52BDEC" },
      { party: "SD", percentage: 15.2, color: "#DDDD00" },
      { party: "C", percentage: 8.3, color: "#009933" },
      { party: "V", percentage: 9.1, color: "#DA291C" },
      { party: "KD", percentage: 6.2, color: "#000077" },
      { party: "L", percentage: 5.8, color: "#006AB3" },
      { party: "MP", percentage: 6.1, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "vastragotaland",
    name: "Västra Götaland",
    votes: [
      { party: "S", percentage: 28.2, color: "#E8112d" },
      { party: "M", percentage: 18.5, color: "#52BDEC" },
      { party: "SD", percentage: 19.8, color: "#DDDD00" },
      { party: "C", percentage: 7.1, color: "#009933" },
      { party: "V", percentage: 8.9, color: "#DA291C" },
      { party: "KD", percentage: 6.8, color: "#000077" },
      { party: "L", percentage: 4.2, color: "#006AB3" },
      { party: "MP", percentage: 4.8, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "skane",
    name: "Skåne",
    votes: [
      { party: "S", percentage: 25.1, color: "#E8112d" },
      { party: "M", percentage: 20.3, color: "#52BDEC" },
      { party: "SD", percentage: 24.6, color: "#DDDD00" },
      { party: "C", percentage: 5.8, color: "#009933" },
      { party: "V", percentage: 7.2, color: "#DA291C" },
      { party: "KD", percentage: 5.9, color: "#000077" },
      { party: "L", percentage: 4.5, color: "#006AB3" },
      { party: "MP", percentage: 4.1, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "ostergotland",
    name: "Östergötland",
    votes: [
      { party: "S", percentage: 29.5, color: "#E8112d" },
      { party: "M", percentage: 17.8, color: "#52BDEC" },
      { party: "SD", percentage: 21.2, color: "#DDDD00" },
      { party: "C", percentage: 6.4, color: "#009933" },
      { party: "V", percentage: 8.1, color: "#DA291C" },
      { party: "KD", percentage: 6.5, color: "#000077" },
      { party: "L", percentage: 4.1, color: "#006AB3" },
      { party: "MP", percentage: 4.6, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "uppsala",
    name: "Uppsala",
    votes: [
      { party: "S", percentage: 26.8, color: "#E8112d" },
      { party: "M", percentage: 21.2, color: "#52BDEC" },
      { party: "SD", percentage: 16.5, color: "#DDDD00" },
      { party: "C", percentage: 8.9, color: "#009933" },
      { party: "V", percentage: 9.4, color: "#DA291C" },
      { party: "KD", percentage: 5.1, color: "#000077" },
      { party: "L", percentage: 5.6, color: "#006AB3" },
      { party: "MP", percentage: 5.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "jonkoping",
    name: "Jönköping",
    votes: [
      { party: "S", percentage: 23.4, color: "#E8112d" },
      { party: "M", percentage: 19.1, color: "#52BDEC" },
      { party: "SD", percentage: 20.8, color: "#DDDD00" },
      { party: "C", percentage: 8.2, color: "#009933" },
      { party: "V", percentage: 5.6, color: "#DA291C" },
      { party: "KD", percentage: 12.4, color: "#000077" },
      { party: "L", percentage: 4.8, color: "#006AB3" },
      { party: "MP", percentage: 3.9, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "kronoberg",
    name: "Kronoberg",
    votes: [
      { party: "S", percentage: 27.1, color: "#E8112d" },
      { party: "M", percentage: 17.5, color: "#52BDEC" },
      { party: "SD", percentage: 22.3, color: "#DDDD00" },
      { party: "C", percentage: 7.8, color: "#009933" },
      { party: "V", percentage: 6.9, color: "#DA291C" },
      { party: "KD", percentage: 8.1, color: "#000077" },
      { party: "L", percentage: 4.2, color: "#006AB3" },
      { party: "MP", percentage: 4.3, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "kalmar",
    name: "Kalmar",
    votes: [
      { party: "S", percentage: 30.2, color: "#E8112d" },
      { party: "M", percentage: 16.8, color: "#52BDEC" },
      { party: "SD", percentage: 21.5, color: "#DDDD00" },
      { party: "C", percentage: 8.6, color: "#009933" },
      { party: "V", percentage: 6.4, color: "#DA291C" },
      { party: "KD", percentage: 6.2, color: "#000077" },
      { party: "L", percentage: 4.1, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "gotland",
    name: "Gotland",
    votes: [
      { party: "S", percentage: 28.5, color: "#E8112d" },
      { party: "M", percentage: 19.2, color: "#52BDEC" },
      { party: "SD", percentage: 17.8, color: "#DDDD00" },
      { party: "C", percentage: 10.1, color: "#009933" },
      { party: "V", percentage: 7.8, color: "#DA291C" },
      { party: "KD", percentage: 4.9, color: "#000077" },
      { party: "L", percentage: 4.5, color: "#006AB3" },
      { party: "MP", percentage: 5.6, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "blekinge",
    name: "Blekinge",
    votes: [
      { party: "S", percentage: 29.8, color: "#E8112d" },
      { party: "M", percentage: 16.5, color: "#52BDEC" },
      { party: "SD", percentage: 24.1, color: "#DDDD00" },
      { party: "C", percentage: 5.9, color: "#009933" },
      { party: "V", percentage: 7.1, color: "#DA291C" },
      { party: "KD", percentage: 5.8, color: "#000077" },
      { party: "L", percentage: 4.6, color: "#006AB3" },
      { party: "MP", percentage: 4.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "halland",
    name: "Halland",
    votes: [
      { party: "S", percentage: 24.2, color: "#E8112d" },
      { party: "M", percentage: 22.8, color: "#52BDEC" },
      { party: "SD", percentage: 19.5, color: "#DDDD00" },
      { party: "C", percentage: 7.9, color: "#009933" },
      { party: "V", percentage: 6.8, color: "#DA291C" },
      { party: "KD", percentage: 7.4, color: "#000077" },
      { party: "L", percentage: 5.1, color: "#006AB3" },
      { party: "MP", percentage: 4.7, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "varmland",
    name: "Värmland",
    votes: [
      { party: "S", percentage: 32.1, color: "#E8112d" },
      { party: "M", percentage: 15.4, color: "#52BDEC" },
      { party: "SD", percentage: 21.8, color: "#DDDD00" },
      { party: "C", percentage: 8.2, color: "#009933" },
      { party: "V", percentage: 7.5, color: "#DA291C" },
      { party: "KD", percentage: 5.6, color: "#000077" },
      { party: "L", percentage: 3.9, color: "#006AB3" },
      { party: "MP", percentage: 3.8, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "orebro",
    name: "Örebro",
    votes: [
      { party: "S", percentage: 30.5, color: "#E8112d" },
      { party: "M", percentage: 17.2, color: "#52BDEC" },
      { party: "SD", percentage: 20.1, color: "#DDDD00" },
      { party: "C", percentage: 7.5, color: "#009933" },
      { party: "V", percentage: 8.2, color: "#DA291C" },
      { party: "KD", percentage: 5.9, color: "#000077" },
      { party: "L", percentage: 4.3, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "vastmanland",
    name: "Västmanland",
    votes: [
      { party: "S", percentage: 31.2, color: "#E8112d" },
      { party: "M", percentage: 16.8, color: "#52BDEC" },
      { party: "SD", percentage: 21.5, color: "#DDDD00" },
      { party: "C", percentage: 6.4, color: "#009933" },
      { party: "V", percentage: 8.1, color: "#DA291C" },
      { party: "KD", percentage: 5.4, color: "#000077" },
      { party: "L", percentage: 4.5, color: "#006AB3" },
      { party: "MP", percentage: 4.3, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "dalarna",
    name: "Dalarna",
    votes: [
      { party: "S", percentage: 33.8, color: "#E8112d" },
      { party: "M", percentage: 14.5, color: "#52BDEC" },
      { party: "SD", percentage: 20.2, color: "#DDDD00" },
      { party: "C", percentage: 9.1, color: "#009933" },
      { party: "V", percentage: 7.8, color: "#DA291C" },
      { party: "KD", percentage: 5.1, color: "#000077" },
      { party: "L", percentage: 3.8, color: "#006AB3" },
      { party: "MP", percentage: 4.1, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "gavleborg",
    name: "Gävleborg",
    votes: [
      { party: "S", percentage: 34.2, color: "#E8112d" },
      { party: "M", percentage: 14.1, color: "#52BDEC" },
      { party: "SD", percentage: 21.8, color: "#DDDD00" },
      { party: "C", percentage: 7.2, color: "#009933" },
      { party: "V", percentage: 8.5, color: "#DA291C" },
      { party: "KD", percentage: 4.8, color: "#000077" },
      { party: "L", percentage: 3.6, color: "#006AB3" },
      { party: "MP", percentage: 4.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "vasternorrland",
    name: "Västernorrland",
    votes: [
      { party: "S", percentage: 35.1, color: "#E8112d" },
      { party: "M", percentage: 13.8, color: "#52BDEC" },
      { party: "SD", percentage: 19.5, color: "#DDDD00" },
      { party: "C", percentage: 8.9, color: "#009933" },
      { party: "V", percentage: 8.2, color: "#DA291C" },
      { party: "KD", percentage: 4.5, color: "#000077" },
      { party: "L", percentage: 3.5, color: "#006AB3" },
      { party: "MP", percentage: 4.8, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "jamtland",
    name: "Jämtland",
    votes: [
      { party: "S", percentage: 31.5, color: "#E8112d" },
      { party: "M", percentage: 14.2, color: "#52BDEC" },
      { party: "SD", percentage: 16.8, color: "#DDDD00" },
      { party: "C", percentage: 12.5, color: "#009933" },
      { party: "V", percentage: 9.1, color: "#DA291C" },
      { party: "KD", percentage: 4.2, color: "#000077" },
      { party: "L", percentage: 4.1, color: "#006AB3" },
      { party: "MP", percentage: 5.8, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "vasterbotten",
    name: "Västerbotten",
    votes: [
      { party: "S", percentage: 33.2, color: "#E8112d" },
      { party: "M", percentage: 14.8, color: "#52BDEC" },
      { party: "SD", percentage: 15.2, color: "#DDDD00" },
      { party: "C", percentage: 10.5, color: "#009933" },
      { party: "V", percentage: 10.8, color: "#DA291C" },
      { party: "KD", percentage: 4.1, color: "#000077" },
      { party: "L", percentage: 4.2, color: "#006AB3" },
      { party: "MP", percentage: 5.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "norrbotten",
    name: "Norrbotten",
    votes: [
      { party: "S", percentage: 36.8, color: "#E8112d" },
      { party: "M", percentage: 12.5, color: "#52BDEC" },
      { party: "SD", percentage: 17.2, color: "#DDDD00" },
      { party: "C", percentage: 9.8, color: "#009933" },
      { party: "V", percentage: 10.2, color: "#DA291C" },
      { party: "KD", percentage: 3.8, color: "#000077" },
      { party: "L", percentage: 3.5, color: "#006AB3" },
      { party: "MP", percentage: 4.5, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  },
  {
    id: "sodermanland",
    name: "Södermanland",
    votes: [
      { party: "S", percentage: 30.8, color: "#E8112d" },
      { party: "M", percentage: 17.5, color: "#52BDEC" },
      { party: "SD", percentage: 22.1, color: "#DDDD00" },
      { party: "C", percentage: 6.2, color: "#009933" },
      { party: "V", percentage: 7.8, color: "#DA291C" },
      { party: "KD", percentage: 5.2, color: "#000077" },
      { party: "L", percentage: 4.4, color: "#006AB3" },
      { party: "MP", percentage: 4.2, color: "#83CF39" },
    ],
    winner: "S",
    winnerColor: "#E8112d"
  }
];
