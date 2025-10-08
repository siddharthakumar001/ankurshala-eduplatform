'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/hooks/useAuth'
import { api } from '@/utils/api'

interface AuthGuardProps {
  children: React.ReactNode
  requiredRoles?: string[]
  fallback?: React.ReactNode
}

const USER_DASHBOARD_ROUTES = {
  ADMIN: '/admin/dashboard',
  TEACHER: '/teacher/profile',
  STUDENT: '/student/profile'
} as const

export default function AuthGuard({ 
  children, 
  requiredRoles = [], 
  fallback = null 
}: AuthGuardProps) {
  const { user, isAuthenticated, isLoading: authLoading } = useAuth()
  const [isLoading, setIsLoading] = useState(true)
  const [isAuthorized, setIsAuthorized] = useState(false)
  const [authError, setAuthError] = useState<string | null>(null)
  const router = useRouter()

  useEffect(() => {
    const checkAuth = async () => {
      try {
        // Always wait for initial auth resolution to avoid flicker
        if (authLoading) {
          setIsLoading(true)
          return
        }

        // Server-validate session via /user/me (with small retry/backoff to avoid race right after login)
        let serverUser: any | null = null
        const maxAttempts = 3
        for (let attempt = 1; attempt <= maxAttempts; attempt++) {
          try {
            const meResponse = await api.get('/user/me', { requireAuth: true })
            serverUser = meResponse.data
            break
          } catch (e) {
            const delayMs = 250 * attempt
            await new Promise(res => setTimeout(res, delayMs))
          }
        }
        if (!serverUser) {
          // If server validation keeps failing, show unauthorized state without redirect bounce
          setIsAuthorized(false)
          setIsLoading(false)
          return
        }

        // Prefer server role if available; fallback to client user
        const effectiveUser = serverUser || user
        const userRole = (effectiveUser as any)?.role

        // Role check (if required)
        if (requiredRoles.length > 0) {
          if (!userRole) {
            router.replace('/unauthorized')
            return
          }
          const hasRequiredRole = requiredRoles.includes(userRole)
          if (!hasRequiredRole) {
            router.replace('/unauthorized')
            return
          }
        }

        setIsAuthorized(true)
        setAuthError(null)
      } catch (error) {
        console.error('Auth check error:', error)
        setAuthError('Authentication failed')
        router.replace('/login')
      } finally {
        setIsLoading(false)
      }
    }

    checkAuth()
  }, [isAuthenticated, user, authLoading, router, requiredRoles])

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

  // Show error state
  if (authError) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="text-center">
          <div className="text-red-500 text-6xl mb-4">⚠️</div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Authentication Error
          </h1>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            {authError}
          </p>
          <button
            onClick={() => router.replace('/login')}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Go to Login
          </button>
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
            onClick={() => router.replace('/login')}
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

// Note: useAuth hook is now imported from @/hooks/useAuth
