'use client'

import { useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { RefreshCw, Home, AlertTriangle } from 'lucide-react'

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  useEffect(() => {
    // Log the error to an error reporting service
    console.error('Global error:', error)
  }, [error])

  const isChunkError = error.message?.includes('Loading chunk') || error.message?.includes('ChunkLoadError')

  return (
    <html>
      <body>
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
          <div className="max-w-md w-full text-center space-y-6">
            <div className="text-center">
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 dark:bg-red-900/20 mb-4">
                <AlertTriangle className="h-6 w-6 text-red-600 dark:text-red-400" />
              </div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                {isChunkError ? 'Application Update Required' : 'Something went wrong'}
              </h1>
              <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
                {isChunkError 
                  ? 'The application has been updated. Please refresh the page to load the latest version.'
                  : 'An unexpected error occurred. Please try refreshing the page.'
                }
              </p>
            </div>

            <div className="space-y-3">
              <Button
                onClick={() => window.location.reload()}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white"
              >
                <RefreshCw className="h-4 w-4 mr-2" />
                Refresh Page
              </Button>
              
              <Button
                onClick={() => window.location.href = '/'}
                variant="outline"
                className="w-full"
              >
                <Home className="h-4 w-4 mr-2" />
                Go to Homepage
              </Button>
            </div>

            {process.env.NODE_ENV === 'development' && (
              <details className="mt-6 text-left">
                <summary className="cursor-pointer text-sm font-medium text-gray-700 dark:text-gray-300">
                  Error Details (Development)
                </summary>
                <pre className="mt-2 text-xs text-gray-600 dark:text-gray-400 bg-gray-100 dark:bg-gray-800 p-3 rounded overflow-auto">
                  {error.message}
                  {error.stack && `\n\n${error.stack}`}
                </pre>
              </details>
            )}
          </div>
        </div>
      </body>
    </html>
  )
}

