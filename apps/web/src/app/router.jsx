import { createBrowserRouter, Navigate } from 'react-router'
import AuthLayout from '../layouts/AuthLayout.jsx'
import ProtectedRoute from '../features/auth/components/ProtectedRoute.jsx'
import PublicOnlyRoute from '../features/auth/components/PublicOnlyRoute.jsx'
import LoginPage from '../features/auth/pages/LoginPage.jsx'
import HomePage from '../pages/HomePage.jsx'

export const router = createBrowserRouter([
  {
    element: <PublicOnlyRoute />,
    children: [
      {
        element: <AuthLayout />,
        children: [{ path: '/login', element: <LoginPage /> }],
      },
    ],
  },
  {
    element: <ProtectedRoute />,
    children: [{ path: '/', element: <HomePage /> }],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])
