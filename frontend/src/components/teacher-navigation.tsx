'use client'

import { useRouter, usePathname } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { 
  LayoutDashboard, 
  Calendar, 
  BookOpen, 
  Play, 
  User, 
  Settings,
  LogOut
} from 'lucide-react'
import { useAuthStore } from '@/store/auth'

const teacherNavigation = [
  {
    name: 'Dashboard',
    href: '/teacher/dashboard',
    icon: LayoutDashboard,
    description: 'Overview and stats'
  },
  {
    name: 'Profile',
    href: '/teacher/profile',
    icon: User,
    description: 'Manage profile'
  },
  {
    name: 'Availability',
    href: '/teacher/availability',
    icon: Calendar,
    description: 'Set schedule'
  },
  {
    name: 'Bookings',
    href: '/teacher/bookings',
    icon: BookOpen,
    description: 'Manage requests'
  },
  {
    name: 'Sessions',
    href: '/teacher/sessions',
    icon: Play,
    description: 'Start/end sessions'
  }
]

export default function TeacherNavigation() {
  const router = useRouter()
  const pathname = usePathname()
  const { logout } = useAuthStore()

  const handleLogout = async () => {
    await logout()
    router.push('/')
  }

  return (
    <nav className="bg-white shadow-sm border-b">
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
              <span className="text-xl font-bold text-gray-900">AnkurShala</span>
            </div>
            
            <div className="hidden md:flex space-x-1">
              {teacherNavigation.map((item) => {
                const Icon = item.icon
                const isActive = pathname === item.href
                
                return (
                  <Button
                    key={item.name}
                    variant={isActive ? "default" : "ghost"}
                    onClick={() => router.push(item.href)}
                    className={`flex items-center gap-2 ${
                      isActive 
                        ? 'bg-blue-600 text-white' 
                        : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                    }`}
                  >
                    <Icon className="h-4 w-4" />
                    <span>{item.name}</span>
                  </Button>
                )
              })}
            </div>
          </div>
          
          <div className="flex items-center space-x-4">
            <Button
              variant="outline"
              onClick={handleLogout}
              className="text-gray-600 hover:text-gray-900"
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
