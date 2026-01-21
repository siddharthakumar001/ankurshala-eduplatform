'use client'

import { useState, useEffect } from 'react'
import AuthGuard from '@/components/AuthGuard'
import SessionManager from '@/components/SessionManager'
import DashboardLayout from '@/components/layout/DashboardLayout'
import MetricCard from '@/components/ui/MetricCard'
import QuickActionCard from '@/components/ui/QuickActionCard'
import ActivityFeed, { ActivityType } from '@/components/ui/ActivityFeed'
import SystemStatusCard, { SystemStatus } from '@/components/ui/SystemStatusCard'
import { 
  Users, 
  GraduationCap, 
  BookOpen,
  CreditCard,
  UserPlus,
  FileText,
  TrendingUp,
  Clock,
  AlertCircle,
  RefreshCw,
  ChevronRight,
  DollarSign,
} from 'lucide-react'
import { api } from '@/utils/api'
import { useAuthStore } from '@/store/auth'

interface DashboardMetrics {
  totalStudents: number
  totalTeachers: number
  activeStudents: number
  activeTeachers: number
  inactiveStudents: number
  inactiveTeachers: number
  newStudentsLast7Days: number
  newStudentsLast30Days: number
  newTeachersLast7Days: number
  newTeachersLast30Days: number
  totalBoards: number
  totalGrades: number
  totalSubjects: number
  totalChapters: number
  totalTopics: number
  activeCourses: number
  completedCourses: number
  totalRevenueCents: number
  revenueLast30DaysCents: number
  revenuePrevious30DaysCents: number
}

interface DashboardActivityItem {
  id: string
  type: ActivityType
  title: string
  description?: string
  timestamp: string
  user?: string
}

interface SystemStatusItem {
  name: string
  status: SystemStatus
  latency?: string
  uptime?: string
}

