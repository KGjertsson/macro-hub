export interface BlogPost {
  id: string;
  title: string;
  excerpt: string;
  content: string;
  author: string;
  date: string;
  category: string;
  imageUrl?: string;
  featured?: boolean;
}

export const blogPosts: BlogPost[] = [
  {
    id: '1',
    title: 'Blocköverskridande samarbete i klimatpolitiken ökar',
    excerpt: 'Hur oväntade allianser mellan Miljöpartiet och Centerpartiet omformar miljölagstiftningen i Sverige.',
    content: 'Under de senaste månaderna har vi sett ett aldrig tidigare skådat samarbete mellan partier som traditionellt stått på olika sidor i många frågor...',
    author: 'Redaktionen',
    date: '2024-12-18',
    category: 'Analys',
    featured: true,
  },
  {
    id: '2',
    title: 'Maria Svensson: 15 år som landsbygdens röst',
    excerpt: 'En fördjupning i karriären hos en av riksdagens mest engagerade företrädare för landsbygden.',
    content: 'Sedan hon kom in i riksdagen 2009 har Maria Svensson konsekvent drivit landsbygdens intressen...',
    author: 'Politikredaktionen',
    date: '2024-12-15',
    category: 'Profil',
  },
  {
    id: '3',
    title: 'Rekordsession: 47 propositioner antagna under hösten',
    excerpt: 'Höstsessionen 2024 har varit en av de mest produktiva i modern riksdagshistoria.',
    content: 'Riksdagens effektivitet nådde nya höjder under perioden, där blocköverskridande överenskommelser drev på en omfattande lagstiftningsagenda...',
    author: 'Redaktionen',
    date: '2024-12-10',
    category: 'Nyheter',
  },
  {
    id: '4',
    title: 'Utskottsarbetet: bakom stängda dörrar',
    excerpt: 'Vad som egentligen händer i riksdagens utskott och varför de betyder mer än debatterna i kammaren.',
    content: 'Medan medierna fokuserar på dramatiska debatter i kammaren sker det verkliga lagstiftningsarbetet i utskotten...',
    author: 'Seniorkorrespondent',
    date: '2024-12-05',
    category: 'Förklaring',
  },
  {
    id: '5',
    title: 'Unga röster: den nya generationen ledamöter under 35',
    excerpt: 'Möt riksdagens yngsta ledamöter och deras vision för Sveriges framtid.',
    content: 'En ny generation politiker tar med sig nya perspektiv till riksdagen. Vi porträtterar ledamöterna under 35 som gör avtryck...',
    author: 'Redaktör för ungdomspolitik',
    date: '2024-11-28',
    category: 'Reportage',
    featured: true,
  },
  {
    id: '6',
    title: 'Från opposition till regering: så går maktskiftet till',
    excerpt: 'Så fungerar maktskiften i det svenska parlamentariska systemet.',
    content: 'När regeringsmakten byter händer startar ett komplext övergångsmaskineri. Så här fungerar det i praktiken...',
    author: 'Konstitutionsredaktionen',
    date: '2024-11-20',
    category: 'Förklaring',
  },
];
