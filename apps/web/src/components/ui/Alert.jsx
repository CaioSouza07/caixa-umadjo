import { CircleAlert } from 'lucide-react'

export default function Alert({ children, className = '' }) {
  if (!children) return null

  return (
    <div
      role="alert"
      className={`flex items-start gap-2 rounded-field border border-danger bg-danger-soft px-3.5 py-3 text-[13px] font-semibold text-danger ${className}`}
    >
      <CircleAlert size={18} className="mt-px shrink-0" aria-hidden="true" />
      <span>{children}</span>
    </div>
  )
}
