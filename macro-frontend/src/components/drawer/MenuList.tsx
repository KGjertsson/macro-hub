'use client';

import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import MailIcon from '@mui/icons-material/Mail';
import ListItemText from '@mui/material/ListItemText';
import * as React from 'react';
import { useRouter } from 'next/navigation';

type MenuItem = {
  name: string;
  route: string;
};

const menuItems: MenuItem[] = [
  {
    name: 'Hem',
    route: '/',
  },
  {
    name: 'Dynamisk graf',
    route: '/chart',
  },
  {
    name: 'Källor',
    route: '/sources',
  },
];

const MenuList = () => {
  const router = useRouter();

  return (
    <List>
      {menuItems.map((menuItem) => (
        <ListItem key={menuItem.route} disablePadding>
          <ListItemButton onClick={() => router.push(menuItem.route)}>
            <ListItemIcon>
              <MailIcon />
            </ListItemIcon>
            <ListItemText primary={menuItem.name} />
          </ListItemButton>
        </ListItem>
      ))}
    </List>
  );
};

export default MenuList;
