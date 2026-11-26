import { Eye, EyeOff } from 'lucide-react'
import { useState } from 'react'
import TextField from './TextField.jsx'

export default function PasswordField(props) {
  const [visible, setVisible] = useState(false)
  const Icon = visible ? EyeOff : Eye

  return (
    <TextField
      {...props}
      type={visible ? 'text' : 'password'}
      endAdornment={
        <button
          type="button"
          onClick={() => setVisible((current) => !current)}
          aria-pressed={visible}
          aria-label={visible ? 'Ocultar senha' : 'Mostrar senha'}
          title={visible ? 'Ocultar senha' : 'Mostrar senha'}
          className="flex size-10 items-center justify-center rounded-full text-ink-muted transition-colors hover:bg-chip hover:text-ink"
        >
          <Icon size={20} aria-hidden="true" />
        </button>
      }
    />
  )
}
