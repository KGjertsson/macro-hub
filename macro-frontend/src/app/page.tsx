'use client';
import PersistentDrawerRight from '@/components/DrawerWrapper';

const Home = () => {
  return (
    <main className="flex min-h-screen flex-col items-center justify-between p-24">
      <PersistentDrawerRight />
    </main>
  );
};

export default Home;
