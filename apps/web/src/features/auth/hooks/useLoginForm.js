import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { ApiError } from '../../../lib/http.js'
import { loginSchema } from '../schemas/loginSchema.js'
import { useAuth } from './useAuth.js'

const FIELDS = ['email', 'password']

export function useLoginForm() {
  const { login } = useAuth()
  const [formError, setFormError] = useState('')

  const {
    register,
    handleSubmit,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  const onSubmit = handleSubmit(async (values) => {
    setFormError('')
    try {
      await login(values)
    } catch (error) {
      const fieldErrors =
        error instanceof ApiError && error.status === 400
          ? error.details.filter(({ field }) => FIELDS.includes(field))
          : []

      if (fieldErrors.length > 0) {
        fieldErrors.forEach(({ field, message }) => setError(field, { message }))
        setFocus(fieldErrors[0].field)
      } else {
        setFormError(error instanceof ApiError ? error.message : 'Erro inesperado. Tente novamente.')
      }
    }
  })

  const field = (name) => register(name, { onChange: () => setFormError('') })

  return { field, errors, formError, submitting: isSubmitting, onSubmit }
}
