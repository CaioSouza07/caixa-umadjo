import Spinner from './Spinner.jsx'

const variants = {
  primary:
    'bg-brand text-on-brand hover:opacity-85 disabled:bg-disabled disabled:text-ink-disabled disabled:opacity-100',
  secondary:
    'bg-card text-ink border border-line hover:opacity-85 disabled:text-ink-disabled disabled:opacity-100',
}

export default function Button({
  variant = 'primary',
  loading = false,
  fullWidth = false,
  disabled = false,
  className = '',
  children,
  ...props
}) {
  return (
    <button
      type="button"
      disabled={disabled || loading}
      aria-busy={loading || undefined}
      className={`inline-flex h-12 items-center justify-center gap-2 whitespace-nowrap rounded-full px-5 text-sm font-semibold transition active:scale-[0.98] disabled:cursor-not-allowed ${variants[variant]} ${fullWidth ? 'w-full' : ''} ${className}`}
      {...props}
    >
      {loading && <Spinner />}
      {children}
    </button>
  )
}
