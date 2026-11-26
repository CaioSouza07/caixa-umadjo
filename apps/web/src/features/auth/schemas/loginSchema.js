import { z } from 'zod'

export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'E-mail é obrigatório')
    .pipe(z.email('Formato de e-mail inválido')),
  password: z.string().min(1, 'Senha é obrigatória'),
})
