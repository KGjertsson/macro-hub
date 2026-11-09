import { AuthError } from '@/api/auth';

export type AuthFormField = 'email' | 'password' | 'displayName';

const MESSAGES: Record<string, string> = {
  invalid_credentials: 'Fel e-postadress eller lösenord.',
  invalid_email: 'Ange en giltig e-postadress.',
  invalid_password: 'Lösenordet måste vara mellan 12 och 128 tecken.',
  invalid_displayName: 'Namnet ska vara 3 till 30 tecken: bokstäver, siffror, mellanslag, - och _.',
  display_name_taken: 'Namnet är upptaget. Välj ett annat.',
  invalid_or_expired_token: 'Länken är ogiltig eller för gammal. Begär en ny.',
};

const FIELDS: Record<string, AuthFormField> = {
  invalid_email: 'email',
  invalid_password: 'password',
  invalid_displayName: 'displayName',
  display_name_taken: 'displayName',
};

export const authErrorMessage = (error: unknown): string => {
  if (error instanceof AuthError) {
    if (error.status === 429) {
      return 'För många försök. Vänta en stund och försök igen.';
    }
    return MESSAGES[error.code] ?? 'Något gick fel. Försök igen.';
  }
  return 'Något gick fel. Försök igen.';
};

export const authErrorField = (error: unknown): AuthFormField | null =>
  error instanceof AuthError ? (FIELDS[error.code] ?? null) : null;