export default function AdminDashboard() {
  const user = useAuthStore((state) => state.user)
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null)
  const [recentActivity, setRecentActivity] = useState<DashboardActivityItem[]>([])
  const [systemStatusItems, setSystemStatusItems] = useState<SystemStatusItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activityError, setActivityError] = useState<string | null>(null)
  const [statusError, setStatusError] = useState<string | null>(null)
  const [hasFetchedMetrics, setHasFetchedMetrics] = useState(false)
  const [isRefreshing, setIsRefreshing] = useState(false)

  const resolveErrorMessage = (err: unknown, fallback: string) => {
    if (err instanceof Error && err.message) {
      return err.message
    }
    return fallback
  }

  const fetchMetrics = async (isRefresh = false) => {
    try {
      if (isRefresh) setIsRefreshing(true)
      else setLoading(true)
      setError(null)
      setActivityError(null)
      setStatusError(null)

      const results = await Promise.allSettled([
        api.get<DashboardMetrics>('/admin/dashboard/metrics'),
        api.get<DashboardActivityItem[]>('/admin/dashboard/activity?limit=10'),
        api.get<SystemStatusItem[]>('/admin/dashboard/status'),
      ])

      const [metricsResult, activityResult, statusResult] = results

      if (metricsResult.status === 'fulfilled') {
        setMetrics(metricsResult.value.data as DashboardMetrics)
      } else {
        const message = resolveErrorMessage(metricsResult.reason, 'Failed to load dashboard metrics')
        if (message.includes('Unauthorized') || message.includes('401')) {
          setError('Session expired or unauthorized. Please login again.')
        } else {
          setError(message)
        }
      }

      if (activityResult.status === 'fulfilled') {
        setRecentActivity(activityResult.value.data as DashboardActivityItem[])
      } else {
        setRecentActivity([])
        setActivityError(resolveErrorMessage(activityResult.reason, 'Unable to load recent activity'))
      }

      if (statusResult.status === 'fulfilled') {
        setSystemStatusItems(statusResult.value.data as SystemStatusItem[])
      } else {
        setSystemStatusItems([])
        setStatusError(resolveErrorMessage(statusResult.reason, 'Unable to load system status'))
      }
    } catch (err: unknown) {
      console.error('Admin Dashboard - Error fetching data:', err)
      setError(resolveErrorMessage(err, 'Failed to load dashboard data'))
    } finally {
      setLoading(false)
      setIsRefreshing(false)
    }
  }

  useEffect(() => {
    if (!user || hasFetchedMetrics) return
    setHasFetchedMetrics(true)
    fetchMetrics()
  }, [user, hasFetchedMetrics])

  const formatRelativeTime = (timestamp: string) => {
    const date = new Date(timestamp)
    if (Number.isNaN(date.getTime())) return timestamp
    const diffMs = Math.max(0, Date.now() - date.getTime())
    const diffMinutes = Math.floor(diffMs / 60000)
    if (diffMinutes < 1) return 'just now'
    if (diffMinutes < 60) return `${diffMinutes} min ago`
    const diffHours = Math.floor(diffMinutes / 60)
    if (diffHours < 24) return `${diffHours} hr ago`
    const diffDays = Math.floor(diffHours / 24)
    return `${diffDays}d ago`
  }

  const formatInr = (cents: number) => {
    const rupees = Math.round(cents / 100)
    return `INR ${new Intl.NumberFormat('en-IN').format(rupees)}`
  }

  // Calculate percentage changes (real data from metrics)
  const studentGrowth = metrics?.newStudentsLast7Days && metrics?.totalStudents 
    ? Math.round((metrics.newStudentsLast7Days / metrics.totalStudents) * 100 * 10) / 10
    : 0
  const teacherGrowth = metrics?.newTeachersLast7Days && metrics?.totalTeachers
    ? Math.round((metrics.newTeachersLast7Days / metrics.totalTeachers) * 100 * 10) / 10
    : 0
  const revenueChange = metrics?.revenuePrevious30DaysCents
    ? Math.round(((metrics.revenueLast30DaysCents - metrics.revenuePrevious30DaysCents) / metrics.revenuePrevious30DaysCents) * 100 * 10) / 10
    : undefined
  const activityItems = recentActivity.map((item) => ({
    ...item,
    timestamp: formatRelativeTime(item.timestamp),
  }))
  const systemStatusDisplay = systemStatusItems.length
    ? systemStatusItems
    : statusError
      ? [{ name: 'System Status', status: 'degraded' as SystemStatus, latency: 'unavailable', uptime: '--' }]
      : []

  // Loading skeleton
  if (loading && !metrics) {
    return (
      <AuthGuard requiredRoles={['ADMIN']}>
        <SessionManager showSessionInfo={false}>
          <DashboardLayout role="admin">
            <div className="space-y-6 animate-pulse">
              <div className="h-32 bg-white/70 dark:bg-slate-900/70 rounded-2xl border border-white/40 dark:border-white/10" />
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {[...Array(4)].map((_, i) => (
                  <div key={i} className="h-36 bg-white/70 dark:bg-slate-900/70 rounded-xl border border-white/40 dark:border-white/10" />
                ))}
              </div>
            </div>
          </DashboardLayout>
        </SessionManager>
      </AuthGuard>
    )
  }

  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      <SessionManager showSessionInfo={false}>
        <DashboardLayout role="admin">
          <div className="space-y-6 relative">
            <div className="pointer-events-none absolute inset-0 -z-10">
              <div className="absolute -top-24 -right-16 h-64 w-64 rounded-full bg-ankur-accent/10 blur-3xl" />
              <div className="absolute bottom-10 left-6 h-72 w-72 rounded-full bg-ankur-primary/10 blur-3xl" />
            </div>
            {/* Page Header */}
            <div className="page-header rounded-2xl p-8 text-white">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                  <h1 className="text-2xl md:text-3xl font-bold mb-2">
                    Welcome back, {user?.name || 'Admin'}!
                  </h1>
                  <p className="text-white/80">
                    Here&apos;s what&apos;s happening with your platform today.
                  </p>
                </div>
                <div className="flex items-center gap-3">
                    <button
                      onClick={() => fetchMetrics(true)}
                      disabled={isRefreshing}
                      className="flex items-center gap-2 bg-white/10 hover:bg-white/20 text-white px-4 py-2.5 rounded-xl transition-colors disabled:opacity-50"
                    >
                    <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin' : ''}`} />
                    {isRefreshing ? 'Refreshing...' : 'Refresh'}
                  </button>
                  <div className="flex items-center gap-2 bg-white/10 px-4 py-2.5 rounded-xl text-sm">
                    <Clock className="w-4 h-4" />
                    <span>{new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Error Alert */}
            {error && (
              <div className="glass rounded-2xl p-4 border border-red-200/60 dark:border-red-500/30 flex items-center gap-3 bg-red-50/70 dark:bg-red-500/10">
                <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
                <div className="flex-1">
                  <p className="text-red-800 dark:text-red-200 font-medium">Error loading dashboard</p>
                  <p className="text-red-600 dark:text-red-200 text-sm">{error}</p>
                </div>
                <button
                  onClick={() => fetchMetrics(true)}
                  className="text-red-600 dark:text-red-200 hover:text-red-800 font-medium text-sm"
                >
                  Retry
                </button>
              </div>
            )}

            {/* Key Metrics */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              <MetricCard
                title="Total Students"
                value={metrics?.totalStudents?.toLocaleString() || '0'}
                change={studentGrowth}
                changeLabel="growth this week"
                icon={GraduationCap}
                iconBgColor="bg-blue-500"
              />
              <MetricCard
                title="Total Teachers"
                value={metrics?.totalTeachers?.toLocaleString() || '0'}
                change={teacherGrowth}
                changeLabel="growth this week"
                icon={Users}
                iconBgColor="bg-ankur-primary"
              />
              <MetricCard
                title="Active Subjects"
                value={metrics?.activeCourses?.toLocaleString() || '0'}
                icon={BookOpen}
                iconBgColor="bg-purple-500"
              />
              <MetricCard
                title="Revenue (30 days)"
                value={formatInr(metrics?.revenueLast30DaysCents || 0)}
                change={revenueChange}
                changeLabel="vs previous 30 days"
                icon={DollarSign}
                iconBgColor="bg-ankur-accent"
                iconColor="text-gray-900"
              />
            </div>

            {/* Quick Actions */}
            <div>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-ankur-secondary dark:text-white">Quick Actions</h2>
                <a href="/admin/profile" className="text-sm text-ankur-primary hover:underline flex items-center gap-1">
                  View profile <ChevronRight className="w-4 h-4" />
                </a>
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                <QuickActionCard
                  title="Manage Students"
                  icon={UserPlus}
                  href="/admin/users/students"
                  iconBgColor="bg-blue-50"
                  iconColor="text-blue-600"
                />
                <QuickActionCard
                  title="Manage Teachers"
                  icon={Users}
                  href="/admin/users/teachers"
                  iconBgColor="bg-green-50"
                  iconColor="text-green-600"
                />
                <QuickActionCard
                  title="Content Structure"
                  icon={BookOpen}
                  href="/admin/content/manage"
                  iconBgColor="bg-purple-50"
                  iconColor="text-purple-600"
                />
                <QuickActionCard
                  title="Content Import"
                  icon={FileText}
                  href="/admin/content/import"
                  iconBgColor="bg-orange-50"
                  iconColor="text-orange-600"
                />
                <QuickActionCard
                  title="Pricing"
                  icon={DollarSign}
                  href="/admin/pricing"
                  iconBgColor="bg-emerald-50"
                  iconColor="text-emerald-600"
                />
                <QuickActionCard
                  title="Fee Waivers"
                  icon={CreditCard}
                  href="/admin/fees"
                  iconBgColor="bg-indigo-50"
                  iconColor="text-indigo-600"
                />
              </div>
            </div>

            {/* Main Content Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Activity Feed */}
              <div className="lg:col-span-2">
                {activityError && (
                  <div className="mb-3 text-sm text-amber-600 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
                    {activityError}
                  </div>
                )}
                <ActivityFeed
                  items={activityItems}
                  maxItems={5}
                  onViewAll={() => window.location.href = '/admin/content/analytics'}
                />
              </div>

              {/* System Status */}
              <div>
                {statusError && (
                  <div className="mb-3 text-sm text-amber-600 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2">
                    {statusError}
                  </div>
                )}
                <SystemStatusCard items={systemStatusDisplay} />
              </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Content Statistics */}
              <div className="glass-panel rounded-2xl border border-white/40 dark:border-white/10 shadow-sm p-6">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="font-semibold text-ankur-secondary dark:text-white">Content Statistics</h3>
                  <FileText className="w-5 h-5 text-gray-400 dark:text-gray-300" />
                </div>
                <div className="space-y-4">
                  {[
                    { label: 'Total Boards', value: metrics?.totalBoards || 0, color: 'bg-blue-500' },
                    { label: 'Total Grades', value: metrics?.totalGrades || 0, color: 'bg-green-500' },
                    { label: 'Total Subjects', value: metrics?.totalSubjects || 0, color: 'bg-purple-500' },
                    { label: 'Total Chapters', value: metrics?.totalChapters || 0, color: 'bg-orange-500' },
                    { label: 'Total Topics', value: metrics?.totalTopics || 0, color: 'bg-pink-500' },
                  ].map((item, index) => (
                    <div key={index} className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <div className={`w-2 h-2 rounded-full ${item.color}`} />
                        <span className="text-sm text-gray-600 dark:text-gray-300">{item.label}</span>
                      </div>
                      <span className="font-semibold text-gray-900 dark:text-white">{item.value.toLocaleString()}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* User Growth */}
              <div className="glass-panel rounded-2xl border border-white/40 dark:border-white/10 shadow-sm p-6">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="font-semibold text-ankur-secondary dark:text-white">User Growth</h3>
                  <TrendingUp className="w-5 h-5 text-green-500" />
                </div>
                <div className="space-y-4">
                  <div className="p-4 bg-blue-50/80 dark:bg-blue-900/20 rounded-xl">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-blue-900 dark:text-blue-100">New Students</span>
                      <span className="text-xs text-blue-600 dark:text-blue-200">Last 30 days</span>
                    </div>
                    <div className="flex items-baseline gap-2">
                      <span className="text-2xl font-bold text-blue-900 dark:text-blue-100">{metrics?.newStudentsLast30Days || 0}</span>
                      <span className="text-sm text-blue-600 dark:text-blue-200">
                        (+{metrics?.newStudentsLast7Days || 0} this week)
                      </span>
                    </div>
                  </div>
                  <div className="p-4 bg-green-50/80 dark:bg-green-900/20 rounded-xl">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-green-900 dark:text-green-100">New Teachers</span>
                      <span className="text-xs text-green-600 dark:text-green-200">Last 30 days</span>
                    </div>
                    <div className="flex items-baseline gap-2">
                      <span className="text-2xl font-bold text-green-900 dark:text-green-100">{metrics?.newTeachersLast30Days || 0}</span>
                      <span className="text-sm text-green-600 dark:text-green-200">
                        (+{metrics?.newTeachersLast7Days || 0} this week)
                      </span>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="p-3 bg-gray-50/80 dark:bg-slate-800/60 rounded-xl text-center">
                      <p className="text-2xl font-bold text-gray-900 dark:text-white">{metrics?.activeStudents || 0}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">Active Students</p>
                    </div>
                    <div className="p-3 bg-gray-50/80 dark:bg-slate-800/60 rounded-xl text-center">
                      <p className="text-2xl font-bold text-gray-900 dark:text-white">{metrics?.activeTeachers || 0}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">Active Teachers</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </DashboardLayout>
      </SessionManager>
    </AuthGuard>
  )
}
