'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { api } from '@/utils/api'
import { useAuth } from '@/hooks/useAuth'
import Image from 'next/image'
import { Eye, EyeOff, Lock, Mail, AlertCircle, User, GraduationCap, BookOpen } from 'lucide-react'

interface SignupFormData {
  name: string
  email: string
  password: string
  confirmPassword: string
  agreeToTerms: boolean
}

const USER_DASHBOARD_ROUTES = {
  ADMIN: '/admin/dashboard',
  TEACHER: '/teacher/profile',
  STUDENT: '/student/profile'
} as const

function StudentSignupForm() {
  const [formData, setFormData] = useState<SignupFormData>({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    agreeToTerms: false
  })
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const router = useRouter()
  const searchParams = useSearchParams()
  const { login, isAuthenticated } = useAuth()

  useEffect(() => {
    // Redirect if already authenticated
    if (isAuthenticated) {
      const redirectTo = searchParams.get('redirect') || '/'
      router.push(redirectTo)
      return
    }
  }, [isAuthenticated, router, searchParams])

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
    if (!formData.name.trim()) {
      setError('Full name is required')
      return false
    }

    if (!formData.email.trim()) {
      setError('Email is required')
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

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match')
      return false
    }

    if (!formData.agreeToTerms) {
      setError('You must agree to the terms and conditions')
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
      console.log('🔐 Starting student signup...')
      const response = await api.post('/auth/signup/student', {
        name: formData.name,
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
          name: userData.name || formData.name,
          role: userData.role || 'STUDENT'
        }

        console.log('✨ Processed user data:', processedUserData)

        if (!processedUserData.id || !processedUserData.role) {
          console.error('❌ Invalid user data - missing id or role')
          setError('Invalid response from server. Please try again.')
          setIsLoading(false)
          return
        }

        // Update authentication state
        console.log('🔑 Setting authentication state...')
        login(processedUserData)

        // Redirect to student dashboard
        const redirectTo = searchParams.get('redirect') || USER_DASHBOARD_ROUTES.STUDENT
        
        console.log('🚀 Redirecting to:', redirectTo)
        
        // Use window.location for immediate redirect to ensure it works
        window.location.href = redirectTo
        
      } else {
        console.error('❌ Signup failed - invalid user data')
        console.error('User data:', userData)
        setError('Signup failed. Please try again.')
        setIsLoading(false)
      }
    } catch (err: any) {
      console.error('Signup error:', err)
      
      // Handle specific error cases
      if (err.response?.status === 409) {
        setError('An account with this email already exists. Please try logging in instead.')
      } else if (err.response?.status >= 500) {
        setError('Server error. Please try again later.')
      } else {
        setError('Signup failed. Please try again.')
      }
      setIsLoading(false)
    }
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
            Join as Student
          </h2>
          <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
            Create your student account to start learning
          </p>
        </div>

        {/* Signup Form */}
        <div className="bg-white dark:bg-gray-800 py-8 px-6 shadow-xl rounded-xl border-0">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {/* Name Field */}
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                <User className="inline h-4 w-4 mr-2" />
                Full Name
              </label>
              <input
                id="name"
                name="name"
                type="text"
                autoComplete="name"
                required
                value={formData.name}
                onChange={handleInputChange}
                className="w-full px-4 py-3 border-2 border-gray-200 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 transition-all duration-200"
                placeholder="Enter your full name"
              />
            </div>

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
                  autoComplete="new-password"
                  required
                  value={formData.password}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 pr-12 border-2 border-gray-200 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 transition-all duration-200"
                  placeholder="Create a password"
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

            {/* Confirm Password Field */}
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                <Lock className="inline h-4 w-4 mr-2" />
                Confirm Password
              </label>
              <div className="relative">
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirmPassword ? 'text' : 'password'}
                  autoComplete="new-password"
                  required
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 pr-12 border-2 border-gray-200 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 transition-all duration-200"
                  placeholder="Confirm your password"
                />
                <button
                  type="button"
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                >
                  {showConfirmPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                </button>
              </div>
            </div>

            {/* Terms Agreement */}
            <div className="flex items-center">
              <input
                id="agreeToTerms"
                name="agreeToTerms"
                type="checkbox"
                checked={formData.agreeToTerms}
                onChange={handleInputChange}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              />
              <label htmlFor="agreeToTerms" className="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                I agree to the{' '}
                <button
                  type="button"
                  className="text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300"
                >
                  Terms and Conditions
                </button>
                {' '}and{' '}
                <button
                  type="button"
                  className="text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300"
                >
                  Privacy Policy
                </button>
              </label>
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
                  Creating Account...
                </>
              ) : (
                <>
                  <GraduationCap className="h-4 w-4 mr-2" />
                  Create Student Account
                </>
              )}
            </button>
          </form>

          {/* Login Link */}
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Already have an account?{' '}
              <button
                onClick={() => router.push('/login')}
                className="text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300 font-medium"
              >
                Sign in
              </button>
            </p>
          </div>
        </div>

        {/* Security Notice */}
        <div className="text-center">
          <div className="text-xs text-gray-500 dark:text-gray-400 space-y-1">
            <p>🔒 Your connection is secured with enterprise-grade encryption</p>
            <p>📚 Start your learning journey with Ankurshala</p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function StudentSignupPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    }>
      <StudentSignupForm />
    </Suspense>
  )
}