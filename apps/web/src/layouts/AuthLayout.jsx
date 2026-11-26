import { Outlet } from 'react-router'

export default function AuthLayout() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-card sm:bg-page sm:p-4">
      <div className="box-border w-full max-w-100 bg-card p-6 sm:rounded-card sm:p-8 sm:shadow-overlay">
        <Outlet />
      </div>
    </main>
  )
}
