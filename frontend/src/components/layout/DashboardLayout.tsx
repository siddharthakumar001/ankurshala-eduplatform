'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/auth';
import {
  LayoutDashboard,
  Users,
  GraduationCap,
  BookOpen,
  Calendar,
  CreditCard,
  Bell,
  Settings,
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
  { label: 'Import', href: '/admin/content/import', icon: <FileText size={20} /> },
  { label: 'Bookings', href: '/admin/bookings', icon: <Calendar size={20} /> },
  { label: 'Payments', href: '/admin/payments', icon: <CreditCard size={20} /> },
  { label: 'Pricing', href: '/admin/pricing', icon: <DollarSign size={20} /> },
  { label: 'Notifications', href: '/admin/notifications', icon: <Bell size={20} />, badge: 3 },
  { label: 'Settings', href: '/admin/settings', icon: <Settings size={20} /> },
];

const teacherNavItems: NavItem[] = [
  { label: 'Dashboard', href: '/teacher/dashboard', icon: <LayoutDashboard size={20} /> },
  { label: 'My Students', href: '/teacher/students', icon: <GraduationCap size={20} /> },
  { label: 'Schedule', href: '/teacher/schedule', icon: <Calendar size={20} /> },
  { label: 'Subjects', href: '/teacher/subjects', icon: <BookOpen size={20} /> },
  { label: 'Earnings', href: '/teacher/earnings', icon: <CreditCard size={20} /> },
  { label: 'Notifications', href: '/teacher/notifications', icon: <Bell size={20} />, badge: 2 },
  { label: 'Profile', href: '/teacher/profile', icon: <User size={20} /> },
];

const studentNavItems: NavItem[] = [
  { label: 'Dashboard', href: '/student/dashboard', icon: <LayoutDashboard size={20} /> },
  { label: 'Find Teachers', href: '/student/teachers', icon: <Users size={20} /> },
  { label: 'My Bookings', href: '/student/bookings', icon: <Calendar size={20} /> },
  { label: 'My Subjects', href: '/student/subjects', icon: <BookOpen size={20} /> },
  { label: 'Payments', href: '/student/payments', icon: <CreditCard size={20} /> },
  { label: 'Notifications', href: '/student/notifications', icon: <Bell size={20} />, badge: 5 },
  { label: 'Profile', href: '/student/profile', icon: <User size={20} /> },
];

