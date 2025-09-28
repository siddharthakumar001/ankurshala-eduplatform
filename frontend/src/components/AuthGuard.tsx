'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { authManager } from '@/utils/auth'

interface AuthGuardProps {
  children: React.ReactNode
  requiredRoles?: string[]
  fallback?: React.ReactNode
}

export default function AuthGuard({ 
  children, 
  requiredRoles = [], 
  fallback = null 
}: AuthGuardProps) {
  const [isLoading, setIsLoading] = useState(true)
  const [isAuthorized, setIsAuthorized] = useState(false)
  const router = useRouter()

  useEffect(() => {
    const checkAuth = async () => {
      try {
        // Check if user is authenticated
        if (!authManager.isAuthenticated()) {
          console.log('User not authenticated, redirecting to login')
          router.push('/login')
          return
        }

        // Check if user has required roles
        if (requiredRoles.length > 0) {
          const user = authManager.getUser()
          if (!user || !user.roles) {
            console.log('User roles not found')
            router.push('/unauthorized')
            return
          }

          const hasRequiredRole = requiredRoles.some(role => 
            user.roles.includes(role)
          )

          if (!hasRequiredRole) {
            console.log('User does not have required role')
            router.push('/unauthorized')
            return
          }
        }

        setIsAuthorized(true)
      } catch (error) {
        console.error('Auth check error:', error)
        router.push('/login')
      } finally {
        setIsLoading(false)
      }
    }

    checkAuth()
  }, [requiredRoles, router])

  // Show loading state
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600 dark:text-gray-400">Verifying authentication...</p>
        </div>
      </div>
    )
  }

  // Show unauthorized state
  if (!isAuthorized) {
    return fallback || (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">🔒</div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Access Denied
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            You don't have permission to access this page.
          </p>
          <button
            onClick={() => router.push('/login')}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Go to Login
          </button>
        </div>
      </div>
    )
  }

  // Render protected content
  return <>{children}</>
}

// Higher-order component for protecting pages
export function withAuth<P extends object>(
  Component: React.ComponentType<P>,
  requiredRoles: string[] = []
) {
  return function AuthenticatedComponent(props: P) {
    return (
      <AuthGuard requiredRoles={requiredRoles}>
        <Component {...props} />
      </AuthGuard>
    )
  }
}

// Hook for checking authentication status
export function useAuth() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState<any>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const checkAuth = () => {
      setIsAuthenticated(authManager.isAuthenticated())
      setUser(authManager.getUser())
      setIsLoading(false)
    }

    checkAuth()

    // Listen for auth state changes
    const interval = setInterval(checkAuth, 1000)

    return () => clearInterval(interval)
  }, [])

  return {
    isAuthenticated,
    user,
    isLoading,
    logout: () => authManager.logout(),
    getToken: () => authManager.getToken()
  }
}
