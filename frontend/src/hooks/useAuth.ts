/**
 * Comprehensive Authentication Hook
 * Provides authentication state management, heartbeat, and session management
 */

import { useEffect, useCallback, useRef } from 'react'
import { useAuthStore } from '@/store/auth'
import { useRouter } from 'next/navigation'

interface UseAuthReturn {
  user: any | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (user: any) => void
  logout: () => Promise<void>
  updateActivity: () => void
}

export function useAuth(): UseAuthReturn {
  const router = useRouter()
  const {
    user,
    isAuthenticated,
    isLoading,
    login,
    logout,
    initializeAuth,
    updateActivity,
    setLoading,
    lastActivity
  } = useAuthStore()

  const heartbeatIntervalRef = useRef<NodeJS.Timeout | null>(null)
  const idleTimeoutRef = useRef<NodeJS.Timeout | null>(null)
  const activityTimeoutRef = useRef<NodeJS.Timeout | null>(null)

  // Constants
  const HEARTBEAT_INTERVAL = 5 * 60 * 1000 // 5 minutes
  const IDLE_TIMEOUT = 45 * 60 * 1000 // 45 minutes
  const ACTIVITY_DEBOUNCE = 1000 // 1 second

  /**
   * Start heartbeat mechanism to keep session alive
   */
  const startHeartbeat = useCallback(() => {
    if (heartbeatIntervalRef.current) {
      clearInterval(heartbeatIntervalRef.current)
    }

    heartbeatIntervalRef.current = setInterval(async () => {
      if (isAuthenticated && user) {
        try {
          // Send heartbeat request to keep session alive
          const response = await fetch('/api/auth/heartbeat', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            credentials: 'include'
          })

          if (response.ok) {
            updateActivity()
            console.log('Heartbeat successful')
          } else {
            console.warn('Heartbeat failed, session may be expired')
            // Don't logout immediately, let the idle timeout handle it
          }
        } catch (error) {
          console.warn('Heartbeat error:', error)
        }
      }
    }, HEARTBEAT_INTERVAL)
  }, [isAuthenticated, user, updateActivity])

  /**
   * Stop heartbeat mechanism
   */
  const stopHeartbeat = useCallback(() => {
    if (heartbeatIntervalRef.current) {
      clearInterval(heartbeatIntervalRef.current)
      heartbeatIntervalRef.current = null
    }
  }, [])

  /**
   * Reset idle timeout
   */
  const resetIdleTimeout = useCallback(() => {
    if (idleTimeoutRef.current) {
      clearTimeout(idleTimeoutRef.current)
    }

    idleTimeoutRef.current = setTimeout(() => {
      console.log('User idle timeout reached, logging out')
      logout().then(() => {
        router.push('/login?message=Session expired due to inactivity')
      })
    }, IDLE_TIMEOUT)
  }, [logout, router])

  /**
   * Clear idle timeout
   */
  const clearIdleTimeout = useCallback(() => {
    if (idleTimeoutRef.current) {
      clearTimeout(idleTimeoutRef.current)
      idleTimeoutRef.current = null
    }
  }, [])

  /**
   * Debounced activity update
   */
  const debouncedUpdateActivity = useCallback(() => {
    if (activityTimeoutRef.current) {
      clearTimeout(activityTimeoutRef.current)
    }

    activityTimeoutRef.current = setTimeout(() => {
      updateActivity()
      if (isAuthenticated) {
        resetIdleTimeout()
      }
    }, ACTIVITY_DEBOUNCE)
  }, [updateActivity, resetIdleTimeout, isAuthenticated])

  /**
   * Setup event listeners for user activity
   */
  const setupActivityListeners = useCallback(() => {
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click']
    
    events.forEach(event => {
      document.addEventListener(event, debouncedUpdateActivity, true)
    })

    // Handle page visibility changes
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible' && isAuthenticated) {
        debouncedUpdateActivity()
      }
    })

    // Handle page unload
    window.addEventListener('beforeunload', () => {
      stopHeartbeat()
      clearIdleTimeout()
    })
  }, [debouncedUpdateActivity, isAuthenticated, stopHeartbeat, clearIdleTimeout])

  /**
   * Cleanup event listeners
   */
  const cleanupActivityListeners = useCallback(() => {
    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click']
    
    events.forEach(event => {
      document.removeEventListener(event, debouncedUpdateActivity, true)
    })

    document.removeEventListener('visibilitychange', debouncedUpdateActivity)
    window.removeEventListener('beforeunload', stopHeartbeat)
  }, [debouncedUpdateActivity, stopHeartbeat])

  /**
   * Initialize authentication on mount
   */
  useEffect(() => {
    initializeAuth()
  }, [initializeAuth])

  /**
   * Setup/cleanup heartbeat and activity listeners based on auth state
   */
  useEffect(() => {
    if (isAuthenticated && user) {
      startHeartbeat()
      setupActivityListeners()
      resetIdleTimeout()
    } else {
      stopHeartbeat()
      cleanupActivityListeners()
      clearIdleTimeout()
    }

    return () => {
      stopHeartbeat()
      cleanupActivityListeners()
      clearIdleTimeout()
    }
  }, [
    isAuthenticated,
    user,
    startHeartbeat,
    stopHeartbeat,
    setupActivityListeners,
    cleanupActivityListeners,
    resetIdleTimeout,
    clearIdleTimeout
  ])

  /**
   * Cleanup on unmount
   */
  useEffect(() => {
    return () => {
      stopHeartbeat()
      cleanupActivityListeners()
      clearIdleTimeout()
      if (activityTimeoutRef.current) {
        clearTimeout(activityTimeoutRef.current)
      }
    }
  }, [stopHeartbeat, cleanupActivityListeners, clearIdleTimeout])

  return {
    user,
    isAuthenticated,
    isLoading,
    login,
    logout,
    updateActivity: debouncedUpdateActivity
  }
}

/**
 * Hook for checking authentication status
 */
export function useRequireAuth() {
  const { isAuthenticated, isLoading } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [isAuthenticated, isLoading, router])

  return { isAuthenticated, isLoading }
}

/**
 * Hook for role-based access control
 */
export function useRequireRole(requiredRoles: string[]) {
  const { user, isAuthenticated, isLoading } = useAuth()
  const router = useRouter()

  const hasRequiredRole = user && requiredRoles.includes(user.role)

  useEffect(() => {
    if (!isLoading && isAuthenticated && !hasRequiredRole) {
      router.push('/unauthorized')
    }
  }, [isAuthenticated, isLoading, hasRequiredRole, router])

  return { 
    isAuthenticated, 
    isLoading, 
    hasRequiredRole,
    userRole: user?.role 
  }
}
