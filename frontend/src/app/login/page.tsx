'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { authManager } from '@/utils/auth'
import { api } from '@/utils/api'
import { useAuthStore } from '@/store/auth'
import Image from 'next/image'
import { Eye, EyeOff, Lock, Mail, AlertCircle } from 'lucide-react'

interface LoginFormData {
  email: string
  password: string
  rememberMe: boolean
}

interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  userId: number
  name: string
  email: string
  role: string
}

const USER_DASHBOARD_ROUTES = {
  ADMIN: '/admin/dashboard',
  TEACHER: '/teacher/profile',
  STUDENT: '/student/profile'
} as const

function LoginForm() {
  const [formData, setFormData] = useState<LoginFormData>({
    email: '',
    password: '',
    rememberMe: false
  })
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const router = useRouter()
  const searchParams = useSearchParams()
  const { login } = useAuthStore()

  useEffect(() => {
    // Remove any exposed credentials from URL params immediately for security
    const urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has('email') || urlParams.has('password')) {
      // Clear URL params without reloading the page
      window.history.replaceState({}, document.title, window.location.pathname)
      
      // Show security warning
      setError('Security Notice: Credentials in URL have been cleared. Please enter them manually.')
      setTimeout(() => setError(''), 5000)
    }

    // Check for redirect message
    const message = urlParams.get('message')
    if (message) {
      setError(message)
      // Clear the message from URL after showing it
      const newUrl = new URL(window.location.href)
      newUrl.searchParams.delete('message')
      window.history.replaceState({}, document.title, newUrl.toString())
    }

    // Check if user is already authenticated and redirect to appropriate dashboard
    const checkAuth = async () => {
      try {
        if (authManager.isAuthenticated()) {
          try {
            const response = await api.get('/user/me')
            const userRole = (response.data as any).role
            
            // Redirect to appropriate dashboard based on role
            const redirectTo = USER_DASHBOARD_ROUTES[userRole as keyof typeof USER_DASHBOARD_ROUTES] || '/'
            router.push(redirectTo)
          } catch (error) {
            // Token is invalid, clear it and stay on login page
            console.log('Token validation failed, clearing auth state')
            authManager.logout()
          }
        }
      } catch (error) {
        console.error('Auth check error:', error)
        authManager.logout()
      }
    }

    checkAuth()
  }, [router])

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target
    
    // Sanitize input
    const sanitizedValue = value
      .replace(/[<>]/g, '')
      .replace(/javascript:/gi, '')
      .replace(/on\w+=/gi, '')
      .trim()

    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : sanitizedValue
    }))
    
    // Clear error when user starts typing
    if (error) {
      setError('')
    }
  }

  const validateForm = (): boolean => {
    if (!formData.email.trim()) {
      setError('Email is required')
      return false
    }

    if (!formData.password.trim()) {
      setError('Password is required')
      return false
    }

    // Basic email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(formData.email)) {
      setError('Please enter a valid email address')
      return false
    }

    // Password strength validation
    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters long')
      return false
    }

    return true
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    setIsLoading(true)
    setError('')

    try {
      const response = await api.post<LoginResponse>('/auth/signin', {
        email: formData.email,
        password: formData.password
      }, { requireAuth: false })

      if (response.data) {
        const userData = {
          id: response.data.userId.toString(),
          email: response.data.email,
          name: response.data.name,
          role: response.data.role
        }

        // Set authentication data in both systems
        authManager.setAuth(
          response.data.accessToken,
          response.data.refreshToken,
          userData
        )
        
        // Also update Zustand store for RouteGuard compatibility
        login(userData)

        // Get redirect URL from search params or determine based on role
        const redirectTo = searchParams.get('redirect') || 
          USER_DASHBOARD_ROUTES[response.data.role as keyof typeof USER_DASHBOARD_ROUTES] || '/'
        
        // Use replace for WebKit compatibility to avoid navigation interruption
        router.replace(redirectTo)
      }
    } catch (err: any) {
      console.error('Login error:', err)
      
      // Handle specific error cases
      if (err.response?.status === 401) {
        setError('Invalid email or password. Please check your credentials and try again.')
      } else if (err.response?.status === 429) {
        setError('Too many login attempts. Please wait a few minutes before trying again.')
      } else if (err.response?.status >= 500) {
        setError('Server error. Please try again later.')
      } else {
        setError('Login failed. Please check your credentials and try again.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  const handleForgotPassword = () => {
    setError('Forgot password functionality will be available soon. Please contact support if needed.')
    setTimeout(() => setError(''), 5000)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-indigo-50 dark:from-gray-900 dark:via-gray-900 dark:to-gray-800 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        {/* Header with Logo */}
        <div className="text-center">
          <div className="mx-auto h-24 w-24 mb-6">
            <Image
              src="/Ankurshala Logo - Watermark (Small) - 300x300.png"
              alt="Ankurshala"
              width={96}
              height={96}
              className="mx-auto rounded-xl shadow-lg"
              priority
            />
          </div>
          <h2 className="text-3xl font-bold text-gray-900 dark:text-white">
            Welcome Back
          </h2>
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
            Sign in to your Ankurshala account
          </p>
        </div>

        {/* Login Form */}
        <div className="bg-white dark:bg-gray-800 py-8 px-6 shadow-xl rounded-xl border-0">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {/* Email Field */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                <Mail className="inline h-4 w-4 mr-2" />
                Email Address
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                required
                value={formData.email}
                onChange={handleInputChange}
                className="w-full px-4 py-3 border-2 border-gray-200 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 transition-all duration-200"
                placeholder="Enter your email"
              />
            </div>

            {/* Password Field */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                <Lock className="inline h-4 w-4 mr-2" />
                Password
              </label>
              <div className="relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  value={formData.password}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 pr-12 border-2 border-gray-200 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 transition-all duration-200"
                  placeholder="Enter your password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                </button>
              </div>
            </div>

            {/* Remember Me and Forgot Password */}
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <input
                  id="rememberMe"
                  name="rememberMe"
                  type="checkbox"
                  checked={formData.rememberMe}
                  onChange={handleInputChange}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="rememberMe" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                  Remember me
                </label>
              </div>
              <button
                type="button"
                onClick={handleForgotPassword}
                className="text-sm text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300"
              >
                Forgot password?
              </button>
            </div>

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 dark:bg-red-900/20 border-2 border-red-200 dark:border-red-800 rounded-lg p-4">
                <div className="flex">
                  <AlertCircle className="h-5 w-5 text-red-400 mr-3 mt-0.5 flex-shrink-0" />
                  <p className="text-sm text-red-800 dark:text-red-200">{error}</p>
                </div>
              </div>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex justify-center items-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none transition-all duration-200 transform hover:scale-[1.02]"
            >
              {isLoading ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          {/* Registration Links */}
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Don't have an account?{' '}
              <button
                onClick={() => router.push('/register-student')}
                className="text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300 font-medium"
              >
                Register as Student
              </button>
              {' or '}
              <button
                onClick={() => router.push('/register-teacher')}
                className="text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300 font-medium"
              >
                Register as Teacher
              </button>
            </p>
          </div>
        </div>

        {/* Security Notice */}
        <div className="text-center">
          <div className="text-xs text-gray-500 dark:text-gray-400 space-y-1">
            <p>🔒 Your connection is secured with enterprise-grade encryption</p>
            <p>⏰ Sessions expire automatically after 45 minutes of inactivity</p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    }>
      <LoginForm />
    </Suspense>
  )
}