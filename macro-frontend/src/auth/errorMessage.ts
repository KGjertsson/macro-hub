import { AuthError } from '@/api/auth';

const MESSAGES: Record<string, string> = {
  invalid_credentials: 'Fel e-postadress eller lösenord.',
  invalid_email: 'Ange en giltig e-postadress.',
  invalid_password: 'Lösenordet måste vara mellan 12 och 128 tecken.',
  invalid_displayName: 'Namnet ska vara 3 till 30 tecken: bokstäver, siffror, mellanslag, - och _.',
  display_name_taken: 'Namnet är upptaget. Välj ett annat.',
  invalid_or_expired_token: 'Länken är ogiltig eller för gammal. Begär en ny.',
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
