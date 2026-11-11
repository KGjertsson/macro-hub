import { Navigate, useLocation } from 'react-router-dom';
import { MapCategory } from './mapCategories';

// The maps used to be pages of their own, and links to them, with their query, should keep working
const RedirectToMap = ({ category }: { category: MapCategory }) => {
  const { search, hash } = useLocation();
  const params = new URLSearchParams(search);
  params.set('k', category);
  return <Navigate to={{ pathname: '/karta', search: params.toString(), hash }} replace />;
};

export default RedirectToMap;
