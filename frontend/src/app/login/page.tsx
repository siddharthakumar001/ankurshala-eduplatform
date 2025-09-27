'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import Image from 'next/image'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuthStore } from '@/store/auth'
import { authAPI } from '@/lib/apiClient'
import { useTheme } from '@/components/theme-provider'
import { Sun, Moon, Eye, EyeOff } from 'lucide-react'

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

type LoginForm = z.infer<typeof loginSchema>

export default function LoginPage() {
  const [loading, setLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const router = useRouter()
  const { login } = useAuthStore()
  const { theme, setTheme } = useTheme()

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark')
  }

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  })

  const onSubmit = async (data: LoginForm) => {
    setLoading(true)

    try {
      const response = await authAPI.signin(data.email, data.password)
      
      // Store tokens
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)
      
      // Update auth store
      login({
        id: response.userId.toString(),
        email: response.email,
        name: response.name,
        role: response.role,
      })

      toast.success('Login successful!')

      // Longer delay to ensure auth state is updated and persisted
      await new Promise(resolve => setTimeout(resolve, 500))

      // Redirect based on role using replace to avoid back button issues
      if (response.role === 'STUDENT') {
        router.replace('/student/profile')
      } else if (response.role === 'TEACHER') {
        router.replace('/teacher/profile')
      } else if (response.role === 'ADMIN') {
        router.replace('/admin')
      }
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-indigo-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      {/* Theme Toggle Button */}
      <div className="absolute top-6 right-6">
        <Button
          variant="ghost"
          size="icon"
          onClick={toggleTheme}
          className="text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full"
        >
          {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
        </Button>
      </div>

      <div className="max-w-md w-full space-y-8">
        {/* Logo and Brand Section */}
        <div className="text-center">
          <div className="flex justify-center mb-6">
            <div className="relative">
              <Image
                src="/ankurshala.svg"
                alt="Ankurshala Logo"
                width={120}
                height={120}
                className="rounded-2xl shadow-lg"
              />
              <div className="absolute -inset-1 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl blur opacity-20"></div>
            </div>
          </div>
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
            AnkurShala
          </h1>
          <p className="mt-3 text-lg text-gray-600 dark:text-gray-300">
            Welcome back! Sign in to continue your learning journey
          </p>
        </div>
        
        {/* Login Card */}
        <Card className="backdrop-blur-sm bg-white/80 dark:bg-gray-800/80 border-0 shadow-2xl">
          <CardHeader className="space-y-1 pb-6">
            <CardTitle className="text-2xl font-bold text-center text-gray-900 dark:text-white">
              Sign In
            </CardTitle>
            <CardDescription className="text-center text-gray-600 dark:text-gray-400">
              Enter your credentials to access your account
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {/* Email Field */}
              <div className="space-y-2">
                <label htmlFor="email" className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Email Address
                </label>
                <input
                  {...register('email')}
                  type="email"
                  className={`w-full px-4 py-3 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-blue-500/20 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 ${
                    errors.email 
                      ? 'border-red-500 focus:border-red-500' 
                      : 'border-gray-200 dark:border-gray-600 focus:border-blue-500'
                  }`}
                  placeholder="Enter your email address"
                />
                {errors.email && (
                  <p className="text-sm text-red-600 dark:text-red-400">{errors.email.message}</p>
                )}
              </div>
              
              {/* Password Field */}
              <div className="space-y-2">
                <label htmlFor="password" className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Password
                </label>
                <div className="relative">
                  <input
                    {...register('password')}
                    type={showPassword ? 'text' : 'password'}
                    className={`w-full px-4 py-3 pr-12 rounded-lg border-2 transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-blue-500/20 bg-white dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 ${
                      errors.password 
                        ? 'border-red-500 focus:border-red-500' 
                        : 'border-gray-200 dark:border-gray-600 focus:border-blue-500'
                    }`}
                    placeholder="Enter your password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 transition-colors"
                  >
                    {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                  </button>
                </div>
                {errors.password && (
                  <p className="text-sm text-red-600 dark:text-red-400">{errors.password.message}</p>
                )}
              </div>
              
              {/* Submit Button */}
              <Button
                type="submit"
                disabled={loading}
                className="w-full py-3 text-lg font-semibold bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white rounded-lg transition-all duration-200 transform hover:scale-[1.02] disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
              >
                {loading ? (
                  <div className="flex items-center justify-center space-x-2">
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    <span>Signing in...</span>
                  </div>
                ) : (
                  'Sign In'
                )}
              </Button>
            </form>
            
            {/* Registration Links */}
            <div className="mt-8 text-center space-y-4">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-200 dark:border-gray-600"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-4 bg-white dark:bg-gray-800 text-gray-500 dark:text-gray-400">
                    New to AnkurShala?
                  </span>
                </div>
              </div>
              
              <div className="flex flex-col sm:flex-row gap-3 justify-center">
                <a 
                  href="/register-student" 
                  className="inline-flex items-center justify-center px-6 py-3 border-2 border-blue-600 text-blue-600 dark:text-blue-400 rounded-lg font-medium hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all duration-200"
                >
                  Join as Student
                </a>
                <a 
                  href="/register-teacher" 
                  className="inline-flex items-center justify-center px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-lg font-medium hover:from-indigo-700 hover:to-purple-700 transition-all duration-200"
                >
                  Join as Teacher
                </a>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Footer */}
        <div className="text-center">
          <p className="text-sm text-gray-500 dark:text-gray-400">
            © 2024 AnkurShala. Empowering education through technology.
          </p>
        </div>
      </div>
    </div>
  )
}
