import {Card, CardContent} from '@/components/ui/card';
import {Badge} from '@/components/ui/badge';
import {Calendar, Sparkles, User} from 'lucide-react';
import {blogPosts} from '@/data/blogData';
import {ExampleBadge, ExampleNote} from '@/components/ExampleContent';

const ParliamentBlog = () => {
    const featuredPost = blogPosts.find(post => post.featured);
    const regularPosts = blogPosts.filter(post => post.id !== featuredPost?.id).slice(0, 4);

    const getCategoryColor = (category: string) => {
        const colors: Record<string, string> = {
            'Analys': 'bg-primary/20 text-primary',
            'Profil': 'bg-chart-2/20 text-chart-2',
            'Nyheter': 'bg-chart-3/20 text-chart-3',
            'Förklaring': 'bg-chart-4/20 text-chart-4',
            'Reportage': 'bg-chart-5/20 text-chart-5'
        };
        return `border-transparent ${colors[category] || 'bg-muted text-muted-foreground'}`;
    };

    return (
        <section
            className='mt-12'
            aria-labelledby='parliament-blog-heading'
            aria-describedby='parliament-blog-example-note'
        >
            <div className='flex items-center gap-3 mb-6'>
                <div className='p-2 rounded-lg bg-secondary/50'>
                    <Sparkles className='w-5 h-5 text-primary'/>
                </div>
                <div>
                    <div className='flex flex-wrap items-center gap-x-3 gap-y-1'>
                        <h2 id='parliament-blog-heading' className='text-2xl font-display font-semibold'>
                            Riksdagsinsikter
                        </h2>
                        <ExampleBadge/>
                    </div>
                    <p className='text-sm text-muted-foreground'>Berättelser och analyser från riksdagen</p>
                    <ExampleNote id='parliament-blog-example-note' className='mt-1'/>
                </div>
            </div>

            <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
                {/* Featured Post */}
                {featuredPost && (
                    <Card className='lg:col-span-2 glass-card overflow-hidden'>
                        <div className='h-2 bg-gradient-to-r from-primary to-primary/50'/>
                        <CardContent className='p-6 md:p-8'>
                            <div className='flex items-center gap-2 mb-4'>
                                <Badge variant='outline' className={getCategoryColor(featuredPost.category)}>
                                    {featuredPost.category}
                                </Badge>
                                <Badge variant='outline' className='border-primary/50 text-primary'>
                                    Utvald
                                </Badge>
                                <ExampleBadge/>
                            </div>

                            <h3
                                className='text-2xl md:text-3xl font-display font-bold mb-4'>
                                {featuredPost.title}
                            </h3>

                            <p className='text-muted-foreground mb-6 text-lg leading-relaxed'>
                                {featuredPost.excerpt}
                            </p>

                            <div className='flex items-center gap-4 text-sm text-muted-foreground'>
                  <span className='flex items-center gap-1'>
                    <User className='w-4 h-4'/>
                      {featuredPost.author}
                  </span>
                                    <span className='flex items-center gap-1'>
                    <Calendar className='w-4 h-4'/>
                                        {new Date(featuredPost.date).toLocaleDateString('sv-SE', {
                                            month: 'short',
                                            day: 'numeric',
                                            year: 'numeric'
                                        })}
                  </span>
                            </div>
                        </CardContent>
                    </Card>
                )}

                {/* Side Posts */}
                <div className='flex flex-col gap-4'>
                    {regularPosts.slice(0, 2).map((post, index) => (
                        <Card
                            key={post.id}
                            className='glass-card animate-fade-in'
                            style={{animationDelay: `${index * 0.1}s`}}
                        >
                            <CardContent className='p-5'>
                                <div className='flex items-center gap-2 mb-3'>
                                    <Badge variant='outline' className={getCategoryColor(post.category)}>
                                        {post.category}
                                    </Badge>
                                    <ExampleBadge/>
                                </div>

                                <h4 className='font-semibold mb-2 line-clamp-2'>
                                    {post.title}
                                </h4>

                                <p className='text-sm text-muted-foreground line-clamp-2 mb-3'>
                                    {post.excerpt}
                                </p>

                                <div className='flex items-center gap-2 text-xs text-muted-foreground'>
                                    <Calendar className='w-3 h-3'/>
                                    {new Date(post.date).toLocaleDateString('sv-SE', {
                                        month: 'short',
                                        day: 'numeric'
                                    })}
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            </div>

            {/* Bottom Row */}
            <div className='grid grid-cols-1 md:grid-cols-2 gap-4 mt-4'>
                {regularPosts.slice(2, 4).map((post, index) => (
                    <Card
                        key={post.id}
                        className='glass-card animate-fade-in'
                        style={{animationDelay: `${(index + 2) * 0.1}s`}}
                    >
                        <CardContent className='p-5 flex gap-4'>
                            <div className='flex-1'>
                                <div className='flex items-center gap-2 mb-2'>
                                    <Badge variant='outline' className={getCategoryColor(post.category)}>
                                        {post.category}
                                    </Badge>
                                    <ExampleBadge/>
                                </div>

                                <h4 className='font-semibold mb-2'>
                                    {post.title}
                                </h4>

                                <p className='text-sm text-muted-foreground line-clamp-2'>
                                    {post.excerpt}
                                </p>
                            </div>

                            <div className='text-xs text-muted-foreground'>
                                {new Date(post.date).toLocaleDateString('sv-SE', {
                                    month: 'short',
                                    day: 'numeric'
                                })}
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>
        </section>
    );
};

export default ParliamentBlog;
