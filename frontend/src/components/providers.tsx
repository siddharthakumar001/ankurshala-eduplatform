'use client'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState, useEffect } from 'react'
import { Toaster } from 'sonner'
import { useAuthStore } from '@/store/auth'
import { ThemeProvider } from '@/components/theme-provider'
import { useChunkErrorRetry } from '@/hooks/use-chunk-error-retry'

export default function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 60 * 1000, // 1 minute
        retry: 1,
      },
    },
  }))

  const { initializeAuth } = useAuthStore()
  
  // Handle chunk loading errors
  useChunkErrorRetry({
    maxRetries: 3,
    retryDelay: 1000,
    onRetry: () => {
      console.log('Retrying chunk load...')
    }
  })

  useEffect(() => {
    initializeAuth().catch(console.error)
  }, [initializeAuth])

  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider
        defaultTheme="system"
        storageKey="ankurshala-ui-theme"
      >
        {children}
        <Toaster position="top-right" richColors />
      </ThemeProvider>
    </QueryClientProvider>
  )
}
