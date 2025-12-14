'use client';

import { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { usePathname } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { 
  BookOpen, 
  Calendar, 
  Home, 
  Clock, 
  Bell, 
  User, 
  DollarSign,
  BarChart3,
  Menu,
  X,
  LogOut,
  Search,
  Moon,
  Sun,
  HelpCircle,
  Settings,
} from 'lucide-react';
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'next/navigation';
import { TeacherRoute } from '@/components/route-guard';

const teacherNavItems = [
  { href: '/teacher/dashboard', label: 'Dashboard', icon: Home },
  { href: '/teacher/bookings', label: 'Bookings', icon: BookOpen },
  { href: '/teacher/availability', label: 'Availability', icon: Calendar },
  { href: '/teacher/sessions', label: 'Sessions', icon: Clock },
  { href: '/teacher/earnings', label: 'Earnings', icon: DollarSign },
  { href: '/teacher/notifications', label: 'Notifications', icon: Bell },
  { href: '/teacher/profile', label: 'Profile', icon: User },
];

export default function TeacherLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(false);
  const pathname = usePathname();
  const { user, logout } = useAuthStore();
  const router = useRouter();

  const handleLogout = async () => {
    await logout();
    router.push('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <div className={`fixed inset-y-0 left-0 z-50 w-64 bg-gradient-to-b from-ankur-secondary to-ankur-secondary-dark shadow-2xl transform transition-transform duration-300 ease-in-out lg:translate-x-0 ${
        sidebarOpen ? 'translate-x-0' : '-translate-x-full'
      }`}>
        {/* Logo */}
        <div className="flex items-center h-20 px-6 border-b border-white/10">
          <Link href="/teacher/dashboard" className="flex items-center space-x-3">
            <Image 
              src="/ankurshala-logo-small.png" 
              alt="Ankurshala" 
              width={44} 
              height={44} 
              className="rounded-xl"
            />
            <div>
              <span className="text-xl font-bold text-white">Ankurshala</span>
              <p className="text-xs text-white/60">On Demand Learning</p>
            </div>
          </Link>
          <Button
            variant="ghost"
            size="sm"
            className="lg:hidden ml-auto text-white/70 hover:text-white hover:bg-white/10"
            onClick={() => setSidebarOpen(false)}
          >
            <X className="h-5 w-5" />
          </Button>
        </div>

        {/* Navigation */}
        <nav className="mt-6 px-3 flex-1 overflow-y-auto">
          <div className="space-y-1">
            {teacherNavItems.map((item) => {
              const Icon = item.icon;
              const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex items-center space-x-3 px-4 py-3 rounded-xl transition-all duration-200 ${
                    isActive
                      ? 'bg-ankur-primary text-white shadow-lg'
                      : 'text-white/70 hover:bg-white/10 hover:text-white'
                  }`}
                  onClick={() => setSidebarOpen(false)}
                >
                  <Icon className="h-5 w-5" />
                  <span className="font-medium">{item.label}</span>
                </Link>
              );
            })}
          </div>
        </nav>

        {/* User Section */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-white/10">
          <div className="flex items-center space-x-3 mb-3 p-3 bg-white/10 rounded-xl">
            <div className="h-10 w-10 bg-ankur-primary rounded-full flex items-center justify-center">
              <span className="text-white font-semibold text-sm">
                {user?.name?.charAt(0) || 'T'}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-white truncate">{user?.name || 'Teacher'}</p>
              <p className="text-xs text-white/60">Teacher</p>
            </div>
          </div>
          <Button
            variant="ghost"
            size="sm"
            className="w-full justify-start text-white/70 hover:text-red-400 hover:bg-white/10"
            onClick={handleLogout}
          >
            <LogOut className="h-4 w-4 mr-2" />
            Logout
          </Button>
        </div>
      </div>

      {/* Main content */}
      <div className="lg:ml-64">
        {/* Top bar */}
        <header className="bg-white border-b border-gray-100 sticky top-0 z-30 shadow-sm">
          <div className="flex items-center justify-between h-16 px-6">
            <div className="flex items-center space-x-4">
              <Button
                variant="ghost"
                size="sm"
                className="lg:hidden text-gray-700"
                onClick={() => setSidebarOpen(true)}
              >
                <Menu className="h-5 w-5" />
              </Button>
              <div>
                <p className="text-xs text-gray-500 uppercase font-semibold tracking-wide">
                  {teacherNavItems.find(item => pathname === item.href || pathname.startsWith(item.href + '/'))?.label || 'Dashboard'}
                </p>
                <h1 className="text-xl font-bold text-gray-900">
                  Welcome back, <span className="text-ankur-primary">{user?.name?.split(' ')[0] || 'Teacher'}</span>
                </h1>
              </div>
            </div>
            
            <div className="flex items-center space-x-2">
              {/* Search */}
              <div className="hidden md:flex items-center bg-gray-100 rounded-xl px-4 py-2 w-64">
                <Search className="h-4 w-4 text-gray-400 mr-2" />
                <input
                  type="text"
                  placeholder="Search..."
                  className="bg-transparent border-none outline-none text-sm text-gray-700 w-full placeholder-gray-400"
                />
              </div>

              {/* Help */}
              <Button variant="ghost" size="sm" className="text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-xl">
                <HelpCircle className="h-5 w-5" />
              </Button>

              {/* Dark mode toggle */}
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsDarkMode(!isDarkMode)}
                className="text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-xl"
              >
                {isDarkMode ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
              </Button>

              {/* Notifications */}
              <Button variant="ghost" size="sm" className="relative text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-xl">
                <Bell className="h-5 w-5" />
                <span className="absolute -top-0.5 -right-0.5 h-4 w-4 bg-red-500 rounded-full text-[10px] text-white flex items-center justify-center font-bold">2</span>
              </Button>

              {/* User Avatar */}
              <div className="flex items-center gap-3 ml-2 pl-4 border-l border-gray-200">
                <div className="h-9 w-9 bg-ankur-primary rounded-full flex items-center justify-center">
                  <span className="text-white font-semibold text-sm">
                    {user?.name?.charAt(0) || 'T'}
                  </span>
                </div>
                <div className="hidden md:block">
                  <p className="text-sm font-semibold text-gray-900">{user?.name || 'Teacher'}</p>
                  <p className="text-xs text-gray-500">Teacher</p>
                </div>
              </div>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="p-6">
          <TeacherRoute>
            {children}
          </TeacherRoute>
        </main>
      </div>
    </div>
  );
}
