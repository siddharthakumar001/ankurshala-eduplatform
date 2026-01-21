'use client';
// Hot-reload trigger: Student layout with fixed sidebar and notification dropdown
import { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { usePathname } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { 
  BookOpen, 
  Calendar, 
  Home, 
  History, 
  Bell, 
  User, 
  Wallet,
  Menu,
  X,
  LogOut,
  Search,
  Moon,
  Sun,
  HelpCircle,
  Sparkles,
} from 'lucide-react';
import { useAuthStore } from '@/store/auth';
import { useRouter } from 'next/navigation';
import { useTheme } from '@/components/theme-provider';
import { NotificationBell } from '@/components/ui/notification-bell';

const primaryNavItems = [
  { href: '/student/dashboard', label: 'Dashboard', icon: Home },
  { href: '/student/discover', label: 'Discover', icon: Search },
  { href: '/student/study-list', label: 'Study List', icon: BookOpen },
  { href: '/student/booking', label: 'Book Class', icon: Calendar },
  { href: '/student/calendar', label: 'Calendar', icon: Calendar },
];

const secondaryNavItems = [
  { href: '/student/history', label: 'History', icon: History },
  { href: '/student/ai-tutor', label: 'AI Companion', icon: Sparkles },
  { href: '/student/payments', label: 'Wallet', icon: Wallet },
  { href: '/student/notifications', label: 'Notifications', icon: Bell },
  { href: '/student/profile', label: 'Profile', icon: User },
];

export default function StudentLayout({
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
      <div className={`fixed inset-y-0 left-0 z-50 w-60 bg-gradient-to-b from-slate-950/95 via-slate-950/95 to-slate-900/95 shadow-2xl backdrop-blur-xl border-r border-white/10 transform transition-transform duration-300 ease-in-out lg:translate-x-0 flex flex-col ${
        sidebarOpen ? 'translate-x-0' : '-translate-x-full'
      }`}>
        {/* Logo */}
        <div className="flex items-center h-16 px-5 border-b border-white/10">
          <Link href="/student/dashboard" className="flex items-center space-x-3">
            <Image 
              src="/ankurshala-logo-small.png" 
              alt="Ankurshala" 
              width={40} 
              height={40} 
              className="rounded-lg"
            />
            <div>
              <span className="text-base font-display font-semibold text-white tracking-tight">Ankurshala</span>
              <p className="text-[10px] text-white/60">On Demand Learning</p>
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
        <nav className="mt-4 px-4 flex-1 overflow-y-auto pb-24">
          <div className="space-y-5">
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-widest text-white/40 px-2 mb-2">
                Main
              </p>
              <div className="space-y-1">
                {primaryNavItems.map((item) => {
                  const Icon = item.icon;
                  const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      className={`relative flex items-center gap-3 px-3 py-2 rounded-xl text-sm transition-all duration-200 border ${
                        isActive
                          ? 'bg-white/15 text-white border-white/20 shadow-lg shadow-emerald-500/10'
                          : 'text-white/70 border-transparent hover:bg-white/10 hover:text-white'
                      }`}
                      onClick={() => setSidebarOpen(false)}
                    >
                      <span className={`absolute left-0 top-1/2 -translate-y-1/2 h-6 w-1 rounded-r-full ${isActive ? 'bg-ankur-accent' : 'bg-transparent'}`} />
                      <Icon className="h-4 w-4" />
                      <span className="font-medium">{item.label}</span>
                    </Link>
                  );
                })}
              </div>
            </div>
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-widest text-white/40 px-2 mb-2">
                Tools
              </p>
              <div className="space-y-1">
                {secondaryNavItems.map((item) => {
                  const Icon = item.icon;
                  const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      className={`relative flex items-center gap-3 px-3 py-2 rounded-xl text-sm transition-all duration-200 border ${
                        isActive
                          ? 'bg-white/15 text-white border-white/20 shadow-lg shadow-emerald-500/10'
                          : 'text-white/70 border-transparent hover:bg-white/10 hover:text-white'
                      }`}
                      onClick={() => setSidebarOpen(false)}
                    >
                      <span className={`absolute left-0 top-1/2 -translate-y-1/2 h-6 w-1 rounded-r-full ${isActive ? 'bg-ankur-accent' : 'bg-transparent'}`} />
                      <Icon className="h-4 w-4" />
                      <span className="font-medium">{item.label}</span>
                    </Link>
                  );
                })}
              </div>
            </div>
          </div>
        </nav>

        {/* User Section */}
        <div className="mt-auto p-4 border-t border-white/10">
          <div className="flex items-center space-x-3 mb-3 p-3 rounded-xl bg-white/10 border border-white/10">
            <div className="h-9 w-9 bg-gradient-to-br from-ankur-primary to-ankur-accent rounded-full flex items-center justify-center shadow-sm">
              <span className="text-white font-semibold text-sm">
                {user?.name?.charAt(0) || 'S'}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-white truncate">{user?.name?.split(' ')[0] || 'Student'}</p>
              <p className="text-[11px] text-white/60">Student</p>
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
      <div className="lg:ml-60">
        {/* Top bar */}
        <header className="sticky top-0 z-30 bg-white/80 backdrop-blur-2xl border-b border-white/50 shadow-sm dark:bg-slate-900/70 dark:border-white/10">
          <div className="flex items-center justify-between h-14 px-4 md:px-6">
            <div className="flex items-center gap-3 min-w-0">
              <Button
                variant="ghost"
                size="sm"
                className="lg:hidden text-gray-700 dark:text-gray-200"
                onClick={() => setSidebarOpen(true)}
              >
                <Menu className="h-5 w-5" />
              </Button>
              <div className="min-w-0">
                <p className="text-[9px] md:text-[10px] text-gray-500 uppercase font-semibold tracking-widest dark:text-gray-400">
                  Student Workspace
                </p>
                <h1 className="text-sm md:text-base font-display font-semibold text-gray-900 dark:text-gray-100 truncate max-w-[140px] sm:max-w-[200px]">
                  {([...primaryNavItems, ...secondaryNavItems].find(item => pathname === item.href || pathname.startsWith(item.href + '/'))?.label || 'Dashboard')}
                </h1>
              </div>
            </div>
            
            <div className="flex items-center gap-1 sm:gap-2">
              {/* Search */}
              <div className="hidden md:flex items-center bg-white/70 border border-white/60 rounded-2xl px-4 py-2 w-56 backdrop-blur shadow-sm dark:bg-slate-900/60 dark:border-white/10">
                <Search className="h-4 w-4 text-gray-400 mr-2" />
                <input
                  type="text"
                  placeholder="Search..."
                  className="bg-transparent border-none outline-none text-sm text-gray-700 w-full placeholder-gray-400 dark:text-gray-200"
                />
              </div>

              {/* Help */}
              <Button variant="ghost" size="sm" className="hidden sm:inline-flex text-gray-600 hover:text-gray-900 hover:bg-white/70 rounded-xl dark:text-gray-200 dark:hover:bg-slate-800/60">
                <HelpCircle className="h-5 w-5" />
              </Button>

              {/* Dark mode toggle */}
              <Button
                variant="ghost"
                size="sm"
                onClick={toggleTheme}
                className="hidden sm:inline-flex text-gray-600 hover:text-gray-900 hover:bg-white/70 rounded-xl dark:text-gray-200 dark:hover:bg-slate-800/60"
              >
                {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
              </Button>

              {/* Notifications */}
              <NotificationBell />

              {/* User Avatar */}
              <div className="flex items-center gap-2 ml-1 pl-3 border-l border-gray-200/60 dark:border-white/10">
                <div
                  className="h-8 w-8 bg-ankur-primary rounded-full flex items-center justify-center shadow-sm"
                  title={user?.name || 'Student'}
                >
                  <span className="text-white font-semibold text-sm">
                    {user?.name?.charAt(0) || 'S'}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="p-4 md:p-5">
          {children}
        </main>
      </div>
    </div>
  );
}
