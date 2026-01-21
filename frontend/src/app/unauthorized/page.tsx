'use client'

import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { ArrowLeft, Home, LogOut, ShieldX } from 'lucide-react'
import { authManager } from '@/utils/auth'

export default function UnauthorizedPage() {
  const router = useRouter()

  const handleGoBack = () => {
    router.back()
  }

  const handleGoHome = () => {
    router.push('/')
  }

  const handleLogout = () => {
    authManager.logout()
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-transparent px-6 py-12">
      <div className="max-w-md w-full text-center glass-panel rounded-3xl border border-white/40 p-8">
        <div className="mb-6">
          <div className="mx-auto w-16 h-16 rounded-2xl bg-ankur-primary/10 text-ankur-primary flex items-center justify-center">
            <ShieldX className="h-8 w-8" />
          </div>
          <h1 className="mt-6 text-3xl font-display font-semibold text-ankur-secondary dark:text-white">
            Access denied
          </h1>
          <p className="mt-2 text-gray-600 dark:text-gray-300">
            You do not have permission to access this resource.
          </p>
        </div>

        <div className="grid gap-3">
          <Button onClick={handleGoHome} className="btn-primary w-full">
            <Home className="h-4 w-4 mr-2" />
            Go home
          </Button>
          <Button onClick={handleGoBack} variant="outline" className="w-full">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Go back
          </Button>
          <Button onClick={handleLogout} variant="destructive" className="w-full">
            <LogOut className="h-4 w-4 mr-2" />
            Logout
          </Button>
        </div>

        <p className="mt-6 text-xs text-gray-500 dark:text-gray-400">
          If you believe this is an error, please contact your administrator.
        </p>
      </div>
    </div>
  )
}
