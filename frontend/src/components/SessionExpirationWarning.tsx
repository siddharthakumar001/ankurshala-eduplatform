'use client'

import { useState, useEffect } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Progress } from '@/components/ui/progress'
import { authManager } from '@/utils/auth'

interface SessionExpirationWarningProps {
  onExtendSession: () => void
  onLogout: () => void
}

export default function SessionExpirationWarning({ 
  onExtendSession, 
  onLogout 
}: SessionExpirationWarningProps) {
  const [isVisible, setIsVisible] = useState(false)
  const [timeRemaining, setTimeRemaining] = useState(0)
  const [warningThreshold, setWarningThreshold] = useState(5 * 60 * 1000) // 5 minutes
  const [countdownInterval, setCountdownInterval] = useState<NodeJS.Timeout | null>(null)

  useEffect(() => {
    if (!authManager.isAuthenticated()) {
      return
    }

    const checkSessionExpiration = () => {
      const timeUntilExpiry = authManager.getTimeUntilExpiry()
      
      if (timeUntilExpiry && timeUntilExpiry <= warningThreshold && timeUntilExpiry > 0) {
        if (!isVisible) {
          setIsVisible(true)
          setTimeRemaining(timeUntilExpiry)
          startCountdown()
        }
      } else if (timeUntilExpiry && timeUntilExpiry <= 0) {
        // Token has expired, force logout
        handleLogout()
      }
    }

    const startCountdown = () => {
      if (countdownInterval) {
        clearInterval(countdownInterval)
      }

      const interval = setInterval(() => {
        const timeUntilExpiry = authManager.getTimeUntilExpiry()
        
        if (timeUntilExpiry && timeUntilExpiry > 0) {
          setTimeRemaining(timeUntilExpiry)
        } else {
          // Token has expired
          clearInterval(interval)
          handleLogout()
        }
      }, 1000)

      setCountdownInterval(interval)
    }

    // Check every 30 seconds
    const checkInterval = setInterval(checkSessionExpiration, 30000)
    
    // Initial check
    checkSessionExpiration()

    return () => {
      clearInterval(checkInterval)
      if (countdownInterval) {
        clearInterval(countdownInterval)
      }
    }
  }, [isVisible, warningThreshold, countdownInterval])

  const handleExtendSession = async () => {
    try {
      setIsVisible(false)
      if (countdownInterval) {
        clearInterval(countdownInterval)
        setCountdownInterval(null)
      }
      
      // Attempt to refresh the token
      const refreshed = await authManager.refreshToken()
      if (refreshed) {
        onExtendSession()
      } else {
        // If refresh fails, logout
        handleLogout()
      }
    } catch (error) {
      console.error('Error extending session:', error)
      handleLogout()
    }
  }

  const handleLogout = () => {
    setIsVisible(false)
    if (countdownInterval) {
      clearInterval(countdownInterval)
      setCountdownInterval(null)
    }
    onLogout()
  }

  const formatTime = (milliseconds: number): string => {
    const minutes = Math.floor(milliseconds / 60000)
    const seconds = Math.floor((milliseconds % 60000) / 1000)
    
    if (minutes > 0) {
      return `${minutes}m ${seconds}s`
    }
    return `${seconds}s`
  }

  const getProgressPercentage = (): number => {
    if (timeRemaining <= 0) return 0
    return Math.max(0, Math.min(100, (timeRemaining / warningThreshold) * 100))
  }

  const getProgressColor = (): string => {
    const percentage = getProgressPercentage()
    if (percentage > 50) return 'bg-green-500'
    if (percentage > 25) return 'bg-yellow-500'
    return 'bg-red-500'
  }

  if (!isVisible) {
    return null
  }

  return (
    <Dialog open={isVisible} onOpenChange={() => {}}>
      <DialogContent className="sm:max-w-md" onPointerDownOutside={(e) => e.preventDefault()}>
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <span className="text-yellow-500">⚠️</span>
            Session Expiring Soon
          </DialogTitle>
          <DialogDescription>
            Your session will expire in <strong>{formatTime(timeRemaining)}</strong>. 
            Would you like to extend your session or logout?
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          <div className="space-y-2">
            <div className="flex justify-between text-sm text-gray-600">
              <span>Time remaining</span>
              <span className="font-medium">{formatTime(timeRemaining)}</span>
            </div>
            <Progress 
              value={getProgressPercentage()} 
              className="h-2"
            />
            <div className={`h-2 rounded-full ${getProgressColor()} transition-all duration-1000`} 
                 style={{ width: `${getProgressPercentage()}%` }} />
          </div>

          <div className="bg-blue-50 dark:bg-blue-900/20 p-3 rounded-md">
            <p className="text-sm text-blue-800 dark:text-blue-200">
              <strong>Extend Session:</strong> Stay logged in and continue your work
              <br />
              <strong>Logout:</strong> End your session and return to login page
            </p>
          </div>
        </div>

        <DialogFooter className="flex gap-2">
          <Button
            variant="outline"
            onClick={handleLogout}
            className="flex-1"
          >
            Logout
          </Button>
          <Button
            onClick={handleExtendSession}
            className="flex-1 bg-green-600 hover:bg-green-700"
          >
            Extend Session
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

// Hook for session expiration management
export function useSessionExpiration() {
  const [isWarningVisible, setIsWarningVisible] = useState(false)
  const [timeRemaining, setTimeRemaining] = useState(0)

  useEffect(() => {
    if (!authManager.isAuthenticated()) {
      return
    }

    const checkExpiration = () => {
      const timeUntilExpiry = authManager.getTimeUntilExpiry()
      const warningThreshold = 5 * 60 * 1000 // 5 minutes

      if (timeUntilExpiry && timeUntilExpiry <= warningThreshold && timeUntilExpiry > 0) {
        setIsWarningVisible(true)
        setTimeRemaining(timeUntilExpiry)
      } else {
        setIsWarningVisible(false)
      }
    }

    // Check every 30 seconds
    const interval = setInterval(checkExpiration, 30000)
    checkExpiration()

    return () => clearInterval(interval)
  }, [])

  const extendSession = async () => {
    try {
      const refreshed = await authManager.refreshToken()
      if (refreshed) {
        setIsWarningVisible(false)
        return true
      }
      return false
    } catch (error) {
      console.error('Error extending session:', error)
      return false
    }
  }

  const logout = () => {
    setIsWarningVisible(false)
    authManager.logout()
  }

  return {
    isWarningVisible,
    timeRemaining,
    extendSession,
    logout
  }
}
