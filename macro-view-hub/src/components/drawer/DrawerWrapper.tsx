'use client';

import * as React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ChevronRight, Menu } from 'lucide-react';
import MenuList from '@/components/drawer/MenuList';

const queryClient = new QueryClient();

const drawerWidth = 240;

const DrawerWrapper = ({ children }: { children: React.ReactNode }) => {
  const [open, setOpen] = React.useState(false);

  const handleDrawerOpen = () => setOpen(true);
  const handleDrawerClose = () => setOpen(false);

  return (
    <div className='flex'>
      {/* Top App Bar */}
      <div className={`fixed top-0 left-0 right-0 z-40 bg-white border-b`}>
        <div className='flex items-center h-14 px-4'>
          <h1 className='text-base font-semibold text-black flex-1'>Macro Hub</h1>
          {!open && (
            <button
              aria-label='open drawer'
              onClick={handleDrawerOpen}
              className='inline-flex items-center justify-center rounded-md p-2 text-black hover:bg-gray-100'
            >
              <Menu className='h-5 w-5' />
            </button>
          )}
        </div>
      </div>

      {/* Main content */}
      <main
        className={`relative flex-1 pt-14 p-4 transition-[margin] duration-300 ease-out ${
          open ? 'mr-60' : 'mr-0'
        }`}
      >
        <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
      </main>

      {/* Right persistent drawer */}
      <aside
        className={`fixed top-0 right-0 h-full bg-white border-l shadow-sm transform transition-transform duration-300 ease-out w-[${drawerWidth}px] z-50 ${
          open ? 'translate-x-0' : 'translate-x-full'
        }`}
        aria-hidden={!open}
      >
        <div className='flex items-center h-14 px-2 border-b'>
          <button
            onClick={handleDrawerClose}
            className='inline-flex items-center justify-center rounded-md p-2 hover:bg-gray-100'
            aria-label='close drawer'
          >
            {/* Using LTR by default */}
            <ChevronRight className='h-5 w-5' />
          </button>
        </div>
        <div className='h-[calc(100%-3.5rem)] overflow-auto'>
          <MenuList />
        </div>
      </aside>
    </div>
  );
};

export default DrawerWrapper;
