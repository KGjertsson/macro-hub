import { useEffect, useState } from 'react';

const readToken = (): string | null => new URLSearchParams(window.location.hash.slice(1)).get('token');

// The mailed link's token is taken out of the address bar at once, so it isn't left in the history or copied along with
// the URL. A link opened in a tab already on this page only changes the fragment, so nothing reloads and the event is all there is
export const useFragmentToken = (): string | null => {
  const [token, setToken] = useState(readToken);

  useEffect(() => {
    const takeToken = () => {
      const found = readToken();
      if (found) {
        setToken(found);
        window.history.replaceState(window.history.state, '', window.location.pathname);
      }
    };
    takeToken();
    window.addEventListener('hashchange', takeToken);

    return () => window.removeEventListener('hashchange', takeToken);
  }, []);

  return token;
};
