'use client'

import { useEffect, useState } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuthStore } from '@/store/auth'

interface RouteGuardProps {
  children: React.ReactNode
  requireAuth?: boolean
  allowedRoles?: string[]
  redirectTo?: string
}

// Stage-1 FE complete: Route protection with role-based access
export default function RouteGuard({ 
  children, 
  requireAuth = false, 
  allowedRoles = [], 
  redirectTo 
}: RouteGuardProps) {
  const router = useRouter()
  const pathname = usePathname()
  const { user, isAuthenticated } = useAuthStore()
  const [isLoading, setIsLoading] = useState(true)
  const [isAuthorized, setIsAuthorized] = useState(false)
  const [authInitialized, setAuthInitialized] = useState(false)

  useEffect(() => {
    // Wait for auth initialization to complete
    const timer = setTimeout(() => {
      setAuthInitialized(true)
    }, 50) // Reduced delay to allow auth initialization

    return () => clearTimeout(timer)
  }, [])

  useEffect(() => {
    if (!authInitialized) return

    const checkAuth = () => {
      // If authentication is not required, allow access
      if (!requireAuth) {
        setIsAuthorized(true)
        setIsLoading(false)
        return
      }

      // Check if user is authenticated
      if (!isAuthenticated || !user) {
        // Redirect to login with return URL
        const returnUrl = encodeURIComponent(pathname)
        router.push(`/login?redirect=${returnUrl}`)
        setIsLoading(false)
        return
      }

      // Check role-based access
      if (allowedRoles.length > 0 && !allowedRoles.includes(user.role || '')) {
        // Redirect based on user role or to forbidden page
        if (redirectTo) {
          router.push(redirectTo)
        } else {
          // Default role-based redirects
          switch (user.role) {
            case 'STUDENT':
              router.push('/student/profile')
              break
            case 'TEACHER':
              router.push('/teacher/profile')
              break
            case 'ADMIN':
              router.push('/admin')
              break
            default:
              router.push('/forbidden')
          }
        }
        setIsLoading(false)
        return
      }

      setIsAuthorized(true)
      setIsLoading(false)
    }

    checkAuth()
  }, [isAuthenticated, user, pathname, requireAuth, allowedRoles, router, redirectTo, authInitialized])

  // Show loading state while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    )
  }

  // Show content only if authorized
  if (isAuthorized) {
    return <>{children}</>
  }

  // This should not be reached due to redirects above, but just in case
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Access Denied</h1>
        <p className="text-gray-600 mb-8">You don't have permission to access this page.</p>
        <button
          onClick={() => router.push('/')}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          Go Home
        </button>
      </div>
    </div>
  )
}

// Convenience components for specific route protection
export function StudentRoute({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard requireAuth={true} allowedRoles={['STUDENT']}>
      {children}
    </RouteGuard>
  )
}

export function TeacherRoute({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard requireAuth={true} allowedRoles={['TEACHER']}>
      {children}
    </RouteGuard>
  )
}

export function AdminRoute({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard requireAuth={true} allowedRoles={['ADMIN']}>
      {children}
    </RouteGuard>
  )
}

export function AuthRoute({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard requireAuth={true}>
      {children}
    </RouteGuard>
  )
}
