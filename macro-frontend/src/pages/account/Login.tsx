import { FormEvent, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/auth/authContext';
import { authErrorMessage } from '@/auth/errorMessage';
import AccountPage from '@/pages/account/AccountPage';
import { Field, FormError } from '@/pages/account/Field';

const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const notice = (useLocation().state as { notice?: string } | null)?.notice;
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setSubmitting(true);
    setError(null);
    try {
      await login(String(form.get('email')), String(form.get('password')));
      navigate('/');
    } catch (failure) {
      setError(authErrorMessage(failure));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AccountPage title='Logga in'>
      {notice && <p className='text-sm text-foreground'>{notice}</p>}
      <form className='space-y-4' onSubmit={handleSubmit}>
        <Field id='email' label='E-post' type='email' autoComplete='username' />
        <Field id='password' label='Lösenord' type='password' autoComplete='current-password' />
        <FormError message={error} />
        <Button type='submit' className='w-full' disabled={submitting}>
          Logga in
        </Button>
      </form>
      <div className='flex justify-between text-sm'>
        <Link to='/konto/registrera' className='text-primary hover:underline'>
          Skapa konto
        </Link>
        <Link to='/konto/aterstall-losenord' className='text-primary hover:underline'>
          Glömt lösenordet?
        </Link>
      </div>
    </AccountPage>
  );
};

export default Login;
