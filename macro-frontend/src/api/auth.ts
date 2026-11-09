import { rootUrl } from '@/models/Constants';

const baseUrl = `${rootUrl}/macro-analyzer/auth`;

export type AuthUser = {
  id: string;
  email: string;
  displayName: string;
  role: 'USER' | 'ADMIN';
};

export type AuthenticatedResponse = {
  accessToken: string;
  expiresInSeconds: number;
  user: AuthUser;
};

export class AuthError extends Error {
  constructor(readonly code: string, readonly status: number) {
    super(`Auth request failed: ${code}`);
  }
}

const post = async (path: string, body?: unknown): Promise<Response> => {
  const response = await fetch(baseUrl + path, {
    method: 'POST',
    headers: body === undefined ? undefined : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) {
    const answer = await response.json().catch(() => ({}));
    throw new AuthError(answer.error ?? 'unknown', response.status);
  }
  return response;
};

export const requestRegistration = (email: string) => post('/register', { email });

export const completeRegistration = (token: string, displayName: string, password: string) =>
  post('/register/complete', { token, displayName, password });

export const requestPasswordReset = (email: string) => post('/password-reset', { email });

export const completePasswordReset = (token: string, password: string) =>
  post('/password-reset/complete', { token, password });

export const login = async (email: string, password: string): Promise<AuthenticatedResponse> =>
  (await post('/login', { email, password })).json();

export const refreshAccessToken = async (): Promise<AuthenticatedResponse> => (await post('/token')).json();

export const logout = () => post('/logout');
