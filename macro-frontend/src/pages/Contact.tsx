import Header from '@/components/Header';
import Footer from '@/components/Footer';
import { Mail, MapPin, Phone } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';

const Contact = () => {
  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Header />
      <main className="flex-1 container mx-auto px-4 pt-24 pb-12">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center gap-3 mb-8">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-primary">
              <Mail className="h-6 w-6 text-primary-foreground" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-foreground">Kontakt</h1>
              <p className="text-muted-foreground">Hör av dig till oss</p>
            </div>
          </div>

          <div className="grid md:grid-cols-2 gap-8">
            <div className="space-y-6">
              <div className="p-6 rounded-lg border border-border bg-card">
                <h2 className="text-lg font-semibold text-foreground mb-4">Skicka ett meddelande</h2>
                <form className="space-y-4">
                  <div>
                    <label htmlFor="name" className="text-sm font-medium text-foreground">Namn</label>
                    <Input id="name" placeholder="Ditt namn" className="mt-1" />
                  </div>
                  <div>
                    <label htmlFor="email" className="text-sm font-medium text-foreground">E-post</label>
                    <Input id="email" type="email" placeholder="din@email.se" className="mt-1" />
                  </div>
                  <div>
                    <label htmlFor="message" className="text-sm font-medium text-foreground">Meddelande</label>
                    <Textarea id="message" placeholder="Ditt meddelande..." className="mt-1" rows={4} />
                  </div>
                  <Button type="submit" className="w-full">Skicka</Button>
                </form>
              </div>
            </div>

            <div className="space-y-4">
              <div className="p-6 rounded-lg border border-border bg-card">
                <div className="flex items-start gap-4">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                    <Mail className="h-5 w-5 text-primary" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-foreground">E-post</h3>
                    <p className="text-muted-foreground">info@macrohub.se</p>
                  </div>
                </div>
              </div>

              <div className="p-6 rounded-lg border border-border bg-card">
                <div className="flex items-start gap-4">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                    <MapPin className="h-5 w-5 text-primary" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-foreground">Adress</h3>
                    <p className="text-muted-foreground">Stockholm, Sverige</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default Contact;
