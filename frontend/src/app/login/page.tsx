'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { api } from '@/utils/api'
import { useAuth } from '@/hooks/useAuth'
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
  STUDENT: '/student/dashboard'
} as const

function LoginForm() {
  const [formData, setFormData] = useState<LoginFormData>({
    email: '',
    password: '',
    rememberMe: false
  })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const router = useRouter()
  const searchParams = useSearchParams()
  const { login, isAuthenticated, isLoading, user } = useAuth()

  useEffect(() => {
    // Debug authentication state
    console.log('🔍 Login page auth state:', { isLoading, isAuthenticated, user })
    
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
      setError(message || '')
      // Clear the message from URL after showing it
      const newUrl = new URL(window.location.href)
      newUrl.searchParams.delete('message')
      window.history.replaceState({}, document.title, newUrl.toString())
    }

    // IMPORTANT: Only redirect authenticated users if they explicitly came from a protected route
    // Don't auto-redirect based on auth state - allow users to access the login page
    // This prevents redirect loops and allows users to switch accounts
    const redirectParam = searchParams.get('redirect')
    
    // Only redirect if:
    // 1. There's an explicit redirect parameter (user came from a protected route)
    // 2. Auth is fully loaded (not loading)
    // 3. User is actually authenticated
    // 4. User has valid user data
    if (redirectParam && !isLoading && isAuthenticated && user?.id) {
      // User came here with a redirect param and is authenticated - send them to the intended destination
      console.log('🚀 Redirecting authenticated user to:', redirectParam)
      router.push(redirectParam)
      return
    }
    
    // If user is authenticated but no redirect param, DO NOT redirect
    // Allow them to stay on login page - they might want to:
    // - Switch accounts
    // - View the login page
    // - Logout and login as a different user
    // This prevents unwanted redirects to home page
  }, [isAuthenticated, isLoading, user, router, searchParams])

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

    setIsSubmitting(true)
    setError('')

    try {
      console.log('🔐 Starting login...')
      const response = await api.post('/auth/signin', {
        email: formData.email,
        password: formData.password
      }, { requireAuth: false })

      console.log('📥 Raw response:', response)
      console.log('📦 Response data:', response.data)

      // The API client already extracts the data from the API response
      // So response.data contains the user data directly
      const userData = response.data as any
      
      console.log('✅ User data received:', userData)
      
      if (userData && userData.userId && userData.role) {
        const processedUserData = {
          id: userData.userId?.toString() || '',
          email: userData.email || formData.email,
          name: userData.name || '',
          role: userData.role || ''
        }

        console.log('✨ Processed user data:', processedUserData)

        if (!processedUserData.id || !processedUserData.role) {
          console.error('❌ Invalid user data - missing id or role')
          setError('Invalid response from server. Please try again.')
          setIsSubmitting(false)
          return
        }

        // Update authentication state
        console.log('🔑 Setting authentication state...')
        login(processedUserData)

        // Get redirect URL from search params or determine based on role
        const redirectTo = searchParams.get('redirect') || 
          USER_DASHBOARD_ROUTES[processedUserData.role as keyof typeof USER_DASHBOARD_ROUTES] || '/'
        
        console.log('🚀 Redirecting to:', redirectTo)
        
        // Small delay to ensure auth state is persisted before redirect
        // This prevents the redirect from happening before state is saved
        setTimeout(() => {
          // Use window.location for immediate redirect to ensure it works
          // This bypasses React state batching issues
          window.location.href = redirectTo
        }, 100)
        
      } else {
        console.error('❌ Login failed - invalid user data')
        console.error('User data:', userData)
        setError('Login failed. Please check your credentials.')
        setIsSubmitting(false)
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
      setIsSubmitting(false)
    }
  }

  const handleForgotPassword = () => {
    setError('Forgot password functionality will be available soon. Please contact support if needed.')
    setTimeout(() => setError(''), 5000)
  }

  return (
    <div className="min-h-screen flex">
      {/* Left Side - Brand Section */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-ankur-secondary via-[#1a3050] to-ankur-secondary-dark relative overflow-hidden">
        {/* Decorative Elements */}
        <div className="absolute inset-0">
          <div className="absolute top-20 -left-20 w-96 h-96 bg-ankur-primary/10 rounded-full blur-3xl" />
          <div className="absolute bottom-20 -right-20 w-96 h-96 bg-ankur-accent/10 rounded-full blur-3xl" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] border border-white/5 rounded-full" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[400px] h-[400px] border border-white/5 rounded-full" />
        </div>
        
        <div className="relative z-10 flex flex-col justify-center px-16 text-white">
          <div className="mb-8">
            <div className="w-16 h-16 rounded-2xl bg-ankur-primary flex items-center justify-center mb-6">
              <Image
                src="/ankurshala-logo-small.png"
                alt="Ankurshala"
                width={48}
                height={48}
                className="rounded-lg"
                priority
              />
            </div>
            <h1 className="text-4xl font-bold mb-4">
              Welcome to <span className="text-ankur-accent">AnkurShala</span>
            </h1>
            <p className="text-xl text-white/70 leading-relaxed">
              Your trusted platform for quality education. Connect with expert teachers and unlock your learning potential.
            </p>
          </div>
          
          {/* Feature Highlights */}
          <div className="space-y-4">
            {[
              { title: 'Expert Teachers', description: 'Learn from verified, experienced educators' },
              { title: 'Flexible Scheduling', description: 'Book classes at your convenience' },
              { title: 'Secure Payments', description: 'Safe and transparent transactions' },
            ].map((feature, idx) => (
              <div key={idx} className="flex items-start gap-3">
                <div className="w-6 h-6 rounded-full bg-ankur-primary/20 flex items-center justify-center flex-shrink-0 mt-0.5">
                  <div className="w-2 h-2 rounded-full bg-ankur-primary" />
                </div>
                <div>
                  <h3 className="font-semibold text-white">{feature.title}</h3>
                  <p className="text-sm text-white/60">{feature.description}</p>
                </div>
              </div>
            ))}
          </div>

          {/* Stats */}
          <div className="mt-12 pt-8 border-t border-white/10">
            <div className="grid grid-cols-3 gap-8">
              <div>
                <p className="text-3xl font-bold text-ankur-accent">1000+</p>
                <p className="text-sm text-white/60">Students</p>
              </div>
              <div>
                <p className="text-3xl font-bold text-ankur-accent">200+</p>
                <p className="text-sm text-white/60">Teachers</p>
              </div>
              <div>
                <p className="text-3xl font-bold text-ankur-accent">50+</p>
                <p className="text-sm text-white/60">Subjects</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Right Side - Login Form */}
      <div className="flex-1 flex items-center justify-center bg-surface px-6 py-12">
        <div className="w-full max-w-md">
          {/* Mobile Logo */}
          <div className="lg:hidden text-center mb-8">
            <div className="inline-flex items-center gap-3 mb-4">
              <div className="w-12 h-12 rounded-xl bg-ankur-primary flex items-center justify-center">
                <Image
                  src="/ankurshala-logo-small.png"
                  alt="Ankurshala"
                  width={36}
                  height={36}
                  className="rounded-lg"
                  priority
                />
              </div>
              <span className="text-2xl font-bold text-ankur-secondary">AnkurShala</span>
            </div>
          </div>

          {/* Form Header */}
          <div className="text-center mb-8">
            <h2 className="text-2xl font-bold text-gray-900">Sign in to your account</h2>
            <p className="mt-2 text-gray-600">
              Welcome back! Please enter your credentials.
            </p>
          </div>

          {/* Login Form */}
          <form className="space-y-5" onSubmit={handleSubmit}>
            {/* Email Field */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                Email Address
              </label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  value={formData.email}
                  onChange={handleInputChange}
                  className="input-modern input-with-icon"
                  placeholder="you@example.com"
                />
              </div>
            </div>

            {/* Password Field */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                Password
              </label>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  value={formData.password}
                  onChange={handleInputChange}
                  className="input-modern input-with-icon pr-12"
                  placeholder="Enter your password"
                />
                <button
                  type="button"
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                </button>
              </div>
            </div>

            {/* Remember Me and Forgot Password */}
            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  id="rememberMe"
                  name="rememberMe"
                  type="checkbox"
                  checked={formData.rememberMe}
                  onChange={handleInputChange}
                  className="w-4 h-4 rounded border-gray-300 text-ankur-primary focus:ring-ankur-primary"
                />
                <span className="text-sm text-gray-600">Remember me</span>
              </label>
              <button
                type="button"
                onClick={handleForgotPassword}
                className="text-sm text-ankur-primary hover:text-ankur-primary-dark font-medium"
              >
                Forgot password?
              </button>
            </div>

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-xl p-4 animate-fade-in">
                <div className="flex items-start gap-3">
                  <AlertCircle className="h-5 w-5 text-red-500 flex-shrink-0 mt-0.5" />
                  <p className="text-sm text-red-700">{error}</p>
                </div>
              </div>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn-primary w-full flex justify-center items-center py-3 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-2 border-white border-t-transparent mr-2"></div>
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="relative my-8">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-4 bg-surface text-gray-500">New to AnkurShala?</span>
            </div>
          </div>

          {/* Registration Links */}
          <div className="grid grid-cols-2 gap-4">
            <button
              onClick={() => router.push('/register-student')}
              className="btn-outline flex items-center justify-center gap-2 py-3"
            >
              <span>👨‍🎓</span>
              <span>Student</span>
            </button>
            <button
              onClick={() => router.push('/register-teacher')}
              className="btn-outline flex items-center justify-center gap-2 py-3"
            >
              <span>👩‍🏫</span>
              <span>Teacher</span>
            </button>
          </div>

          {/* Security Notice */}
          <p className="mt-8 text-center text-xs text-gray-500">
            🔒 Your connection is secured with enterprise-grade encryption
          </p>
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