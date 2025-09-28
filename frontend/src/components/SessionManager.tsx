'use client'

import { useEffect, useState } from 'react'
import { authManager } from '@/utils/auth'
import SessionExpirationManager from './SessionExpirationManager'

interface SessionInfo {
  isActive: boolean
  lastActivity: Date
  timeUntilExpiry: number | null
  tokenExpiration: Date | null
}

interface SessionManagerProps {
  children: React.ReactNode
  showSessionInfo?: boolean
}

export default function SessionManager({ 
  children, 
  showSessionInfo = false 
}: SessionManagerProps) {
  const [sessionInfo, setSessionInfo] = useState<SessionInfo>({
    isActive: false,
    lastActivity: new Date(),
    timeUntilExpiry: null,
    tokenExpiration: null
  })

  useEffect(() => {
    if (!authManager.isAuthenticated()) {
      return
    }

    const updateSessionInfo = () => {
      const expiration = authManager.getTokenExpiration()
      const timeUntilExpiry = authManager.getTimeUntilExpiry()
      
      setSessionInfo({
        isActive: authManager.isAuthenticated(),
        lastActivity: new Date(),
        timeUntilExpiry,
        tokenExpiration: expiration
      })
    }

    // Update session info immediately
    updateSessionInfo()

    // Update session info every minute
    const interval = setInterval(updateSessionInfo, 60000)

    return () => clearInterval(interval)
  }, [])

  const formatTimeRemaining = (milliseconds: number | null): string => {
    if (!milliseconds) return 'Unknown'
    
    const minutes = Math.floor(milliseconds / 60000)
    const seconds = Math.floor((milliseconds % 60000) / 1000)
    
    if (minutes > 0) {
      return `${minutes}m ${seconds}s`
    }
    return `${seconds}s`
  }

  const getSessionStatusColor = (): string => {
    if (!sessionInfo.timeUntilExpiry) return 'text-gray-500'
    
    const minutesRemaining = sessionInfo.timeUntilExpiry / 60000
    
    if (minutesRemaining < 5) return 'text-red-500'
    if (minutesRemaining < 15) return 'text-yellow-500'
    return 'text-green-500'
  }

  return (
    <SessionExpirationManager>
      {children}
      
      {showSessionInfo && authManager.isAuthenticated() && (
        <div className="fixed bottom-4 right-4 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg shadow-lg p-4 max-w-sm">
          <div className="flex items-center justify-between mb-2">
            <h3 className="text-sm font-medium text-gray-900 dark:text-white">
              Session Status
            </h3>
            <div className={`w-2 h-2 rounded-full ${getSessionStatusColor()}`}></div>
          </div>
          
          <div className="space-y-1 text-xs text-gray-600 dark:text-gray-400">
            <div>
              <span className="font-medium">Status:</span> 
              <span className={`ml-1 ${getSessionStatusColor()}`}>
                {sessionInfo.isActive ? 'Active' : 'Inactive'}
              </span>
            </div>
            
            {sessionInfo.timeUntilExpiry && (
              <div>
                <span className="font-medium">Expires in:</span> 
                <span className={`ml-1 ${getSessionStatusColor()}`}>
                  {formatTimeRemaining(sessionInfo.timeUntilExpiry)}
                </span>
              </div>
            )}
            
            {sessionInfo.tokenExpiration && (
              <div>
                <span className="font-medium">Expires at:</span> 
                <span className="ml-1">
                  {sessionInfo.tokenExpiration.toLocaleTimeString()}
                </span>
              </div>
            )}
          </div>
          
          <div className="mt-3 pt-2 border-t border-gray-200 dark:border-gray-700">
            <button
              onClick={() => authManager.logout()}
              className="text-xs text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
            >
              Logout
            </button>
          </div>
        </div>
      )}
    </SessionExpirationManager>
  )
}

// Hook for session management
export function useSession() {
  const [sessionInfo, setSessionInfo] = useState<SessionInfo>({
    isActive: false,
    lastActivity: new Date(),
    timeUntilExpiry: null,
    tokenExpiration: null
  })

  useEffect(() => {
    if (!authManager.isAuthenticated()) {
      return
    }

    const updateSessionInfo = () => {
      const expiration = authManager.getTokenExpiration()
      const timeUntilExpiry = authManager.getTimeUntilExpiry()
      
      setSessionInfo({
        isActive: authManager.isAuthenticated(),
        lastActivity: new Date(),
        timeUntilExpiry,
        tokenExpiration: expiration
      })
    }

    updateSessionInfo()
    const interval = setInterval(updateSessionInfo, 60000)

    return () => clearInterval(interval)
  }, [])

  return {
    ...sessionInfo,
    logout: () => authManager.logout(),
    refreshToken: () => authManager.refreshToken()
  }
}
