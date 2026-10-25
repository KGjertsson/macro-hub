import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail } from 'lucide-react';

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
    name: 'Makroindikatorer',
    route: '/macroindicator',
  },
  {
    name: 'Källor',
    route: '/sources',
  },
];

const MenuList = () => {
  const navigate = useNavigate();

  return (
    <ul className="py-2">
      {menuItems.map((menuItem) => (
        <li key={menuItem.route}>
          <button
            onClick={() => navigate(menuItem.route)}
            className="w-full flex items-center gap-3 px-3 py-2 text-sm hover:bg-gray-50"
          >
            <Mail className="h-4 w-4 text-gray-500" />
            <span className="text-gray-900">{menuItem.name}</span>
          </button>
        </li>
      ))}
    </ul>
  );
};

export default MenuList;
