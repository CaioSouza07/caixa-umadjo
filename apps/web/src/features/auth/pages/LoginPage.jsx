import Logo from '../../../components/brand/Logo.jsx'
import LoginForm from '../components/LoginForm.jsx'

export default function LoginPage() {
  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center gap-2.5">
        <Logo />
        <span className="text-lg font-bold tracking-tight">Caixa UMADJO</span>
      </div>
      <div className="flex flex-col gap-1">
        <h1 className="m-0 text-2xl font-bold leading-[30px]">Entrar</h1>
        <p className="m-0 text-sm text-ink-muted">Acesse com seu e-mail e senha.</p>
      </div>
      <LoginForm />
      <p className="m-0 text-center text-xs text-ink-muted">
        Acesso restrito. Peça sua conta ao administrador.
      </p>
    </div>
  )
}
