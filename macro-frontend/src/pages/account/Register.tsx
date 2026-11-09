import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { completeRegistration, requestRegistration } from '@/api/auth';
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
      await requestRegistration(email);
      setSentTo(email);
    } catch (failure) {
      setError(authErrorMessage(failure));
    }
  };

  if (sentTo) {
    return (
      <p className='text-sm text-foreground'>
        Om {sentTo} kan registreras har vi skickat en länk dit. Öppna den för att välja namn och lösenord.
      </p>
    );
  }

  return (
    <form className='space-y-4' onSubmit={handleSubmit}>
      <Field id='email' label='E-post' type='email' autoComplete='email' hint='Vi skickar en länk för att skapa kontot.' />
      <FormError message={error} />
      <Button type='submit' className='w-full'>
        Skicka länk
      </Button>
    </form>
  );
};

const CompleteRegistration = ({ token }: { token: string }) => {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setError(null);
    try {
      await completeRegistration(token, String(form.get('displayName')), String(form.get('password')));
      navigate('/konto/logga-in', { state: { notice: 'Kontot är skapat. Logga in.' } });
    } catch (failure) {
      setError(authErrorMessage(failure));
    }
  };

  return (
    <form className='space-y-4' onSubmit={handleSubmit}>
      <Field id='displayName' label='Namn' autoComplete='nickname' minLength={3} maxLength={30} hint='Visas för andra.' />
      <Field
        id='password'
        label='Lösenord'
        type='password'
        autoComplete='new-password'
        minLength={MIN_PASSWORD_LENGTH}
        maxLength={MAX_PASSWORD_LENGTH}
        hint={NEW_PASSWORD_HINT}
      />
      <FormError message={error} />
      <Button type='submit' className='w-full'>
        Skapa konto
      </Button>
    </form>
  );
};

const Register = () => {
  const token = useFragmentToken();

  return <AccountPage title='Skapa konto'>{token ? <CompleteRegistration token={token} /> : <RequestLink />}</AccountPage>;
};

export default Register;
