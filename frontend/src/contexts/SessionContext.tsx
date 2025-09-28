'use client'

import { createContext, useContext, useEffect, useState } from 'react'
import { authManager } from '@/utils/auth'
import SessionExpirationManager from '@/components/SessionExpirationManager'

interface SessionContextType {
  isAuthenticated: boolean
  user: any | null
  timeUntilExpiry: number | null
  extendSession: () => Promise<boolean>
  logout: () => Promise<void>
}

const SessionContext = createContext<SessionContextType | undefined>(undefined)

interface SessionProviderProps {
  children: React.ReactNode
}

export function SessionProvider({ children }: SessionProviderProps) {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState<any>(null)
  const [timeUntilExpiry, setTimeUntilExpiry] = useState<number | null>(null)

  useEffect(() => {
    const updateSessionState = () => {
      const authenticated = authManager.isAuthenticated()
      const currentUser = authManager.getUser()
      const timeUntilExpiry = authManager.getTimeUntilExpiry()
      
      setIsAuthenticated(authenticated)
      setUser(currentUser)
      setTimeUntilExpiry(timeUntilExpiry)
    }

    // Initial state update
    updateSessionState()

    // Update state every 30 seconds
    const interval = setInterval(updateSessionState, 30000)

    return () => clearInterval(interval)
  }, [])

  const extendSession = async (): Promise<boolean> => {
    try {
      const refreshed = await authManager.refreshToken()
      if (refreshed) {
        const timeUntilExpiry = authManager.getTimeUntilExpiry()
        setTimeUntilExpiry(timeUntilExpiry)
        return true
      }
      return false
    } catch (error) {
      console.error('Error extending session:', error)
      return false
    }
  }

  const logout = async () => {
    await authManager.logout()
    setIsAuthenticated(false)
    setUser(null)
    setTimeUntilExpiry(null)
  }

  const value: SessionContextType = {
    isAuthenticated,
    user,
    timeUntilExpiry,
    extendSession,
    logout
  }

  return (
    <SessionContext.Provider value={value}>
      <SessionExpirationManager>
        {children}
      </SessionExpirationManager>
    </SessionContext.Provider>
  )
}

export function useSessionContext() {
  const context = useContext(SessionContext)
  if (context === undefined) {
    throw new Error('useSessionContext must be used within a SessionProvider')
  }
  return context
}
