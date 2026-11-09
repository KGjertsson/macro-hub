import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { completePasswordReset, requestPasswordReset } from '@/api/auth';
import { Button } from '@/components/ui/button';
import { useAuthForm } from '@/auth/useAuthForm';
import { useFragmentToken } from '@/auth/useFragmentToken';
import AccountPage from '@/pages/account/AccountPage';
import { Field, FormError, MAX_PASSWORD_LENGTH, MIN_PASSWORD_LENGTH, NEW_PASSWORD_HINT } from '@/pages/account/Field';

const RequestLink = () => {
  const [sentTo, setSentTo] = useState<string | null>(null);
  const form = useAuthForm(async (values) => {
    const email = String(values.get('email'));
    await requestPasswordReset(email);
    setSentTo(email);
  });

  if (sentTo) {
    return (
      <p role='status' className='text-sm text-foreground'>
        Om {sentTo} har ett konto har vi skickat en länk dit.
      </p>
    );
  }

  return (
    <form className='space-y-4' onSubmit={form.handleSubmit}>
      <Field id='email' label='E-post' type='email' autoComplete='email' aria-invalid={form.isInvalid('email')} />
      <FormError message={form.errorMessage} />
      <Button type='submit' className='w-full' disabled={form.submitting}>
        Skicka länk
      </Button>
    </form>
  );
};

const ChooseNewPassword = ({ token }: { token: string }) => {
  const navigate = useNavigate();
  const form = useAuthForm(async (values) => {
    await completePasswordReset(token, String(values.get('password')));
    navigate('/konto/logga-in', { state: { notice: 'Lösenordet är ändrat. Logga in.' } });
  });

  return (
    <form className='space-y-4' onSubmit={form.handleSubmit}>
      <Field
        id='password'
        label='Nytt lösenord'
        type='password'
        autoComplete='new-password'
        minLength={MIN_PASSWORD_LENGTH}
        maxLength={MAX_PASSWORD_LENGTH}
        hint={NEW_PASSWORD_HINT}
        aria-invalid={form.isInvalid('password')}
      />
      <FormError message={form.errorMessage} />
      <Button type='submit' className='w-full' disabled={form.submitting}>
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
