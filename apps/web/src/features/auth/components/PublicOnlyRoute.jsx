import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../hooks/useAuth.js'

export default function PublicOnlyRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (isAuthenticated) return <Navigate to={location.state?.from ?? '/'} replace />

  return <Outlet />
}
