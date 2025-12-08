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
  Calendar,
  CreditCard,
  Bell,
  Settings,
  UserPlus,
  FileText,
  TrendingUp,
  Clock,
  AlertCircle,
  RefreshCw,
  ChevronRight,
  Activity,
  DollarSign,
  CheckCircle,
  BarChart3,
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
}

export default function AdminDashboard() {
  const user = useAuthStore((state) => state.user)
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [hasFetchedMetrics, setHasFetchedMetrics] = useState(false)
  const [isRefreshing, setIsRefreshing] = useState(false)

  const fetchMetrics = async (isRefresh = false) => {
    try {
      if (isRefresh) setIsRefreshing(true)
      else setLoading(true)
      setError(null)
      
      const response = await api.get('/admin/dashboard/metrics')
      const metricsData = response.data as DashboardMetrics
      setMetrics(metricsData)
      
    } catch (err: any) {
      console.error('Admin Dashboard - Error fetching metrics:', err)
      if (err?.message?.includes('Unauthorized') || err?.message?.includes('401')) {
        setError('Session expired or unauthorized. Please login again.')
      } else {
        setError(err?.message || 'Failed to load dashboard metrics')
      }
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

  // Calculate percentage changes (mock data for demonstration)
  const studentGrowth = metrics?.newStudentsLast7Days && metrics?.totalStudents 
    ? Math.round((metrics.newStudentsLast7Days / metrics.totalStudents) * 100 * 10) / 10
    : 0
  const teacherGrowth = metrics?.newTeachersLast7Days && metrics?.totalTeachers
    ? Math.round((metrics.newTeachersLast7Days / metrics.totalTeachers) * 100 * 10) / 10
    : 0

  // Recent activity data (mock - would come from API)
  const recentActivity = [
    { id: '1', type: 'success' as ActivityType, title: 'New student registered', description: 'John Doe enrolled in Class 10 Physics', timestamp: '2 min ago', user: 'System' },
    { id: '2', type: 'info' as ActivityType, title: 'Booking confirmed', description: 'Class scheduled for tomorrow at 4 PM', timestamp: '15 min ago', user: 'Priya Sharma' },
    { id: '3', type: 'warning' as ActivityType, title: 'Payment pending', description: 'Invoice #INV-2024-001 awaiting payment', timestamp: '1 hour ago', user: 'Rahul Kumar' },
    { id: '4', type: 'success' as ActivityType, title: 'Teacher approved', description: 'Dr. Amit Singh is now verified', timestamp: '2 hours ago', user: 'Admin' },
    { id: '5', type: 'info' as ActivityType, title: 'Course content updated', description: 'New chapter added to Class 12 Mathematics', timestamp: '3 hours ago', user: 'Content Team' },
  ]

  // System status data (would come from API in production)
  const systemStatus = [
    { name: 'Backend API', status: 'operational' as SystemStatus, latency: '45ms', uptime: '99.9%' },
    { name: 'Database', status: 'operational' as SystemStatus, latency: '12ms', uptime: '99.99%' },
    { name: 'Redis Cache', status: 'operational' as SystemStatus, latency: '2ms', uptime: '99.9%' },
    { name: 'Payment Gateway', status: 'operational' as SystemStatus, latency: '120ms', uptime: '99.8%' },
  ]

  // Loading skeleton
  if (loading && !metrics) {
    return (
      <AuthGuard requiredRoles={['ADMIN']}>
        <SessionManager showSessionInfo={false}>
          <DashboardLayout role="admin">
            <div className="space-y-6 animate-pulse">
              <div className="h-32 bg-gray-200 rounded-2xl" />
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {[...Array(4)].map((_, i) => (
                  <div key={i} className="h-36 bg-gray-200 rounded-xl" />
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
          <div className="space-y-6">
            {/* Page Header */}
            <div className="bg-gradient-to-r from-ankur-secondary to-[#2a4a73] rounded-2xl p-8 text-white">
              <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                <div>
                  <h1 className="text-2xl md:text-3xl font-bold mb-2">
                    Welcome back, {user?.name || 'Admin'}! 👋
                  </h1>
                  <p className="text-white/80">
                    Here&apos;s what&apos;s happening with your platform today.
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => fetchMetrics(true)}
                    disabled={isRefreshing}
                    className="flex items-center gap-2 bg-white/10 hover:bg-white/20 text-white px-4 py-2.5 rounded-lg transition-colors disabled:opacity-50"
                  >
                    <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin' : ''}`} />
                    {isRefreshing ? 'Refreshing...' : 'Refresh'}
                  </button>
                  <div className="flex items-center gap-2 bg-white/10 px-4 py-2.5 rounded-lg text-sm">
                    <Clock className="w-4 h-4" />
                    <span>{new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Error Alert */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3">
                <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
                <div className="flex-1">
                  <p className="text-red-800 font-medium">Error loading dashboard</p>
                  <p className="text-red-600 text-sm">{error}</p>
                </div>
                <button
                  onClick={() => fetchMetrics(true)}
                  className="text-red-600 hover:text-red-800 font-medium text-sm"
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
                title="Active Courses"
                value={metrics?.totalSubjects?.toLocaleString() || '0'}
                change={5}
                changeLabel="new this month"
                icon={BookOpen}
                iconBgColor="bg-purple-500"
              />
              <MetricCard
                title="Revenue"
                value="₹4,52,000"
                change={12.5}
                changeLabel="vs last month"
                icon={DollarSign}
                iconBgColor="bg-ankur-accent"
                iconColor="text-gray-900"
              />
            </div>

            {/* Quick Actions */}
            <div>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-semibold text-gray-900">Quick Actions</h2>
                <a href="/admin/settings" className="text-sm text-ankur-primary hover:underline flex items-center gap-1">
                  View all settings <ChevronRight className="w-4 h-4" />
                </a>
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                <QuickActionCard
                  title="Add Student"
                  icon={UserPlus}
                  href="/admin/students/new"
                  iconBgColor="bg-blue-50"
                  iconColor="text-blue-600"
                />
                <QuickActionCard
                  title="Add Teacher"
                  icon={Users}
                  href="/admin/teachers/new"
                  iconBgColor="bg-green-50"
                  iconColor="text-green-600"
                />
                <QuickActionCard
                  title="Manage Subjects"
                  icon={BookOpen}
                  href="/admin/subjects"
                  iconBgColor="bg-purple-50"
                  iconColor="text-purple-600"
                />
                <QuickActionCard
                  title="View Bookings"
                  icon={Calendar}
                  href="/admin/bookings"
                  iconBgColor="bg-orange-50"
                  iconColor="text-orange-600"
                />
                <QuickActionCard
                  title="Payments"
                  icon={CreditCard}
                  href="/admin/payments"
                  iconBgColor="bg-emerald-50"
                  iconColor="text-emerald-600"
                />
                <QuickActionCard
                  title="Reports"
                  icon={BarChart3}
                  href="/admin/reports"
                  iconBgColor="bg-indigo-50"
                  iconColor="text-indigo-600"
                />
              </div>
            </div>

            {/* Main Content Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* Activity Feed */}
              <div className="lg:col-span-2">
                <ActivityFeed
                  items={recentActivity}
                  maxItems={5}
                  onViewAll={() => window.location.href = '/admin/activity'}
                />
              </div>

              {/* System Status */}
              <div>
                <SystemStatusCard items={systemStatus} />
              </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Content Statistics */}
              <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="font-semibold text-gray-900">Content Statistics</h3>
                  <FileText className="w-5 h-5 text-gray-400" />
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
                        <span className="text-sm text-gray-600">{item.label}</span>
                      </div>
                      <span className="font-semibold text-gray-900">{item.value.toLocaleString()}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* User Growth */}
              <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-6">
                <div className="flex items-center justify-between mb-6">
                  <h3 className="font-semibold text-gray-900">User Growth</h3>
                  <TrendingUp className="w-5 h-5 text-green-500" />
                </div>
                <div className="space-y-4">
                  <div className="p-4 bg-blue-50 rounded-xl">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-blue-900">New Students</span>
                      <span className="text-xs text-blue-600">Last 30 days</span>
                    </div>
                    <div className="flex items-baseline gap-2">
                      <span className="text-2xl font-bold text-blue-900">{metrics?.newStudentsLast30Days || 0}</span>
                      <span className="text-sm text-blue-600">
                        (+{metrics?.newStudentsLast7Days || 0} this week)
                      </span>
                    </div>
                  </div>
                  <div className="p-4 bg-green-50 rounded-xl">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm font-medium text-green-900">New Teachers</span>
                      <span className="text-xs text-green-600">Last 30 days</span>
                    </div>
                    <div className="flex items-baseline gap-2">
                      <span className="text-2xl font-bold text-green-900">{metrics?.newTeachersLast30Days || 0}</span>
                      <span className="text-sm text-green-600">
                        (+{metrics?.newTeachersLast7Days || 0} this week)
                      </span>
                    </div>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="p-3 bg-gray-50 rounded-xl text-center">
                      <p className="text-2xl font-bold text-gray-900">{metrics?.activeStudents || 0}</p>
                      <p className="text-xs text-gray-500">Active Students</p>
                    </div>
                    <div className="p-3 bg-gray-50 rounded-xl text-center">
                      <p className="text-2xl font-bold text-gray-900">{metrics?.activeTeachers || 0}</p>
                      <p className="text-xs text-gray-500">Active Teachers</p>
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
