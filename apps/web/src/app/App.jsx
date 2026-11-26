import { RouterProvider } from 'react-router/dom'
import AuthProvider from '../features/auth/context/AuthProvider.jsx'
import { router } from './router.jsx'

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
    </AuthProvider>
  )
}
