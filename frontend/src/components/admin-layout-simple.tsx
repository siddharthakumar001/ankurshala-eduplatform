'use client'

import { useState } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuthStore } from '@/store/auth'
import { authAPI } from '@/lib/apiClient'
import { Button } from '@/components/ui/button'
import Link from 'next/link'
import { useTheme } from '@/components/theme-provider'
import { 
  LayoutDashboard, 
  Users, 
  GraduationCap, 
  FileText, 
  BarChart3, 
  DollarSign, 
  Bell, 
  Settings,
  LogOut,
  Menu,
  X,
  Sun,
  Moon
} from 'lucide-react'

interface AdminLayoutProps {
  children: React.ReactNode
}

export default function AdminLayoutSimple({ children }: AdminLayoutProps) {
  const router = useRouter()
  const pathname = usePathname()
  const { logout } = useAuthStore()
  const { theme, setTheme } = useTheme()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark')
  }

  const handleLogout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        // Call logout API to invalidate refresh token
        await authAPI.logout(refreshToken)
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      logout() // Clear Zustand store
      router.push('/')
    }
  }

  const navItems = [
    { name: 'Dashboard', href: '/admin', icon: LayoutDashboard },
    { name: 'Students', href: '/admin/users/students', icon: Users },
    { name: 'Teachers', href: '/admin/users/teachers', icon: GraduationCap },
    { name: 'Content Import', href: '/admin/content/import', icon: FileText },
    { name: 'Content Browse', href: '/admin/content/browse', icon: FileText },
    { name: 'Analytics', href: '/admin/content/analytics', icon: BarChart3 },
    { name: 'Pricing', href: '/admin/pricing', icon: DollarSign },
    { name: 'Notifications', href: '/admin/notifications', icon: Bell },
    { name: 'Fee Waivers', href: '/admin/fees', icon: DollarSign },
  ]

  return (
    <div className="flex min-h-screen">
      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-50 w-64 bg-gray-800 text-white transform ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        } md:relative md:translate-x-0 transition-transform duration-200 ease-in-out`}
      >
        <div className="flex items-center justify-between p-4 border-b border-gray-700">
          <Link href="/admin" className="flex items-center gap-2">
            <div className="w-8 h-8 bg-blue-600 rounded-md flex items-center justify-center">
              <span className="text-white font-bold text-sm">A</span>
            </div>
            <span className="text-xl font-semibold">Admin Panel</span>
          </Link>
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden text-gray-400 hover:text-white"
            onClick={() => setSidebarOpen(false)}
          >
            <X className="h-6 w-6" />
          </Button>
        </div>
        <nav className="mt-5">
          {navItems.map((item) => (
            <Link
              key={item.name}
              href={item.href}
              className={`flex items-center gap-3 px-4 py-2 text-sm font-medium ${
                pathname === item.href
                  ? 'bg-blue-600 text-white'
                  : 'text-gray-300 hover:bg-gray-700 hover:text-white'
              }`}
              onClick={() => setSidebarOpen(false)}
            >
              <item.icon className="h-5 w-5" />
              {item.name}
            </Link>
          ))}
        </nav>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col md:ml-64">
        {/* Topbar */}
        <header className="sticky top-0 z-40 flex h-16 items-center gap-4 border-b bg-white px-4 md:px-6 shadow-sm dark:bg-gray-900 dark:text-white">
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden text-gray-600 dark:text-gray-300"
            onClick={() => setSidebarOpen(true)}
          >
            <Menu className="h-6 w-6" />
          </Button>
          <div className="flex-1">
            <h2 className="text-lg font-semibold">
              {navItems.find(item => pathname.startsWith(item.href))?.name || 'Admin Dashboard'}
            </h2>
          </div>
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="icon"
              onClick={toggleTheme}
              className="text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800"
            >
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 p-4 md:p-6 bg-gray-100 dark:bg-gray-800">
          {children}
        </main>
      </div>
    </div>
  )
}
