export interface BlogPost {
  id: string;
  title: string;
  excerpt: string;
  content: string;
  author: string;
  date: string;
  category: string;
  politicianId?: string;
  imageUrl?: string;
  featured?: boolean;
}

export const blogPosts: BlogPost[] = [
  {
    id: '1',
    title: 'The Rise of Cross-Party Collaboration in Climate Policy',
    excerpt: 'How unlikely alliances between Miljöpartiet and Centerpartiet are reshaping environmental legislation in Sweden.',
    content: 'In recent months, we have witnessed an unprecedented level of cooperation between parties traditionally on opposite sides of many issues...',
    author: 'Editorial Team',
    date: '2024-12-18',
    category: 'Analysis',
    featured: true,
  },
  {
    id: '2',
    title: 'Maria Svensson: 15 Years of Rural Advocacy',
    excerpt: 'A deep dive into the career of one of Parliament\'s most dedicated voices for countryside communities.',
    content: 'Since entering the Riksdag in 2009, Maria Svensson has consistently championed the interests of rural Sweden...',
    author: 'Political Desk',
    date: '2024-12-15',
    category: 'Profile',
    politicianId: '3',
  },
  {
    id: '3',
    title: 'Record-Breaking Session: 47 Bills Passed in Autumn Term',
    excerpt: 'The autumn 2024 session has been one of the most productive in recent parliamentary history.',
    content: 'Parliamentary efficiency reached new heights this term, with cross-party agreements driving an unprecedented legislative agenda...',
    author: 'Editorial Team',
    date: '2024-12-10',
    category: 'News',
  },
  {
    id: '4',
    title: 'The Art of Committee Work: Behind Closed Doors',
    excerpt: 'What really happens in parliamentary committees and why they matter more than plenary debates.',
    content: 'While media attention focuses on dramatic plenary sessions, the real work of lawmaking happens in committee rooms...',
    author: 'Senior Correspondent',
    date: '2024-12-05',
    category: 'Explainer',
  },
  {
    id: '5',
    title: 'Young Voices: The New Generation of MPs Under 35',
    excerpt: 'Meet the youngest members of Parliament and their vision for Sweden\'s future.',
    content: 'A new generation of politicians is bringing fresh perspectives to the Riksdag. We profile the under-35 MPs making waves...',
    author: 'Youth Politics Editor',
    date: '2024-11-28',
    category: 'Feature',
    featured: true,
  },
  {
    id: '6',
    title: 'From Opposition to Government: The Transition Explained',
    excerpt: 'Understanding how power shifts work in the Swedish parliamentary system.',
    content: 'When government changes hands, a complex machinery of transition begins. Here\'s how it works in practice...',
    author: 'Constitutional Affairs',
    date: '2024-11-20',
    category: 'Explainer',
  },
];
