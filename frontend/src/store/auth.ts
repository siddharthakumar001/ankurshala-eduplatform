import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { api } from '@/utils/api'
import type { StudentSignupRequest, TeacherSignupRequest, AuthResponse } from '@/types/auth'

interface User {
  id: string
  email: string
  name: string
  role: string
}

interface AuthState {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  lastActivity: number
  login: (user: User) => void
  logout: () => Promise<void>
  signup: (role: 'student' | 'teacher', data: StudentSignupRequest | TeacherSignupRequest) => Promise<void>
  initializeAuth: () => Promise<void>
  updateActivity: () => void
  setLoading: (loading: boolean) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      lastActivity: Date.now(),
      
      login: (user) => {
        console.log('🔐 Auth Store: Setting user state:', user)
        set({ 
          user, 
          isAuthenticated: true, 
          lastActivity: Date.now(),
          isLoading: false 
        })
        console.log('✅ Auth Store: User state set successfully')
      },
      
      signup: async (role: 'student' | 'teacher', data: any) => {
        set({ isLoading: true })
        try {
          const endpoint = role === 'student' ? '/auth/signup/student' : '/auth/signup/teacher'
          const response = await api.post(endpoint, data, { requireAuth: false })
          
          // API client extracts 'data' from ApiResponse wrapper
          const authResponse = response.data as any
          
          // Extract user info and tokens from response
          if (authResponse.accessToken && authResponse.refreshToken && authResponse.userId) {
            // Store tokens in localStorage
            if (typeof window !== 'undefined') {
              localStorage.setItem('accessToken', authResponse.accessToken)
              localStorage.setItem('refreshToken', authResponse.refreshToken)
            }
            
            // Set user state
            const user: User = {
              id: authResponse.userId.toString(),
              email: authResponse.email,
              name: authResponse.name,
              role: authResponse.role // Backend returns Role enum as string
            }
            
            set({ 
              user, 
              isAuthenticated: true, 
              lastActivity: Date.now(),
              isLoading: false 
            })
          } else {
            throw new Error('Invalid response from server')
          }
        } catch (error: any) {
          set({ isLoading: false })
          throw error
        }
      },
      
      logout: async () => {
        set({ isLoading: true })
        try {
          // Call logout API to clear server-side session
          await api.post('/auth/logout', {}, { requireAuth: false })
        } catch (error) {
          console.warn('Logout API call failed:', error)
          // Continue with logout even if API call fails
        } finally {
          // Clear all state
          set({ 
            user: null, 
            isAuthenticated: false, 
            isLoading: false,
            lastActivity: Date.now()
          })
          
          // Clear localStorage
          if (typeof window !== 'undefined') {
            localStorage.removeItem('auth-storage')
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('user')
            localStorage.removeItem('lastActivity')
            
            // Force redirect to home page to clear any cached state
            window.location.href = '/'
          }
        }
      },
      
      initializeAuth: async () => {
        const currentState = get()
        
        // If we already have a valid user in state, don't re-initialize
        // This prevents clearing auth state immediately after login
        if (currentState.user && currentState.isAuthenticated && !currentState.isLoading) {
          console.log('✅ Auth Store: User already authenticated, skipping initialization')
          return
        }
        
        // Check if we have cookies before making API call
        const hasCookies = typeof document !== 'undefined' && 
          (document.cookie.includes('accessToken') || document.cookie.includes('refreshToken'))
        
        if (!hasCookies) {
          console.log('ℹ️ Auth Store: No cookies found, user not authenticated')
          set({ user: null, isAuthenticated: false, isLoading: false })
          return
        }
        
        set({ isLoading: true })
        try {
          console.log('🔄 Auth Store: Initializing auth from API...')
          // Try to fetch current user info to verify authentication
          const response = await api.get('/user/me', { requireAuth: true })
          const userInfo = response.data as any
          
          console.log('📥 Auth Store: Received user info:', userInfo)
          
          if (userInfo && userInfo.id && userInfo.email && userInfo.role) {
            set({ 
              user: {
                id: userInfo.id.toString(),
                email: userInfo.email,
                name: userInfo.name || '',
                role: userInfo.role
              }, 
              isAuthenticated: true,
              lastActivity: Date.now(),
              isLoading: false
            })
            console.log('✅ Auth Store: Auth initialized successfully')
          } else {
            console.warn('⚠️ Auth Store: Invalid user data received')
            // User data invalid - but keep state if cookies exist
            if (hasCookies) {
              console.log('✅ Auth Store: Cookies exist, keeping existing state')
              set({ isLoading: false })
              return
            }
            // Only clear if no cookies
            set({ user: null, isAuthenticated: false, isLoading: false })
            if (typeof window !== 'undefined') {
              localStorage.removeItem('auth-storage')
              localStorage.removeItem('accessToken')
              localStorage.removeItem('refreshToken')
              localStorage.removeItem('user')
              localStorage.removeItem('lastActivity')
            }
          }
        } catch (error: any) {
          // Always check cookies again in catch block
          const stillHasCookies = typeof document !== 'undefined' && 
            (document.cookie.includes('accessToken') || document.cookie.includes('refreshToken'))
          
          if (stillHasCookies) {
            console.warn('⚠️ Auth Store: API call failed but cookies exist, keeping state')
            // Don't clear state if we have cookies - might be a temporary issue
            // Keep existing state if we have one
            const currentState = get()
            if (currentState.user && currentState.isAuthenticated) {
              console.log('✅ Auth Store: Keeping existing authenticated state')
              set({ isLoading: false })
              return
            }
            // If no state but cookies exist, just set loading to false
            set({ isLoading: false })
            return
          }
          
          console.log('ℹ️ Auth Store: User not authenticated (no cookies found)')
          // User is not authenticated or token is invalid - this is normal for logged out users
          set({ user: null, isAuthenticated: false, isLoading: false })
          // Clear localStorage to remove any stale data
          if (typeof window !== 'undefined') {
            localStorage.removeItem('auth-storage')
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            localStorage.removeItem('user')
            localStorage.removeItem('lastActivity')
          }
        }
      },
      
      updateActivity: () => {
        set({ lastActivity: Date.now() })
      },
      
      setLoading: (loading: boolean) => {
        set({ isLoading: loading })
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ 
        user: state.user, 
        isAuthenticated: state.isAuthenticated,
        lastActivity: state.lastActivity
      }),
    }
  )
)
