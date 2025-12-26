export interface Party {
  id: string;
  name: string;
  shortName: string;
  seats: number;
  color: string;
  ideology: string;
}

export interface Politician {
  id: string;
  name: string;
  party: string;
  role: string;
  constituency: string;
  imageUrl: string;
  achievements: string[];
  yearsInParliament: number;
  committees: string[];
}

export interface ElectionResult {
  year: number;
  parties: Omit<Party, 'ideology'>[];
}

// Swedish national elections (every 4 years)
export const electionYears = [2022, 2018, 2014, 2010, 2006, 2002, 1998, 1994, 1991, 1988] as const;

// Historical election results
export const electionResults: ElectionResult[] = [
  {
    year: 2022,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 107, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 68, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 73, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 24, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 24, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 19, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 16, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 18, color: '#83CF39' },
    ]
  },
  {
    year: 2018,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 100, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 70, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 62, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 31, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 28, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 22, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 20, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 16, color: '#83CF39' },
    ]
  },
  {
    year: 2014,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 113, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 84, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 49, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 22, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 21, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 16, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 19, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 25, color: '#83CF39' },
    ]
  },
  {
    year: 2010,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 112, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 107, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 20, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 23, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 19, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 19, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 24, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 25, color: '#83CF39' },
    ]
  },
  {
    year: 2006,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 130, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 97, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 0, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 29, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 22, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 24, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 28, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 19, color: '#83CF39' },
    ]
  },
  {
    year: 2002,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 144, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 55, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 0, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 22, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 30, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 33, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 48, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 17, color: '#83CF39' },
    ]
  },
  {
    year: 1998,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 131, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 82, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 0, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 18, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 43, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 42, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 17, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 16, color: '#83CF39' },
    ]
  },
  {
    year: 1994,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 161, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 80, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 0, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 27, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 22, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 15, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 26, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 18, color: '#83CF39' },
    ]
  },
  {
    year: 1991,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 138, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 80, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 0, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 31, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 16, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 26, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 33, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 0, color: '#83CF39' },
      { id: 'NyD', name: 'Ny Demokrati', shortName: 'NyD', seats: 25, color: '#888888' },
    ]
  },
  {
    year: 1988,
    parties: [
      { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 156, color: '#E8112d' },
      { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 66, color: '#52BDEC' },
      { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 0, color: '#DDDD00' },
      { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 42, color: '#009933' },
      { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 21, color: '#DA291C' },
      { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 0, color: '#000077' },
      { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 44, color: '#006AB3' },
      { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 20, color: '#83CF39' },
    ]
  },
];

// Current parties (2022 election)
export const parties: Party[] = [
  { id: 'S', name: 'Socialdemokraterna', shortName: 'S', seats: 107, color: '#E8112d', ideology: 'Social Democracy' },
  { id: 'M', name: 'Moderaterna', shortName: 'M', seats: 68, color: '#52BDEC', ideology: 'Liberal Conservatism' },
  { id: 'SD', name: 'Sverigedemokraterna', shortName: 'SD', seats: 73, color: '#DDDD00', ideology: 'National Conservatism' },
  { id: 'C', name: 'Centerpartiet', shortName: 'C', seats: 24, color: '#009933', ideology: 'Agrarian Liberalism' },
  { id: 'V', name: 'Vänsterpartiet', shortName: 'V', seats: 24, color: '#DA291C', ideology: 'Democratic Socialism' },
  { id: 'KD', name: 'Kristdemokraterna', shortName: 'KD', seats: 19, color: '#000077', ideology: 'Christian Democracy' },
  { id: 'L', name: 'Liberalerna', shortName: 'L', seats: 16, color: '#006AB3', ideology: 'Social Liberalism' },
  { id: 'MP', name: 'Miljöpartiet', shortName: 'MP', seats: 18, color: '#83CF39', ideology: 'Green Politics' },
];

export const getPartiesForYear = (year: number) => {
  const result = electionResults.find(e => e.year === year);
  return result?.parties.filter(p => p.seats > 0) || parties;
};

export const politicians: Politician[] = [
  {
    id: '1',
    name: 'Anna Lindberg',
    party: 'S',
    role: 'Minister of Finance',
    constituency: 'Stockholm',
    imageUrl: '',
    achievements: [
      'Led pension reform committee 2022',
      'Authored Climate Investment Act',
      'Championed workers rights legislation'
    ],
    yearsInParliament: 12,
    committees: ['Finance Committee', 'EU Affairs']
  },
  {
    id: '2',
    name: 'Erik Johansson',
    party: 'M',
    role: 'Opposition Leader',
    constituency: 'Gothenburg',
    imageUrl: '',
    achievements: [
      'Tax reform proposal 2023',
      'Defense spending increase advocacy',
      'Small business deregulation'
    ],
    yearsInParliament: 8,
    committees: ['Defense Committee', 'Finance Committee']
  },
  {
    id: '3',
    name: 'Maria Svensson',
    party: 'C',
    role: 'Committee Chair',
    constituency: 'Uppsala',
    imageUrl: '',
    achievements: [
      'Rural development initiative',
      'Agricultural subsidy reform',
      'Green energy transition plan'
    ],
    yearsInParliament: 15,
    committees: ['Environment Committee', 'Agriculture Committee']
  },
  {
    id: '4',
    name: 'Karl Andersson',
    party: 'SD',
    role: 'Deputy Speaker',
    constituency: 'Malmö',
    imageUrl: '',
    achievements: [
      'Immigration policy reform',
      'Law enforcement funding bill',
      'National security framework'
    ],
    yearsInParliament: 6,
    committees: ['Justice Committee', 'Constitutional Committee']
  },
  {
    id: '5',
    name: 'Sofia Bergström',
    party: 'V',
    role: 'Party Spokesperson',
    constituency: 'Lund',
    imageUrl: '',
    achievements: [
      'Housing affordability act',
      'Healthcare expansion bill',
      'Minimum wage legislation'
    ],
    yearsInParliament: 10,
    committees: ['Social Affairs Committee', 'Housing Committee']
  },
  {
    id: '6',
    name: 'Lars Nilsson',
    party: 'KD',
    role: 'Committee Member',
    constituency: 'Jönköping',
    imageUrl: '',
    achievements: [
      'Family policy reform',
      'Education choice expansion',
      'Faith community protection act'
    ],
    yearsInParliament: 4,
    committees: ['Education Committee', 'Social Affairs Committee']
  },
  {
    id: '7',
    name: 'Emma Karlsson',
    party: 'L',
    role: 'Deputy Committee Chair',
    constituency: 'Linköping',
    imageUrl: '',
    achievements: [
      'Digital infrastructure bill',
      'Privacy protection legislation',
      'Innovation fund establishment'
    ],
    yearsInParliament: 7,
    committees: ['Industry Committee', 'Digital Affairs Committee']
  },
  {
    id: '8',
    name: 'Oscar Holm',
    party: 'MP',
    role: 'Environment Spokesperson',
    constituency: 'Umeå',
    imageUrl: '',
    achievements: [
      'Carbon neutrality roadmap',
      'Biodiversity protection act',
      'Public transport expansion'
    ],
    yearsInParliament: 9,
    committees: ['Environment Committee', 'Transport Committee']
  },
];

export const getPartyById = (id: string): Party | undefined => {
  return parties.find(p => p.id === id);
};

export const getPoliticianById = (id: string): Politician | undefined => {
  return politicians.find(p => p.id === id);
};
