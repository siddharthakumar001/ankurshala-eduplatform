'use client'

import { useRouter } from 'next/navigation'
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
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
      <div className="max-w-md w-full text-center">
        <div className="mb-8">
          <div className="text-6xl mb-4">🚫</div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
            Access Denied
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            You don't have permission to access this resource.
          </p>
        </div>

        <div className="space-y-4">
          <button
            onClick={handleGoBack}
            className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Go Back
          </button>
          
          <button
            onClick={handleGoHome}
            className="w-full px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500"
          >
            Go Home
          </button>
          
          <button
            onClick={handleLogout}
            className="w-full px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500"
          >
            Logout
          </button>
        </div>

        <div className="mt-8 text-xs text-gray-500 dark:text-gray-400">
          <p>If you believe this is an error, please contact your administrator.</p>
        </div>
      </div>
    </div>
  )
}
