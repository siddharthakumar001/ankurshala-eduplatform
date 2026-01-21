'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Image from 'next/image'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/store/auth'
import { authAPI } from '@/lib/apiClient'
import { useTheme } from '@/components/theme-provider'
import { Sun, Moon } from 'lucide-react'

// Stage-1 FE complete: Navigation bar with authentication state
export default function Navbar() {
  const router = useRouter()
  const { user, logout } = useAuthStore()
  const { theme, setTheme } = useTheme()

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark')
  }

  const handleLogout = async () => {
    try {
      // Use the centralized logout from auth store which handles everything
      await logout()
      // Redirect to home page after logout
      router.push('/')
    } catch (error) {
      console.error('Logout error:', error)
      // Even if logout fails, redirect to home
      router.push('/')
    }
  }

  return (
    <nav className="bg-white/70 dark:bg-slate-900/70 shadow-sm border-b border-white/40 dark:border-white/10 backdrop-blur-2xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo and Brand */}
          <div className="flex items-center">
            <Link href="/" className="flex items-center space-x-3">
              <Image
                src="/ankurshala-logo-small.png"
                alt="Ankurshala Logo"
                width={32}
                height={32}
                className="rounded-lg"
              />
              <span className="text-xl font-display font-semibold text-gray-900 dark:text-white">AnkurShala</span>
            </Link>
          </div>

          {/* Navigation Links and User Actions */}
          <div className="flex items-center space-x-4">
            {/* Theme Toggle */}
            <Button
              variant="ghost"
              size="icon"
              onClick={toggleTheme}
              className="text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>
            
            {user ? (
              <>
                {/* User Info with Role Badge */}
                <div className="flex items-center space-x-3">
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900 dark:text-white">{user.name}</p>
                    <div className="flex items-center space-x-2">
                      <span
                        className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${
                          user.role === 'STUDENT'
                            ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/60 dark:text-emerald-200'
                            : user.role === 'TEACHER'
                            ? 'bg-sky-100 text-sky-800 dark:bg-sky-900/60 dark:text-sky-200'
                            : 'bg-violet-100 text-violet-800 dark:bg-violet-900/60 dark:text-violet-200'
                        }`}
                      >
                        {user.role?.toLowerCase()}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Role-based Navigation */}
                <div className="flex items-center space-x-2">
                  {user.role === 'STUDENT' && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => router.push('/student/profile')}
                    >
                      My Profile
                    </Button>
                  )}
                  {user.role === 'TEACHER' && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => router.push('/teacher/profile')}
                    >
                      My Profile
                    </Button>
                  )}
                  {user.role === 'ADMIN' && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => router.push('/admin/profile')}
                    >
                      Admin Panel
                    </Button>
                  )}

                  <Button variant="outline" size="sm" onClick={handleLogout}>
                    Logout
                  </Button>
                </div>
              </>
            ) : (
              <>
                {/* Guest Navigation */}
                <div className="flex items-center space-x-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => router.push('/login')}
                  >
                    Login
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => router.push('/register-student')}
                  >
                    Student
                  </Button>
                  <Button
                    size="sm"
                    onClick={() => router.push('/register-teacher')}
                  >
                    Teacher
                  </Button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
