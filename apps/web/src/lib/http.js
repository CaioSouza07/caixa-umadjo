import { API_URL } from '../config/env.js'
import { readSession } from './storage.js'

export class ApiError extends Error {
  constructor(message, { status = 0, details = [] } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

let unauthorizedHandler = null

export function onUnauthorized(handler) {
  unauthorizedHandler = handler
  return () => {
    if (unauthorizedHandler === handler) unauthorizedHandler = null
  }
}

export async function http(path, { method = 'GET', body, auth = true } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  const session = auth ? readSession() : null
  if (session?.token) headers.Authorization = `Bearer ${session.token}`

  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor')
  }

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    if (response.status === 401 && session) unauthorizedHandler?.()
    throw new ApiError(data?.message || 'Erro inesperado. Tente novamente.', {
      status: response.status,
      details: data?.details ?? [],
    })
  }

  return data
}
