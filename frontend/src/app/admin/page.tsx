'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { authManager } from '@/utils/auth'

export default function AdminPage() {
  const router = useRouter()

  useEffect(() => {
    // Redirect to dashboard
    router.push('/admin/dashboard')
  }, [router])

  return (
    <div className="min-h-screen flex items-center justify-center bg-transparent">
      <div className="text-center glass-panel rounded-2xl border border-white/40 px-8 py-6">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-ankur-primary mx-auto"></div>
        <p className="mt-4 text-gray-600 dark:text-gray-300">Redirecting to dashboard...</p>
      </div>
    </div>
  )
}
