import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { userAPI } from '@/lib/apiClient'

interface User {
  id: string
  email: string
  name: string
  role: string
}

interface AuthState {
  user: User | null
  isAuthenticated: boolean
  login: (user: User) => void
  logout: () => void
  initializeAuth: () => Promise<void>
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      login: (user) => set({ user, isAuthenticated: true }),
      logout: () => {
        if (typeof window !== 'undefined') {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
        }
        set({ user: null, isAuthenticated: false })
      },
      initializeAuth: async () => {
        if (typeof window !== 'undefined') {
          const accessToken = localStorage.getItem('accessToken')
          const refreshToken = localStorage.getItem('refreshToken')
          
          if (accessToken && refreshToken) {
            try {
              // Try to fetch current user info to verify token is still valid
              const userInfo = await userAPI.getCurrentUser()
              set({ 
                user: {
                  id: userInfo.id.toString(),
                  email: userInfo.email,
                  name: userInfo.name,
                  role: userInfo.role
                }, 
                isAuthenticated: true 
              })
            } catch (error) {
              // Token is invalid, clear it and set unauthenticated
              localStorage.removeItem('accessToken')
              localStorage.removeItem('refreshToken')
              set({ user: null, isAuthenticated: false })
            }
          }
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
    }
  )
)
