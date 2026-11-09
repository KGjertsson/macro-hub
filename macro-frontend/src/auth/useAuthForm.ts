import { FormEvent, useState } from 'react';
import { authErrorField, authErrorMessage, AuthFormField } from '@/auth/errorMessage';

type Failure = { message: string; field: AuthFormField | null };

export const useAuthForm = (action: (form: FormData) => Promise<void>) => {
  const [failure, setFailure] = useState<Failure | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (submitting) {
      return;
    }
    const form = new FormData(event.currentTarget);
    setSubmitting(true);
    setFailure(null);
    try {
      await action(form);
    } catch (error) {
      setFailure({ message: authErrorMessage(error), field: authErrorField(error) });
    } finally {
      setSubmitting(false);
    }
  };

  return {
    handleSubmit,
    submitting,
    errorMessage: failure?.message ?? null,
    isInvalid: (field: AuthFormField) => failure?.field === field,
  };
};
