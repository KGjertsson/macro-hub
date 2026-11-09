import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { completeRegistration, requestRegistration } from '@/api/auth';
import { Button } from '@/components/ui/button';
import { useAuthForm } from '@/auth/useAuthForm';
import { useFragmentToken } from '@/auth/useFragmentToken';
import AccountPage from '@/pages/account/AccountPage';
import { Field, FormError, MAX_PASSWORD_LENGTH, MIN_PASSWORD_LENGTH, NEW_PASSWORD_HINT } from '@/pages/account/Field';

const RequestLink = () => {
  const [sentTo, setSentTo] = useState<string | null>(null);
  const form = useAuthForm(async (values) => {
    const email = String(values.get('email'));
    await requestRegistration(email);
    setSentTo(email);
  });

  if (sentTo) {
    return (
      <p role='status' className='text-sm text-foreground'>
        Om {sentTo} kan registreras har vi skickat en länk dit. Öppna den för att välja namn och lösenord.
      </p>
    );
  }

  return (
    <form className='space-y-4' onSubmit={form.handleSubmit}>
      <Field
        id='email'
        label='E-post'
        type='email'
        autoComplete='email'
        hint='Vi skickar en länk för att skapa kontot.'
        aria-invalid={form.isInvalid('email')}
      />
      <FormError message={form.errorMessage} />
      <Button type='submit' className='w-full' disabled={form.submitting}>
        Skicka länk
      </Button>
    </form>
  );
};

const CompleteRegistration = ({ token }: { token: string }) => {
  const navigate = useNavigate();
  const form = useAuthForm(async (values) => {
    await completeRegistration(token, String(values.get('displayName')), String(values.get('password')));
    navigate('/konto/logga-in', { state: { notice: 'Kontot är skapat. Logga in.' } });
  });

  return (
    <form className='space-y-4' onSubmit={form.handleSubmit}>
      <Field
        id='displayName'
        label='Namn'
        autoComplete='nickname'
        minLength={3}
        maxLength={30}
        hint='Visas för andra.'
        aria-invalid={form.isInvalid('displayName')}
      />
      <Field
        id='password'
        label='Lösenord'
        type='password'
        autoComplete='new-password'
        minLength={MIN_PASSWORD_LENGTH}
        maxLength={MAX_PASSWORD_LENGTH}
        hint={NEW_PASSWORD_HINT}
        aria-invalid={form.isInvalid('password')}
      />
      <FormError message={form.errorMessage} />
      <Button type='submit' className='w-full' disabled={form.submitting}>
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