export default function DashboardLayout({ children, role }: DashboardLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout } = useAuthStore();

  const navItems = role === 'admin' ? adminNavItems : role === 'teacher' ? teacherNavItems : studentNavItems;

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const isActiveRoute = (href: string) => {
    return pathname === href || pathname.startsWith(href + '/');
  };

  // Close mobile menu on route change
  useEffect(() => {
    setMobileMenuOpen(false);
  }, [pathname]);

  return (
    <div className="min-h-screen bg-surface flex">
      {/* Sidebar - Desktop */}
      <aside
        className={`fixed top-0 left-0 z-40 h-screen transition-all duration-300 ${
          sidebarOpen ? 'w-64' : 'w-20'
        } hidden lg:block`}
      >
        <div className="h-full bg-gradient-to-b from-ankur-secondary to-ankur-secondary-dark flex flex-col">
          {/* Logo */}
          <div className="p-6 border-b border-white/10">
            <Link href={`/${role}/dashboard`} className="flex items-center gap-3">
              <Image 
                src="/ankurshala-logo-small.png" 
                alt="Ankurshala" 
                width={40} 
                height={40} 
                className="rounded-xl"
              />
              {sidebarOpen && (
                <span className="text-white font-bold text-xl tracking-tight">AnkurShala</span>
              )}
            </Link>
          </div>

          {/* Navigation */}
          <nav className="flex-1 py-6 overflow-y-auto custom-scrollbar">
            <ul className="space-y-1 px-3">
              {navItems.map((item) => (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 group relative ${
                      isActiveRoute(item.href)
                        ? 'bg-white/15 text-white border-l-4 border-ankur-primary -ml-px'
                        : 'text-white/70 hover:bg-white/10 hover:text-white'
                    }`}
                  >
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
              className="flex items-center gap-3 w-full px-4 py-3 rounded-lg text-white/70 hover:bg-white/10 hover:text-white transition-all duration-200"
            >
              <LogOut size={20} />
              {sidebarOpen && <span className="font-medium">Sign Out</span>}
            </button>
          </div>

          {/* Collapse Toggle */}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="absolute -right-3 top-20 w-6 h-6 bg-ankur-secondary border-2 border-white/20 rounded-full flex items-center justify-center text-white hover:bg-ankur-primary transition-colors"
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
        className={`fixed top-0 left-0 z-50 h-screen w-64 transform transition-transform duration-300 lg:hidden ${
          mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="h-full bg-gradient-to-b from-ankur-secondary to-ankur-secondary-dark flex flex-col">
          {/* Header */}
          <div className="p-6 border-b border-white/10 flex items-center justify-between">
            <Link href={`/${role}/dashboard`} className="flex items-center gap-3">
              <Image 
                src="/ankurshala-logo-small.png" 
                alt="Ankurshala" 
                width={40} 
                height={40} 
                className="rounded-xl"
              />
              <span className="text-white font-bold text-xl">AnkurShala</span>
            </Link>
            <button
              onClick={() => setMobileMenuOpen(false)}
              className="text-white/70 hover:text-white"
            >
              <X size={24} />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 py-6 overflow-y-auto">
            <ul className="space-y-1 px-3">
              {navItems.map((item) => (
                <li key={item.href}>
                  <Link
                    href={item.href}
                    className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 ${
                      isActiveRoute(item.href)
                        ? 'bg-white/15 text-white border-l-4 border-ankur-primary -ml-px'
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
              className="flex items-center gap-3 w-full px-4 py-3 rounded-lg text-white/70 hover:bg-white/10 hover:text-white transition-all duration-200"
            >
              <LogOut size={20} />
              <span className="font-medium">Sign Out</span>
            </button>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <div className={`flex-1 transition-all duration-300 ${sidebarOpen ? 'lg:ml-64' : 'lg:ml-20'}`}>
        {/* Top Header */}
        <header className="sticky top-0 z-30 bg-white/80 backdrop-blur-lg border-b border-gray-100">
          <div className="px-6 py-4 flex items-center justify-between gap-4">
            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(true)}
              className="lg:hidden p-2 hover:bg-gray-100 rounded-lg"
            >
              <Menu size={24} className="text-gray-600" />
            </button>

            {/* Search Bar */}
            <div className="flex-1 max-w-xl hidden md:block">
              <div className="relative">
                <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search..."
                  className="w-full pl-12 pr-4 py-2.5 bg-gray-100 border-0 rounded-xl focus:bg-white focus:ring-2 focus:ring-ankur-primary/20 transition-all duration-200"
                />
              </div>
            </div>

            {/* Right Section */}
            <div className="flex items-center gap-3">
              {/* Help */}
              <button className="p-2 hover:bg-gray-100 rounded-lg text-gray-600 hidden sm:block">
                <HelpCircle size={22} />
              </button>

              {/* Theme Toggle */}
              <button
                onClick={() => setIsDarkMode(!isDarkMode)}
                className="p-2 hover:bg-gray-100 rounded-lg text-gray-600 hidden sm:block"
              >
                {isDarkMode ? <Sun size={22} /> : <Moon size={22} />}
              </button>

              {/* Notifications */}
              <button className="relative p-2 hover:bg-gray-100 rounded-lg text-gray-600">
                <Bell size={22} />
                <span className="absolute top-1 right-1 w-2 h-2 bg-ankur-error rounded-full"></span>
              </button>

              {/* User Menu */}
              <div className="relative">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center gap-3 p-2 hover:bg-gray-100 rounded-xl transition-colors"
                >
                  <div className="w-9 h-9 rounded-full bg-gradient-to-br from-ankur-primary to-ankur-primary-dark flex items-center justify-center text-white font-semibold">
                    {user?.name?.[0]?.toUpperCase() || 'U'}
                  </div>
                  <div className="hidden sm:block text-left">
                    <p className="text-sm font-medium text-gray-900">
                      {user?.name}
                    </p>
                    <p className="text-xs text-gray-500 capitalize">{role}</p>
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
                    <div className="absolute right-0 top-full mt-2 w-56 bg-white rounded-xl shadow-lg border border-gray-100 py-2 z-50 animate-fade-in">
                      <Link
                        href={`/${role}/profile`}
                        className="flex items-center gap-3 px-4 py-2.5 text-gray-700 hover:bg-gray-50"
                        onClick={() => setUserMenuOpen(false)}
                      >
                        <User size={18} />
                        <span>My Profile</span>
                      </Link>
                      <Link
                        href={`/${role}/settings`}
                        className="flex items-center gap-3 px-4 py-2.5 text-gray-700 hover:bg-gray-50"
                        onClick={() => setUserMenuOpen(false)}
                      >
                        <Settings size={18} />
                        <span>Settings</span>
                      </Link>
                      <hr className="my-2 border-gray-100" />
                      <button
                        onClick={() => {
                          setUserMenuOpen(false);
                          handleLogout();
                        }}
                        className="flex items-center gap-3 px-4 py-2.5 text-red-600 hover:bg-red-50 w-full"
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
