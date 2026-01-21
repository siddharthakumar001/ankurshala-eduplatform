'use client'

import { useRouter, usePathname } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { 
  LayoutDashboard, 
  Calendar, 
  BookOpen, 
  Play, 
  User, 
  LogOut,
  Bell,
  CreditCard,
  Sparkles,
  Moon,
  Sun
} from 'lucide-react'
import { useAuthStore } from '@/store/auth'
import { useState, useEffect } from 'react'
import { api } from '@/utils/api'
import { useTheme } from '@/components/theme-provider'

const studentNavigation = [
  {
    name: 'Dashboard',
    href: '/student/dashboard',
    icon: LayoutDashboard,
    description: 'Overview and stats'
  },
  {
    name: 'Profile',
    href: '/student/profile',
    icon: User,
    description: 'Manage profile'
  },
  {
    name: 'Book Class',
    href: '/student/booking',
    icon: BookOpen,
    description: 'Book sessions'
  },
  {
    name: 'Calendar',
    href: '/student/calendar',
    icon: Calendar,
    description: 'View schedule'
  },
  {
    name: 'History',
    href: '/student/history',
    icon: Play,
    description: 'Session history'
  },
  {
    name: 'AI Companion',
    href: '/student/ai-tutor',
    icon: Sparkles,
    description: 'Study with AI support'
  },
  {
    name: 'Payments',
    href: '/student/payments',
    icon: CreditCard,
    description: 'Billing & payments'
  }
]

export default function StudentNavigation() {
  const router = useRouter()
  const pathname = usePathname()
  const { logout } = useAuthStore()
  const [unreadNotificationCount, setUnreadNotificationCount] = useState(0)
  const { theme, setTheme } = useTheme()

  useEffect(() => {
    fetchUnreadNotificationCount()
  }, [])

  const fetchUnreadNotificationCount = async () => {
    try {
      const response = await api.get('/student/notifications/unread-count')
      setUnreadNotificationCount((response.data as number) || 0)
    } catch (error) {
      console.error('Error fetching unread notification count:', error)
    }
  }

  const handleLogout = async () => {
    await logout()
    router.push('/')
  }

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark')
  }

  return (
    <nav className="bg-white/70 backdrop-blur-2xl border-b border-white/40 shadow-sm dark:bg-slate-900/70 dark:border-white/10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center space-x-8">
            <div className="flex items-center space-x-3">
              <img 
                alt="AnkurShala Logo" 
                width="32" 
                height="32" 
                className="rounded-lg" 
                src="/ankurshala-logo-small.png"
              />
              <span className="text-xl font-display font-semibold text-gray-900 dark:text-gray-100">AnkurShala</span>
            </div>
            
            <div className="hidden md:flex space-x-1">
              {studentNavigation.map((item) => {
                const Icon = item.icon
                const isActive = pathname === item.href
                
                return (
                  <Button
                    key={item.name}
                    variant={isActive ? "default" : "ghost"}
                    onClick={() => router.push(item.href)}
                    className={`flex items-center gap-2 ${
                      isActive 
                        ? 'bg-ankur-primary text-white shadow-sm' 
                        : 'text-gray-600 hover:text-gray-900 hover:bg-white/70 dark:text-gray-200 dark:hover:bg-slate-800/60'
                    }`}
                  >
                    <Icon className="h-4 w-4" />
                    <span>{item.name}</span>
                  </Button>
                )
              })}
            </div>
          </div>
          
          <div className="flex items-center space-x-3">
            <Button
              variant="outline"
              onClick={() => router.push('/student/notifications')}
              className="text-gray-700 hover:text-gray-900 relative dark:text-gray-200"
            >
              <Bell className="h-4 w-4 mr-2" />
              Notifications
              {unreadNotificationCount > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                  {unreadNotificationCount}
                </span>
              )}
            </Button>
            <Button
              variant="ghost"
              onClick={toggleTheme}
              className="text-gray-600 hover:text-gray-900 hover:bg-white/70 dark:text-gray-200 dark:hover:bg-slate-800/60"
            >
              {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
            </Button>
            <Button
              variant="outline"
              onClick={handleLogout}
              className="text-gray-700 hover:text-gray-900 dark:text-gray-200"
            >
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          </div>
        </div>
      </div>
    </nav>
  )
}
