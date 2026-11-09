import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/auth/authContext';
import { useAuthForm } from '@/auth/useAuthForm';
import AccountPage from '@/pages/account/AccountPage';
import { Field, FormError } from '@/pages/account/Field';

const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const notice = (useLocation().state as { notice?: string } | null)?.notice;
  const form = useAuthForm(async (values) => {
    await login(String(values.get('email')), String(values.get('password')));
    navigate('/');
  });

  return (
    <AccountPage title='Logga in'>
      {notice && (
        <p role='status' className='text-sm text-foreground'>
          {notice}
        </p>
      )}
      <form className='space-y-4' onSubmit={form.handleSubmit}>
        <Field id='email' label='E-post' type='email' autoComplete='username' aria-invalid={form.isInvalid('email')} />
        <Field
          id='password'
          label='Lösenord'
          type='password'
          autoComplete='current-password'
          aria-invalid={form.errorMessage !== null && !form.isInvalid('email')}
        />
        <FormError message={form.errorMessage} />
        <Button type='submit' className='w-full' disabled={form.submitting}>
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
