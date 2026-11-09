import { ComponentProps } from 'react';
import { Input } from '@/components/ui/input';

type Props = ComponentProps<typeof Input> & {
  id: string;
  label: string;
  hint?: string;
};

export const Field = ({ id, label, hint, ...input }: Props) => (
  <div>
    <label htmlFor={id} className='text-sm font-medium text-foreground'>
      {label}
    </label>
    <Input id={id} name={id} required className='mt-1' aria-describedby={hint ? `${id}-hint` : undefined} {...input} />
    {hint && (
      <p id={`${id}-hint`} className='mt-1 text-xs text-muted-foreground'>
        {hint}
      </p>
    )}
  </div>
);

export const NEW_PASSWORD_HINT = 'Minst 12 tecken.';
export const MIN_PASSWORD_LENGTH = 12;
export const MAX_PASSWORD_LENGTH = 128;

export const FormError = ({ message }: { message: string | null }) =>
  message ? (
    <p role='alert' className='text-sm text-destructive'>
      {message}
    </p>
  ) : null;
