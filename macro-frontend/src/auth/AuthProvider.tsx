import { ReactNode, useCallback, useEffect, useMemo, useRef, useState } from 'react';
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
    refreshing.current ??= authApi
      .refreshAccessToken()
      .then(hold)
      .catch((error: unknown) => {
        // Only a refused session ends the signed-in state: the service may just be restarting
        if (error instanceof authApi.AuthError && error.status === 401) {
          forget();
        }
        return null;
      })
      .finally(() => {
        refreshing.current = null;
      });
    return refreshing.current;
  }, [forget, hold]);

  useEffect(() => {
    if (hasSignedInHint()) {
      void refresh();
    }
  }, [refresh]);

  const value = useMemo(
    () => ({
      user,
      login: async (email: string, password: string) => {
        hold(await authApi.login(email, password));
        setSignedInHint(true);
      },
      logout: async () => {
        try {
          await authApi.logout();
        } finally {
          forget();
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
