import Alert from '../../../components/ui/Alert.jsx'
import Button from '../../../components/ui/Button.jsx'
import PasswordField from '../../../components/ui/PasswordField.jsx'
import TextField from '../../../components/ui/TextField.jsx'
import { useLoginForm } from '../hooks/useLoginForm.js'

export default function LoginForm() {
  const { field, errors, formError, submitting, onSubmit } = useLoginForm()

  return (
    <form onSubmit={onSubmit} noValidate className="flex flex-col gap-4">
      <Alert>{formError}</Alert>
      <TextField
        label="E-mail"
        type="email"
        inputMode="email"
        autoComplete="email"
        autoFocus
        error={errors.email?.message}
        {...field('email')}
      />
      <PasswordField
        label="Senha"
        autoComplete="current-password"
        error={errors.password?.message}
        {...field('password')}
      />
      <Button type="submit" fullWidth loading={submitting} className="mt-2">
        {submitting ? 'Entrando...' : 'Entrar'}
      </Button>
    </form>
  )
}
