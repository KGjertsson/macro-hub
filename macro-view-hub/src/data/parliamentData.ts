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
