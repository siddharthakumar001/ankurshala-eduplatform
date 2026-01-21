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
  Menu,
  X,
  LogOut,
  Search,
  Moon,
  Sun,
  HelpCircle,
} from 'lucide-react';
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'next/navigation';
import { TeacherRoute } from '@/components/route-guard';
import { useTheme } from '@/components/theme-provider';

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
  const pathname = usePathname();
  const { user, logout } = useAuthStore();
  const router = useRouter();
  const { theme, setTheme } = useTheme();

  const handleLogout = async () => {
    await logout();
    router.push('/login');
  };

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark');
  };

  return (
    <div className="min-h-screen bg-transparent text-foreground">
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <div className={`fixed inset-y-0 left-0 z-50 w-64 bg-gradient-to-b from-slate-950/95 via-slate-950/95 to-slate-900/95 shadow-2xl backdrop-blur-xl border-r border-white/10 transform transition-transform duration-300 ease-in-out lg:translate-x-0 ${
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
              <span className="text-xl font-display font-semibold text-white">Ankurshala</span>
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
                  className={`flex items-center space-x-3 px-4 py-3 rounded-2xl transition-all duration-200 border ${
                    isActive
                      ? 'bg-white/15 text-white border-white/20 shadow-lg shadow-emerald-500/10'
                      : 'text-white/70 border-transparent hover:bg-white/10 hover:text-white'
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
          <div className="flex items-center space-x-3 mb-3 p-3 bg-white/10 rounded-2xl">
            <div className="h-10 w-10 bg-ankur-primary rounded-full flex items-center justify-center shadow-sm">
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
        <header className="sticky top-0 z-30 bg-white/70 backdrop-blur-2xl border-b border-white/40 shadow-sm dark:bg-slate-900/70 dark:border-white/10">
          <div className="flex items-center justify-between h-16 px-6">
            <div className="flex items-center space-x-4">
              <Button
                variant="ghost"
                size="sm"
                className="lg:hidden text-gray-700 dark:text-gray-200"
                onClick={() => setSidebarOpen(true)}
              >
                <Menu className="h-5 w-5" />
              </Button>
              <div>
                <p className="text-xs text-gray-500 uppercase font-semibold tracking-wide dark:text-gray-400">
                  {teacherNavItems.find(item => pathname === item.href || pathname.startsWith(item.href + '/'))?.label || 'Dashboard'}
                </p>
                <h1 className="text-xl font-display font-semibold text-gray-900 dark:text-gray-100">
                  Welcome back, <span className="text-ankur-primary">{user?.name?.split(' ')[0] || 'Teacher'}</span>
                </h1>
              </div>
            </div>
            
            <div className="flex items-center space-x-2">
              {/* Search */}
              <div className="hidden md:flex items-center bg-white/70 border border-white/50 rounded-2xl px-4 py-2 w-64 backdrop-blur dark:bg-slate-900/60 dark:border-white/10">
                <Search className="h-4 w-4 text-gray-400 mr-2" />
                <input
                  type="text"
                  placeholder="Search..."
                  className="bg-transparent border-none outline-none text-sm text-gray-700 w-full placeholder-gray-400 dark:text-gray-200"
                />
              </div>

              {/* Help */}
              <Button variant="ghost" size="sm" className="text-gray-600 hover:text-gray-900 hover:bg-white/70 rounded-xl dark:text-gray-200 dark:hover:bg-slate-800/60">
                <HelpCircle className="h-5 w-5" />
              </Button>

              {/* Dark mode toggle */}
              <Button
                variant="ghost"
                size="sm"
                onClick={toggleTheme}
                className="text-gray-600 hover:text-gray-900 hover:bg-white/70 rounded-xl dark:text-gray-200 dark:hover:bg-slate-800/60"
              >
                {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
              </Button>

              {/* Notifications */}
              <Button variant="ghost" size="sm" className="relative text-gray-600 hover:text-gray-900 hover:bg-white/70 rounded-xl dark:text-gray-200 dark:hover:bg-slate-800/60">
                <Bell className="h-5 w-5" />
                <span className="absolute -top-0.5 -right-0.5 h-4 w-4 bg-red-500 rounded-full text-[10px] text-white flex items-center justify-center font-bold">2</span>
              </Button>

              {/* User Avatar */}
              <div className="flex items-center gap-3 ml-2 pl-4 border-l border-gray-200/60 dark:border-white/10">
                <div className="h-9 w-9 bg-ankur-primary rounded-full flex items-center justify-center shadow-sm">
                  <span className="text-white font-semibold text-sm">
                    {user?.name?.charAt(0) || 'T'}
                  </span>
                </div>
                <div className="hidden md:block">
                  <p className="text-sm font-semibold text-gray-900 dark:text-gray-100">{user?.name || 'Teacher'}</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">Teacher</p>
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
