'use client'

import { useEffect, useState } from 'react'

interface RetryConfig {
  maxRetries?: number
  retryDelay?: number
  onRetry?: () => void
}

export function useChunkErrorRetry(config: RetryConfig = {}) {
  const { maxRetries = 3, retryDelay = 1000, onRetry } = config
  const [retryCount, setRetryCount] = useState(0)
  const [isRetrying, setIsRetrying] = useState(false)

  const retry = () => {
    if (retryCount < maxRetries) {
      setIsRetrying(true)
      onRetry?.()
      
      setTimeout(() => {
        setRetryCount(prev => prev + 1)
        setIsRetrying(false)
        window.location.reload()
      }, retryDelay)
    } else {
      // Max retries reached, redirect to homepage
      window.location.href = '/'
    }
  }

  useEffect(() => {
    const handleChunkError = (event: ErrorEvent) => {
      if (event.message?.includes('Loading chunk') || event.message?.includes('ChunkLoadError')) {
        event.preventDefault()
        retry()
      }
    }

    const handleUnhandledRejection = (event: PromiseRejectionEvent) => {
      if (event.reason?.message?.includes('Loading chunk') || event.reason?.message?.includes('ChunkLoadError')) {
        event.preventDefault()
        retry()
      }
    }

    window.addEventListener('error', handleChunkError)
    window.addEventListener('unhandledrejection', handleUnhandledRejection)

    return () => {
      window.removeEventListener('error', handleChunkError)
      window.removeEventListener('unhandledrejection', handleUnhandledRejection)
    }
  }, [retryCount])

  return { retry, retryCount, isRetrying, canRetry: retryCount < maxRetries }
}

