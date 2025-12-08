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
  const { user, isAuthenticated, initializeAuth } = useAuthStore()
  const [isLoading, setIsLoading] = useState(true)
  const [isAuthorized, setIsAuthorized] = useState(false)
  const [authInitialized, setAuthInitialized] = useState(false)

  useEffect(() => {
    // Initialize auth from API on mount if we have cookies
    const init = async () => {
      try {
        console.log('🔍 RouteGuard: Initializing auth from API...')
        await initializeAuth()
        console.log('✅ RouteGuard: Auth initialized from API')
      } catch (error) {
        console.log('⚠️ RouteGuard: Auth initialization failed:', error)
        // Don't clear auth state if we have cookies - might be temporary API issue
        const hasCookies = typeof document !== 'undefined' && 
          (document.cookie.includes('accessToken') || document.cookie.includes('refreshToken'))
        
        if (hasCookies) {
          console.log('✅ RouteGuard: Cookies exist, keeping auth state')
          // Keep the existing auth state if cookies exist
        }
      } finally {
        setAuthInitialized(true)
      }
    }

    // Check if we already have user in store (from login or persisted)
    if (user && isAuthenticated) {
      console.log('✅ RouteGuard: User already in store:', user.email, user.role)
      setAuthInitialized(true)
      // Don't call initializeAuth if user is already authenticated
      return
    }
    
    // Check if we have cookies before trying to initialize
    const hasCookies = typeof document !== 'undefined' && 
      (document.cookie.includes('accessToken') || document.cookie.includes('refreshToken'))
    
    if (hasCookies) {
      console.log('🔄 RouteGuard: Cookies found, fetching user from API...')
      // Try to fetch user from API using cookies
      init()
    } else {
      console.log('⚠️ RouteGuard: No cookies found, user not authenticated')
      setAuthInitialized(true)
    }
  }, [])

  useEffect(() => {
    if (!authInitialized) return

    const checkAuth = () => {
      console.log('🔐 RouteGuard: Checking auth...', {
        pathname,
        requireAuth,
        isAuthenticated,
        user: user?.email,
        role: user?.role,
        allowedRoles
      })

      // If authentication is not required, allow access
      if (!requireAuth) {
        console.log('✅ RouteGuard: No auth required for', pathname)
        setIsAuthorized(true)
        setIsLoading(false)
        return
      }

      // Check if user is authenticated
      if (!isAuthenticated || !user) {
        // Check if we have cookies - if so, wait a bit for auth to initialize
        const hasCookies = typeof document !== 'undefined' && 
          (document.cookie.includes('accessToken') || document.cookie.includes('refreshToken'))
        
        if (hasCookies && !authInitialized) {
          console.log('⏳ RouteGuard: Cookies exist but auth not initialized yet, waiting...')
          // Don't redirect yet if we have cookies - auth might still be initializing
          return
        }
        
        console.log('❌ RouteGuard: User not authenticated, redirecting to login')
        // Redirect to login with return URL
        const returnUrl = encodeURIComponent(pathname)
        router.push(`/login?redirect=${returnUrl}`)
        setIsLoading(false)
        return
      }

      // Check role-based access
      if (allowedRoles.length > 0 && !allowedRoles.includes(user.role || '')) {
        console.log('❌ RouteGuard: User role not allowed', {
          userRole: user.role,
          allowedRoles
        })
        // Redirect based on user role or to forbidden page
        if (redirectTo) {
          router.push(redirectTo)
        } else {
          // Default role-based redirects
          switch (user.role) {
            case 'STUDENT':
              router.push('/student/dashboard')
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

      console.log('✅ RouteGuard: Authorization successful for', pathname)
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
