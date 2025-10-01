'use client'

import { useEffect, useState, useCallback } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Progress } from '@/components/ui/progress'
import { AlertCircle, Clock, Shield } from 'lucide-react'
import { authManager } from '@/utils/auth'
import { toast } from 'sonner'

interface SessionExpirationManagerProps {
  children: React.ReactNode
  warningThreshold?: number // in milliseconds, default 5 minutes
  checkInterval?: number // in milliseconds, default 30 seconds
}

export default function SessionExpirationManager({ 
  children, 
  warningThreshold = 5 * 60 * 1000, // 5 minutes
  checkInterval = 30 * 1000 // 30 seconds
}: SessionExpirationManagerProps) {
  const [isWarningVisible, setIsWarningVisible] = useState(false)
  const [timeRemaining, setTimeRemaining] = useState(0)
  const [isExtending, setIsExtending] = useState(false)
  const [countdownInterval, setCountdownInterval] = useState<NodeJS.Timeout | null>(null)
  const [hasShownWarning, setHasShownWarning] = useState(false) // Prevent multiple warnings

  const checkSessionExpiration = useCallback(() => {
    if (!authManager.isAuthenticated()) {
      setIsWarningVisible(false)
      setHasShownWarning(false)
      return
    }

    const timeUntilExpiry = authManager.getTimeUntilExpiry()
    
    if (timeUntilExpiry && timeUntilExpiry <= warningThreshold && timeUntilExpiry > 0) {
      if (!isWarningVisible && !hasShownWarning) {
        setIsWarningVisible(true)
        setHasShownWarning(true)
        setTimeRemaining(timeUntilExpiry)
        startCountdown()
        toast.warning('Your session will expire soon', {
          description: `You have ${formatTime(timeUntilExpiry)} remaining`,
          duration: 5000
        })
      }
    } else if (timeUntilExpiry && timeUntilExpiry <= 0) {
      // Token has expired, force logout
      handleForceLogout()
    } else if (timeUntilExpiry && timeUntilExpiry > warningThreshold) {
      // Reset warning flag if session is extended beyond threshold
      setHasShownWarning(false)
    }
  }, [isWarningVisible, hasShownWarning, warningThreshold])

  const startCountdown = useCallback(() => {
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
        handleForceLogout()
      }
    }, 1000)

    setCountdownInterval(interval)
  }, [countdownInterval])

  const handleExtendSession = async () => {
    try {
      setIsExtending(true)
      
      // Attempt to refresh the token
      const refreshed = await authManager.refreshToken()
      if (refreshed) {
        setIsWarningVisible(false)
        setHasShownWarning(false) // Reset warning flag after successful extension
        if (countdownInterval) {
          clearInterval(countdownInterval)
          setCountdownInterval(null)
        }
        
        toast.success('Session extended successfully', {
          description: 'Your session has been renewed for another period',
          duration: 3000
        })
      } else {
        // If refresh fails, logout
        handleForceLogout()
      }
    } catch (error) {
      console.error('Error extending session:', error)
      toast.error('Failed to extend session', {
        description: 'Please log in again',
        duration: 5000
      })
      handleForceLogout()
    } finally {
      setIsExtending(false)
    }
  }

  const handleLogout = async () => {
    setIsWarningVisible(false)
    if (countdownInterval) {
      clearInterval(countdownInterval)
      setCountdownInterval(null)
    }
    
    toast.info('Logging out...', {
      description: 'Your session has been ended',
      duration: 2000
    })
    
    await authManager.logout()
  }

  const handleForceLogout = async () => {
    setIsWarningVisible(false)
    if (countdownInterval) {
      clearInterval(countdownInterval)
      setCountdownInterval(null)
    }
    
    toast.error('Session expired', {
      description: 'You have been logged out due to inactivity',
      duration: 5000
    })
    
    await authManager.forceLogout('Your session has expired. Please log in again.')
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

  const getUrgencyLevel = (): 'low' | 'medium' | 'high' => {
    const percentage = getProgressPercentage()
    if (percentage > 50) return 'low'
    if (percentage > 25) return 'medium'
    return 'high'
  }

  useEffect(() => {
    if (!authManager.isAuthenticated()) {
      return
    }

    // Check every specified interval
    const checkIntervalId = setInterval(checkSessionExpiration, checkInterval)
    
    // Initial check
    checkSessionExpiration()

    return () => {
      clearInterval(checkIntervalId)
      if (countdownInterval) {
        clearInterval(countdownInterval)
      }
    }
  }, [checkSessionExpiration, checkInterval, countdownInterval])

  return (
    <>
      {children}
      
      {/* Session Expiration Warning Dialog */}
      <Dialog open={isWarningVisible} onOpenChange={() => {}}>
        <DialogContent 
          className="sm:max-w-md" 
          onPointerDownOutside={(e) => e.preventDefault()}
          onEscapeKeyDown={(e) => e.preventDefault()}
        >
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <AlertCircle className="h-5 w-5 text-yellow-500" />
              Session Expiring Soon
            </DialogTitle>
            <DialogDescription>
              Your session will expire in <strong>{formatTime(timeRemaining)}</strong>. 
              Would you like to extend your session or logout?
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            {/* Time remaining progress */}
            <div className="space-y-2">
              <div className="flex justify-between text-sm text-gray-600 dark:text-gray-400">
                <span className="flex items-center gap-1">
                  <Clock className="h-4 w-4" />
                  Time remaining
                </span>
                <span className="font-medium">{formatTime(timeRemaining)}</span>
              </div>
              <Progress 
                value={getProgressPercentage()} 
                className="h-2"
              />
              <div className={`h-2 rounded-full ${getProgressColor()} transition-all duration-1000`} 
                   style={{ width: `${getProgressPercentage()}%` }} />
            </div>

            {/* Urgency indicator */}
            <div className={`p-3 rounded-md ${
              getUrgencyLevel() === 'high' ? 'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800' :
              getUrgencyLevel() === 'medium' ? 'bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800' :
              'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800'
            }`}>
              <div className="flex items-center gap-2">
                <Shield className={`h-4 w-4 ${
                  getUrgencyLevel() === 'high' ? 'text-red-500' :
                  getUrgencyLevel() === 'medium' ? 'text-yellow-500' :
                  'text-green-500'
                }`} />
                <p className={`text-sm font-medium ${
                  getUrgencyLevel() === 'high' ? 'text-red-800 dark:text-red-200' :
                  getUrgencyLevel() === 'medium' ? 'text-yellow-800 dark:text-yellow-200' :
                  'text-green-800 dark:text-green-200'
                }`}>
                  {getUrgencyLevel() === 'high' ? 'Critical: Session expires very soon!' :
                   getUrgencyLevel() === 'medium' ? 'Warning: Session expires soon' :
                   'Info: Session expires in a few minutes'}
                </p>
              </div>
            </div>

            {/* Action descriptions */}
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
              disabled={isExtending}
            >
              Logout
            </Button>
            <Button
              onClick={handleExtendSession}
              className="flex-1 bg-green-600 hover:bg-green-700"
              disabled={isExtending}
            >
              {isExtending ? (
                <div className="flex items-center gap-2">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                  Extending...
                </div>
              ) : (
                'Extend Session'
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
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
