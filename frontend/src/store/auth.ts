import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { api } from '@/utils/api'

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
        set({ 
          user, 
          isAuthenticated: true, 
          lastActivity: Date.now(),
          isLoading: false 
        })
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
        set({ isLoading: true })
        try {
          // Try to fetch current user info to verify authentication
          const response = await api.get('/user/me', { requireAuth: true })
          const userInfo = response.data as any
          
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
          } else {
            // User data invalid - clear any stale data
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
        } catch (error) {
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
          // Don't log error to avoid console noise
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
