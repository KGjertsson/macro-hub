import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ArrowRight, Calendar, Sparkles, User } from 'lucide-react';
import { blogPosts } from '@/data/blogData';

const ParliamentBlog = () => {
  const featuredPost = blogPosts.find(post => post.featured);
  const regularPosts = blogPosts.filter(post => post.id !== featuredPost?.id).slice(0, 4);

  const getCategoryColor = (category: string) => {
    const colors: Record<string, string> = {
      'Analysis': 'bg-primary/20 text-primary',
      'Profile': 'bg-chart-2/20 text-chart-2',
      'News': 'bg-chart-3/20 text-chart-3',
      'Explainer': 'bg-chart-4/20 text-chart-4',
      'Feature': 'bg-chart-5/20 text-chart-5'
    };
    return colors[category] || 'bg-muted text-muted-foreground';
  };

  return (
    <section className='mt-12'>
      <div className='flex items-center gap-3 mb-6'>
        <div className='p-2 rounded-lg bg-secondary/50'>
          <Sparkles className='w-5 h-5 text-primary' />
        </div>
        <div>
          <h2 className='text-2xl font-display font-semibold'>Parliament Insights</h2>
          <p className='text-sm text-muted-foreground'>Stories and analysis from the Riksdag</p>
        </div>
      </div>

      <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
        {/* Featured Post */}
        {featuredPost && (
          <Card
            className='lg:col-span-2 glass-card overflow-hidden group hover:border-primary/50 transition-all duration-300'>
            <div className='h-2 bg-gradient-to-r from-primary to-primary/50' />
            <CardContent className='p-6 md:p-8'>
              <div className='flex items-center gap-2 mb-4'>
                <Badge className={getCategoryColor(featuredPost.category)}>
                  {featuredPost.category}
                </Badge>
                <Badge variant='outline' className='border-primary/50 text-primary'>
                  Featured
                </Badge>
              </div>

              <h3
                className='text-2xl md:text-3xl font-display font-bold mb-4 group-hover:text-primary transition-colors'>
                {featuredPost.title}
              </h3>

              <p className='text-muted-foreground mb-6 text-lg leading-relaxed'>
                {featuredPost.excerpt}
              </p>

              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-4 text-sm text-muted-foreground'>
                  <span className='flex items-center gap-1'>
                    <User className='w-4 h-4' />
                    {featuredPost.author}
                  </span>
                  <span className='flex items-center gap-1'>
                    <Calendar className='w-4 h-4' />
                    {new Date(featuredPost.date).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                      year: 'numeric'
                    })}
                  </span>
                </div>

                <span
                  className='flex items-center gap-1 text-primary text-sm font-medium group-hover:gap-2 transition-all'>
                  Read more
                  <ArrowRight className='w-4 h-4' />
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
              className='glass-card hover:border-primary/50 transition-all duration-300 group cursor-pointer animate-fade-in'
              style={{ animationDelay: `${index * 0.1}s` }}
            >
              <CardContent className='p-5'>
                <Badge className={`${getCategoryColor(post.category)} mb-3`}>
                  {post.category}
                </Badge>

                <h4 className='font-semibold mb-2 group-hover:text-primary transition-colors line-clamp-2'>
                  {post.title}
                </h4>

                <p className='text-sm text-muted-foreground line-clamp-2 mb-3'>
                  {post.excerpt}
                </p>

                <div className='flex items-center gap-2 text-xs text-muted-foreground'>
                  <Calendar className='w-3 h-3' />
                  {new Date(post.date).toLocaleDateString('en-US', {
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
            className='glass-card hover:border-primary/50 transition-all duration-300 group cursor-pointer animate-fade-in'
            style={{ animationDelay: `${(index + 2) * 0.1}s` }}
          >
            <CardContent className='p-5 flex gap-4'>
              <div className='flex-1'>
                <Badge className={`${getCategoryColor(post.category)} mb-2`}>
                  {post.category}
                </Badge>

                <h4 className='font-semibold mb-2 group-hover:text-primary transition-colors'>
                  {post.title}
                </h4>

                <p className='text-sm text-muted-foreground line-clamp-2'>
                  {post.excerpt}
                </p>
              </div>

              <div className='flex flex-col justify-between items-end text-xs text-muted-foreground'>
                <span>{new Date(post.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</span>
                <ArrowRight className='w-4 h-4 text-primary opacity-0 group-hover:opacity-100 transition-opacity' />
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </section>
  );
};

export default ParliamentBlog;
