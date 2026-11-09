import { ReactNode } from 'react';
import { UserRound } from 'lucide-react';
import Footer from '@/components/Footer';
import Header from '@/components/Header';

type Props = {
  title: string;
  children: ReactNode;
};

const AccountPage = ({ title, children }: Props) => (
  <div className='min-h-screen flex flex-col bg-background'>
    <Header />
    <main className='flex-1 container mx-auto px-4 pt-24 pb-12'>
      <div className='max-w-md mx-auto'>
        <div className='flex items-center gap-3 mb-8'>
          <div className='flex h-12 w-12 items-center justify-center rounded-lg bg-gradient-primary'>
            <UserRound className='h-6 w-6 text-primary-foreground' />
          </div>
          <h1 className='text-3xl font-bold text-foreground'>{title}</h1>
        </div>
        <div className='p-6 rounded-lg border border-border bg-card space-y-4'>{children}</div>
      </div>
    </main>
    <Footer />
  </div>
);

export default AccountPage;
