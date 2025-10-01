'use client'

import { useRouter } from 'next/navigation'
import { authManager } from '@/utils/auth'
import { Button } from '@/components/ui/button'
import { AlertTriangle, Home, LogIn, ArrowLeft } from 'lucide-react'

export default function ForbiddenPage() {
  const router = useRouter()

  const handleGoBack = () => {
    router.back()
  }

  const handleGoHome = () => {
    router.push('/')
  }

  const handleLogin = () => {
    router.push('/login')
  }

  const handleLogout = () => {
    authManager.logout()
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-red-50 via-white to-orange-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800">
      <div className="max-w-md w-full text-center px-4">
        <div className="mb-8">
          <div className="mx-auto w-24 h-24 bg-red-100 dark:bg-red-900/20 rounded-full flex items-center justify-center mb-6">
            <AlertTriangle className="h-12 w-12 text-red-600 dark:text-red-400" />
          </div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
            Access Forbidden
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            You don't have permission to access this page. Please login with appropriate credentials to continue.
          </p>
          <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4">
            <p className="text-sm text-yellow-800 dark:text-yellow-200">
              <strong>Need access?</strong> Contact your administrator or login with the correct role.
            </p>
          </div>
        </div>

        <div className="space-y-3">
          <Button
            onClick={handleLogin}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white"
          >
            <LogIn className="h-4 w-4 mr-2" />
            Login
          </Button>
          
          <Button
            onClick={handleGoBack}
            variant="outline"
            className="w-full"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Go Back
          </Button>
          
          <Button
            onClick={handleGoHome}
            variant="outline"
            className="w-full"
          >
            <Home className="h-4 w-4 mr-2" />
            Go Home
          </Button>
          
          {authManager.isAuthenticated() && (
            <Button
              onClick={handleLogout}
              variant="destructive"
              className="w-full"
            >
              Logout
            </Button>
          )}
        </div>

        <div className="mt-8 text-xs text-gray-500 dark:text-gray-400">
          <p>If you believe this is an error, please contact support.</p>
          <p className="mt-1">Error Code: 403 - Forbidden</p>
        </div>
      </div>
    </div>
  )
}