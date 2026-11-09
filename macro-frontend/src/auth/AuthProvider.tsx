import { ReactNode, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { toast } from 'sonner';
import * as authApi from '@/api/auth';
import { AuthContext } from '@/auth/authContext';

type HeldToken = { value: string; expiresAtMs: number };

const SIGNED_IN_HINT = 'macro-hub-signed-in';
const RENEW_BEFORE_EXPIRY_MS = 30_000;

// The session cookie is HttpOnly, so this flag is how a page knows there may be a session. Without it, every anonymous
// visit would ask for a token and leave a 401 in the browser's console
const hasSignedInHint = (): boolean => {
  try {
    return localStorage.getItem(SIGNED_IN_HINT) !== null;
  } catch {
    return false;
  }
};

const setSignedInHint = (signedIn: boolean): void => {
  try {
    if (signedIn) {
      localStorage.setItem(SIGNED_IN_HINT, '1');
    } else {
      localStorage.removeItem(SIGNED_IN_HINT);
    }
  } catch {
    // Private windows can refuse storage: the session then only lasts until the page is reloaded
  }
};

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<authApi.AuthUser | null>(null);
  const token = useRef<HeldToken | null>(null);
  const refreshing = useRef<Promise<string | null> | null>(null);
  // A login or logout moves this on, so the answer to a refresh started before it can't overwrite what it did
  const generation = useRef(0);

  const hold = useCallback((answer: authApi.AuthenticatedResponse): string => {
    token.current = { value: answer.accessToken, expiresAtMs: Date.now() + answer.expiresInSeconds * 1000 };
    setUser(answer.user);
    return answer.accessToken;
  }, []);

  const forget = useCallback(() => {
    token.current = null;
    setUser(null);
    setSignedInHint(false);
  }, []);

  const refresh = useCallback((): Promise<string | null> => {
    if (refreshing.current) {
      return refreshing.current;
    }
    const startedIn = generation.current;
    const pending: Promise<string | null> = authApi
      .refreshAccessToken()
      .then((answer) => (generation.current === startedIn ? hold(answer) : (token.current?.value ?? null)))
      .catch((error: unknown) => {
        // Only a refused session ends the signed-in state: the service may just be restarting
        if (generation.current === startedIn && error instanceof authApi.AuthError && error.status === 401) {
          forget();
        }
        return null;
      })
      .finally(() => {
        if (refreshing.current === pending) {
          refreshing.current = null;
        }
      });
    refreshing.current = pending;
    return pending;
  }, [forget, hold]);

  useEffect(() => {
    if (hasSignedInHint()) {
      void refresh();
    }
  }, [refresh]);

  // The other tabs' logins and logouts arrive through storage. Returning to a tab whose token has run out is when a session
  // revoked elsewhere shows up
  useEffect(() => {
    const followOtherTab = (event: StorageEvent) => {
      if (event.key !== SIGNED_IN_HINT) {
        return;
      }
      generation.current += 1;
      // A refresh still in flight belongs to the old generation, and its answer will be dropped, so it can't be joined
      refreshing.current = null;
      if (event.newValue === null) {
        token.current = null;
        setUser(null);
      } else {
        void refresh();
      }
    };
    const recheckWhenVisible = () => {
      const expired = !token.current || token.current.expiresAtMs <= Date.now();
      if (document.visibilityState === 'visible' && expired && hasSignedInHint()) {
        void refresh();
      }
    };
    window.addEventListener('storage', followOtherTab);
    document.addEventListener('visibilitychange', recheckWhenVisible);

    return () => {
      window.removeEventListener('storage', followOtherTab);
      document.removeEventListener('visibilitychange', recheckWhenVisible);
    };
  }, [refresh]);

  const value = useMemo(
    () => ({
      user,
      login: async (email: string, password: string) => {
        const answer = await authApi.login(email, password);
        generation.current += 1;
        hold(answer);
        setSignedInHint(true);
      },
      logout: async () => {
        generation.current += 1;
        try {
          await authApi.logout();
          forget();
        } catch {
          // The session still exists, so showing the visitor as logged out would be wrong
          toast.error('Det gick inte att logga ut. Försök igen.');
        }
      },
      getAccessToken: async () => {
        if (token.current && token.current.expiresAtMs - RENEW_BEFORE_EXPIRY_MS > Date.now()) {
          return token.current.value;
        }
        return hasSignedInHint() ? refresh() : null;
      },
    }),
    [forget, hold, refresh, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
