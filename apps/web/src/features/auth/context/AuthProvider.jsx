import { useCallback, useEffect, useMemo, useState } from 'react'
import { onUnauthorized } from '../../../lib/http.js'
import { clearSession, readSession, saveSession } from '../../../lib/storage.js'
import * as authApi from '../api/authApi.js'
import { isTokenExpired } from '../utils/token.js'
import { AuthContext } from './AuthContext.js'

function loadInitialSession() {
  const session = readSession()
  if (!session?.token || isTokenExpired(session.token)) {
    clearSession()
    return null
  }
  return session
}

export default function AuthProvider({ children }) {
  const [session, setSession] = useState(loadInitialSession)

  const login = useCallback(async (credentials) => {
    const { token, user } = await authApi.login(credentials)
    const next = { token, user }
    saveSession(next)
    setSession(next)
  }, [])

  const logout = useCallback(() => {
    clearSession()
    setSession(null)
  }, [])

  useEffect(() => onUnauthorized(logout), [logout])

  const value = useMemo(
    () => ({ user: session?.user ?? null, isAuthenticated: Boolean(session), login, logout }),
    [session, login, logout],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
