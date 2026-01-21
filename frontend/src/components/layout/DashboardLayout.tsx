'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/auth';
import { useTheme } from '@/components/theme-provider';
import { NotificationBell } from '@/components/ui/notification-bell';
import { studentAPI } from '@/lib/apiClient';
import {
  LayoutDashboard,
  Users,
  GraduationCap,
  BookOpen,
  Calendar,
  CreditCard,
  Bell,
  LogOut,
  Menu,
  X,
  ChevronDown,
  Search,
  User,
  Moon,
  Sun,
  HelpCircle,
  FileText,
  BarChart3,
  DollarSign,
  Sparkles,
} from 'lucide-react';

interface NavItem {
  label: string;
  href: string;
  icon: React.ReactNode;
  badge?: number;
  children?: NavItem[];
}

interface DashboardLayoutProps {
  children: React.ReactNode;
  role: 'admin' | 'teacher' | 'student';
}

const adminNavItems: NavItem[] = [
  { label: 'Dashboard', href: '/admin/dashboard', icon: <LayoutDashboard size={20} /> },
  { label: 'Students', href: '/admin/users/students', icon: <GraduationCap size={20} /> },
  { label: 'Teachers', href: '/admin/users/teachers', icon: <Users size={20} /> },
  { label: 'Content', href: '/admin/content/manage', icon: <BookOpen size={20} /> },
  { label: 'Content Import', href: '/admin/content/import', icon: <FileText size={20} /> },
  { label: 'Analytics', href: '/admin/content/analytics', icon: <BarChart3 size={20} /> },
  { label: 'Pricing', href: '/admin/pricing', icon: <DollarSign size={20} /> },
  { label: 'Fee Waivers', href: '/admin/fees', icon: <CreditCard size={20} /> },
  { label: 'Notifications', href: '/admin/notifications', icon: <Bell size={20} /> },
  { label: 'Profile', href: '/admin/profile', icon: <User size={20} /> },
];

const teacherNavItems: NavItem[] = [
  { label: 'Dashboard', href: '/teacher/dashboard', icon: <LayoutDashboard size={20} /> },
  { label: 'My Students', href: '/teacher/students', icon: <GraduationCap size={20} /> },
  { label: 'Schedule', href: '/teacher/schedule', icon: <Calendar size={20} /> },
  { label: 'Subjects', href: '/teacher/subjects', icon: <BookOpen size={20} /> },
  { label: 'Earnings', href: '/teacher/earnings', icon: <CreditCard size={20} /> },
  { label: 'Notifications', href: '/teacher/notifications', icon: <Bell size={20} /> },
  { label: 'Profile', href: '/teacher/profile', icon: <User size={20} /> },
];

const studentNavItems: NavItem[] = [
  { label: 'Dashboard', href: '/student/dashboard', icon: <LayoutDashboard size={20} /> },
  { label: 'Find Teachers', href: '/student/teachers', icon: <Users size={20} /> },
  { label: 'My Bookings', href: '/student/bookings', icon: <Calendar size={20} /> },
  { label: 'My Subjects', href: '/student/subjects', icon: <BookOpen size={20} /> },
  { label: 'AI Companion', href: '/student/ai-tutor', icon: <Sparkles size={20} /> },
  { label: 'Payments', href: '/student/payments', icon: <CreditCard size={20} /> },
  { label: 'Notifications', href: '/student/notifications', icon: <Bell size={20} /> },
  { label: 'Profile', href: '/student/profile', icon: <User size={20} /> },
];

