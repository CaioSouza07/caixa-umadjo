import { useId } from 'react'

export default function TextField({
  label,
  error,
  hint,
  endAdornment,
  ref,
  className = '',
  ...props
}) {
  const id = useId()
  const messageId = `${id}-message`
  const hasMessage = Boolean(error || hint)

  return (
    <div className={`flex flex-col gap-1.5 ${className}`}>
      <label htmlFor={id} className="text-xs font-semibold">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          ref={ref}
          aria-invalid={error ? 'true' : undefined}
          aria-describedby={hasMessage ? messageId : undefined}
          className={`box-border h-12 w-full rounded-field bg-card px-3.5 text-base text-ink ${
            error ? 'border-2 border-danger' : 'border border-field'
          } ${endAdornment ? 'pr-13' : ''}`}
          {...props}
        />
        {endAdornment && (
          <div className="absolute inset-y-0 right-1.5 flex items-center">{endAdornment}</div>
        )}
      </div>
      {hasMessage && (
        <span
          id={messageId}
          role={error ? 'alert' : undefined}
          className={error ? 'text-[13px] font-semibold text-danger' : 'text-xs text-ink-muted'}
        >
          {error || hint}
        </span>
      )}
    </div>
  )
}
