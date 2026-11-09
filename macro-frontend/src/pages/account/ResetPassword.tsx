import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { completePasswordReset, requestPasswordReset } from '@/api/auth';
import { Button } from '@/components/ui/button';
import { authErrorMessage } from '@/auth/errorMessage';
import { useFragmentToken } from '@/auth/useFragmentToken';
import AccountPage from '@/pages/account/AccountPage';
import { Field, FormError, MAX_PASSWORD_LENGTH, MIN_PASSWORD_LENGTH, NEW_PASSWORD_HINT } from '@/pages/account/Field';

const RequestLink = () => {
  const [sentTo, setSentTo] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const email = String(new FormData(event.currentTarget).get('email'));
    setError(null);
    try {
      await requestPasswordReset(email);
      setSentTo(email);
    } catch (failure) {
      setError(authErrorMessage(failure));
    }
  };

  if (sentTo) {
    return <p className='text-sm text-foreground'>Om {sentTo} har ett konto har vi skickat en länk dit.</p>;
  }

  return (
    <form className='space-y-4' onSubmit={handleSubmit}>
      <Field id='email' label='E-post' type='email' autoComplete='email' />
      <FormError message={error} />
      <Button type='submit' className='w-full'>
        Skicka länk
      </Button>
    </form>
  );
};

const ChooseNewPassword = ({ token }: { token: string }) => {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    try {
      await completePasswordReset(token, String(new FormData(event.currentTarget).get('password')));
      navigate('/konto/logga-in', { state: { notice: 'Lösenordet är ändrat. Logga in.' } });
    } catch (failure) {
      setError(authErrorMessage(failure));
    }
  };

  return (
    <form className='space-y-4' onSubmit={handleSubmit}>
      <Field
        id='password'
        label='Nytt lösenord'
        type='password'
        autoComplete='new-password'
        minLength={MIN_PASSWORD_LENGTH}
        maxLength={MAX_PASSWORD_LENGTH}
        hint={NEW_PASSWORD_HINT}
      />
      <FormError message={error} />
      <Button type='submit' className='w-full'>
        Spara lösenord
      </Button>
    </form>
  );
};

const ResetPassword = () => {
  const token = useFragmentToken();

  return (
    <AccountPage title='Återställ lösenord'>{token ? <ChooseNewPassword token={token} /> : <RequestLink />}</AccountPage>
  );
};

export default ResetPassword;