export default function DashboardLayout({ children, role }: DashboardLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const [studentNotificationCount, setStudentNotificationCount] = useState<number | null>(null);
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuthStore();
  const { theme, setTheme } = useTheme();

  const baseNavItems = role === 'admin' ? adminNavItems : role === 'teacher' ? teacherNavItems : studentNavItems;
  const navItems = role === 'student'
    ? baseNavItems.map((item) => {
        if (item.label !== 'Notifications') return item;
        const badge = studentNotificationCount && studentNotificationCount > 0 ? studentNotificationCount : undefined;
        return { ...item, badge };
      })
    : baseNavItems;

  const isActiveRoute = (href: string) => {
    return pathname === href || pathname.startsWith(href + '/');
  };

  const currentNavLabel = navItems.find((item) => isActiveRoute(item.href))?.label || 'Dashboard';
  const workspaceLabel = role === 'admin' ? 'Admin Workspace' : role === 'teacher' ? 'Teacher Workspace' : 'Student Workspace';

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  // Close mobile menu on route change
  useEffect(() => {
    setMobileMenuOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (role !== 'student') return;
    let mounted = true;

    const fetchNotificationCount = async () => {
      try {
        const count = await studentAPI.getUnreadNotifications();
        if (mounted) {
          setStudentNotificationCount(typeof count === 'number' ? count : 0);
        }
      } catch (error) {
        console.error('Failed to load student notifications count:', error);
        if (mounted) {
          setStudentNotificationCount(0);
        }
      }
    };

    fetchNotificationCount();
    const interval = setInterval(fetchNotificationCount, 30000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, [role]);

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark');
  };

  return (
    <div className="min-h-screen bg-transparent flex text-foreground">
      {/* Sidebar - Desktop */}
      <aside
        className={`fixed top-0 left-0 z-40 h-screen transition-all duration-300 ${
          sidebarOpen ? 'w-60' : 'w-20'
        } hidden lg:block`}
      >
        <div className="h-full bg-gradient-to-b from-slate-950/90 via-slate-950/95 to-slate-900/95 backdrop-blur-xl border-r border-white/10 flex flex-col">
          {/* Logo */}
          <div className="h-16 px-5 flex items-center border-b border-white/10">
            <Link href={`/${role}/dashboard`} className="flex items-center gap-3">
              <Image 
                src="/ankurshala-logo-small.png" 
                alt="Ankurshala" 
                width={36} 
                height={36} 
                className="rounded-lg"
              />
              {sidebarOpen && (
                <span className="text-white font-display font-semibold text-lg tracking-tight">AnkurShala</span>
              )}
            </Link>
          </div>

          {/* Navigation */}
          <nav className="flex-1 py-4 pb-24 overflow-y-auto custom-scrollbar">
            <ul className="space-y-1 px-4">
              {navItems.map((item) => (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    className={`relative flex items-center gap-3 px-3 py-2 rounded-xl text-sm transition-all duration-200 group ${
                      isActiveRoute(item.href)
                        ? 'bg-white/15 text-white shadow-lg shadow-emerald-500/10 border border-white/20'
                        : 'text-white/70 hover:bg-white/10 hover:text-white'
                    }`}
                  >
                    <span className={`absolute left-0 top-1/2 -translate-y-1/2 h-6 w-1 rounded-r-full ${isActiveRoute(item.href) ? 'bg-ankur-accent' : 'bg-transparent'}`} />
                    <span className="flex-shrink-0">{item.icon}</span>
                    {sidebarOpen && (
                      <>
                        <span className="font-medium">{item.label}</span>
                        {item.badge && (
                          <span className="ml-auto bg-ankur-accent text-ankur-secondary-dark text-xs font-bold px-2 py-0.5 rounded-full">
                            {item.badge}
                          </span>
                        )}
                      </>
                    )}
                    {!sidebarOpen && item.badge && (
                      <span className="absolute -top-1 -right-1 w-5 h-5 bg-ankur-accent text-ankur-secondary-dark text-xs font-bold rounded-full flex items-center justify-center">
                        {item.badge}
                      </span>
                    )}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>

          {/* User Section */}
          <div className="p-4 border-t border-white/10">
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 w-full px-4 py-3 rounded-2xl text-white/70 hover:bg-white/10 hover:text-white transition-all duration-200"
            >
              <LogOut size={20} />
              {sidebarOpen && <span className="font-medium">Sign Out</span>}
            </button>
          </div>

          {/* Collapse Toggle */}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="absolute -right-3 top-16 w-7 h-7 bg-slate-900 border-2 border-white/20 rounded-full flex items-center justify-center text-white hover:bg-ankur-primary transition-colors"
          >
            <ChevronDown className={`w-4 h-4 transition-transform ${sidebarOpen ? 'rotate-90' : '-rotate-90'}`} />
          </button>
        </div>
      </aside>

      {/* Mobile Sidebar Overlay */}
      {mobileMenuOpen && (
        <div
          className="fixed inset-0 bg-black/50 backdrop-blur-sm z-40 lg:hidden"
          onClick={() => setMobileMenuOpen(false)}
        />
      )}

      {/* Mobile Sidebar */}
      <aside
        className={`fixed top-0 left-0 z-50 h-screen w-60 transform transition-transform duration-300 lg:hidden ${
          mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="h-full bg-gradient-to-b from-slate-950/95 via-slate-950/95 to-slate-900/95 backdrop-blur-xl border-r border-white/10 flex flex-col">
          {/* Header */}
          <div className="h-16 px-5 border-b border-white/10 flex items-center justify-between">
            <Link href={`/${role}/dashboard`} className="flex items-center gap-3">
              <Image 
                src="/ankurshala-logo-small.png" 
                alt="Ankurshala" 
                width={36} 
                height={36} 
                className="rounded-lg"
              />
              <span className="text-white font-display font-semibold text-lg">AnkurShala</span>
            </Link>
            <button
              onClick={() => setMobileMenuOpen(false)}
              className="text-white/70 hover:text-white"
            >
              <X size={24} />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 py-4 pb-24 overflow-y-auto">
            <ul className="space-y-1 px-4">
              {navItems.map((item) => (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    className={`flex items-center gap-3 px-3 py-2 rounded-xl text-sm transition-all duration-200 ${
                      isActiveRoute(item.href)
                        ? 'bg-white/15 text-white shadow-lg shadow-emerald-500/10 border border-white/20'
                        : 'text-white/70 hover:bg-white/10 hover:text-white'
                    }`}
                  >
                    <span>{item.icon}</span>
                    <span className="font-medium">{item.label}</span>
                    {item.badge && (
                      <span className="ml-auto bg-ankur-accent text-ankur-secondary-dark text-xs font-bold px-2 py-0.5 rounded-full">
                        {item.badge}
                      </span>
                    )}
                  </Link>
                </li>
              ))}
            </ul>
          </nav>

          {/* Logout */}
          <div className="p-4 border-t border-white/10">
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 w-full px-4 py-3 rounded-2xl text-white/70 hover:bg-white/10 hover:text-white transition-all duration-200"
            >
              <LogOut size={20} />
              <span className="font-medium">Sign Out</span>
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <div className={`flex-1 transition-all duration-300 ${sidebarOpen ? 'lg:ml-60' : 'lg:ml-20'}`}>
        {/* Top Header */}
        <header className="sticky top-0 z-30 bg-white/80 backdrop-blur-2xl border-b border-white/50 dark:bg-slate-900/70 dark:border-white/10">
          <div className="px-4 md:px-6 py-3 flex items-center justify-between gap-4">
            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(true)}
              className="lg:hidden p-2 hover:bg-gray-100 rounded-lg"
            >
              <Menu size={24} className="text-gray-700 dark:text-gray-200" />
            </button>

            {/* Mobile Title */}
            <div className="md:hidden min-w-0">
              <p className="text-[9px] uppercase tracking-widest text-gray-500 dark:text-gray-400 font-semibold">
                {workspaceLabel}
              </p>
              <p className="text-sm font-display font-semibold text-gray-900 dark:text-gray-100 truncate max-w-[160px]">
                {currentNavLabel}
              </p>
            </div>

            {/* Workspace Label */}
            <div className="hidden md:block min-w-[160px]">
              <p className="text-[10px] uppercase tracking-widest text-gray-500 dark:text-gray-400 font-semibold">
                {workspaceLabel}
              </p>
              <p className="text-base font-display font-semibold text-gray-900 dark:text-gray-100">
                {currentNavLabel}
              </p>
            </div>

            {/* Search Bar */}
            <div className="flex-1 max-w-md hidden md:block">
              <div className="relative">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search..."
                  className="w-full pl-12 pr-4 py-2 bg-white/70 border border-white/50 rounded-2xl focus:bg-white focus:ring-2 focus:ring-ankur-primary/20 transition-all duration-200 backdrop-blur shadow-sm dark:bg-slate-900/60 dark:border-white/10"
                />
              </div>
            </div>

            {/* Right Section */}
            <div className="flex items-center gap-3">
              {/* Help */}
              <button className="p-2 hover:bg-white/60 rounded-xl text-gray-700 dark:text-gray-200 hidden sm:block">
                <HelpCircle size={22} />
              </button>

              {/* Theme Toggle */}
              <button
                onClick={toggleTheme}
                className="p-2 hover:bg-white/60 rounded-xl text-gray-700 dark:text-gray-200 hidden sm:block"
              >
                {theme === 'dark' ? <Sun size={22} /> : <Moon size={22} />}
              </button>

              {/* Notifications */}
              {role === 'student' ? (
                <NotificationBell />
              ) : (
                <Link
                  href={`/${role}/notifications`}
                  className="relative p-2 hover:bg-white/60 rounded-xl text-gray-700 dark:text-gray-200"
                  aria-label="Notifications"
                >
                  <Bell size={22} />
                </Link>
              )}

              {/* User Menu */}
              <div className="relative">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center gap-3 p-2 hover:bg-gray-100 rounded-xl transition-colors"
                >
                <div className="w-8 h-8 rounded-full bg-gradient-to-br from-ankur-primary to-ankur-primary-dark flex items-center justify-center text-white font-semibold shadow-sm">
                  {user?.name?.[0]?.toUpperCase() || 'U'}
                </div>
                <div className="hidden sm:block text-left">
                  <p className="text-sm font-medium text-gray-900 dark:text-gray-100">
                      {user?.name}
                    </p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 capitalize">{role}</p>
                  </div>
                  <ChevronDown
                    size={16}
                    className={`text-gray-400 hidden sm:block transition-transform ${
                      userMenuOpen ? 'rotate-180' : ''
                    }`}
                  />
                </button>

                {/* User Dropdown */}
                {userMenuOpen && (
                  <>
                    <div
                      className="fixed inset-0 z-40"
                      onClick={() => setUserMenuOpen(false)}
                    />
                    <div className="absolute right-0 top-full mt-2 w-56 bg-white/90 rounded-2xl shadow-lg border border-white/60 py-2 z-50 animate-fade-in backdrop-blur dark:bg-slate-900/80 dark:border-white/10">
                      <Link
                        href={`/${role}/profile`}
                        className="flex items-center gap-3 px-4 py-2.5 text-gray-700 hover:bg-white/70 dark:text-gray-200 dark:hover:bg-slate-800/60"
                        onClick={() => setUserMenuOpen(false)}
                      >
                        <User size={18} />
                        <span>My Profile</span>
                      </Link>
                      <hr className="my-2 border-gray-100/60 dark:border-white/10" />
                      <button
                        onClick={() => {
                          setUserMenuOpen(false);
                          handleLogout();
                        }}
                        className="flex items-center gap-3 px-4 py-2.5 text-red-600 hover:bg-red-50/70 w-full dark:hover:bg-red-500/10"
                      >
                        <LogOut size={18} />
                        <span>Sign Out</span>
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="p-6 min-h-[calc(100vh-73px)]">
          {children}
        </main>
      </div>
    </div>
  );
}
