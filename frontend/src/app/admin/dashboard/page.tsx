'use client'

import { useState, useEffect } from 'react'
import AuthGuard from '@/components/AuthGuard'
import SessionManager from '@/components/SessionManager'
import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { 
  Users, 
  GraduationCap, 
  CheckCircle,
  Clock,
  AlertCircle
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

  const fetchMetrics = async () => {
    try {
      console.log('Admin Dashboard - Starting metrics fetch...')
      setLoading(true)
      setError(null)
      
      // Direct API call - the backend returns DashboardMetricsDto directly, not wrapped
      const response = await api.get('/admin/dashboard/metrics')
      
      console.log('Admin Dashboard - Raw response:', response)
      
      // The response.data contains the actual metrics (not wrapped in ApiResponse)
      const metricsData = response.data as DashboardMetrics
      
      console.log('Admin Dashboard - Metrics fetched successfully:', metricsData)
      setMetrics(metricsData)
      
    } catch (err: any) {
      console.error('Admin Dashboard - Error fetching metrics:', err)
      console.error('Admin Dashboard - Error details:', {
        message: err?.message,
        response: err?.response,
        status: err?.response?.status
      })
      
      // Handle 401 Unauthorized specifically
      if (err?.message?.includes('Unauthorized') || err?.message?.includes('401')) {
        setError('Session expired or unauthorized. Please login again.')
      } else {
        const errorMessage = err?.message || 'Failed to load dashboard metrics'
        setError(errorMessage)
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    console.log('Admin Dashboard - Checking authentication...', {
      hasUser: !!user,
      userEmail: user?.email,
      hasFetchedMetrics
    })
    
    if (!user) {
      console.log('Admin Dashboard - No user authenticated')
      setLoading(false)
      return
    }

    if (hasFetchedMetrics) {
      console.log('Admin Dashboard - Metrics already fetched, skipping')
      return
    }

    console.log('Admin Dashboard - User authenticated, fetching metrics...')
    setHasFetchedMetrics(true)
    fetchMetrics()
  }, [user, hasFetchedMetrics])

  // Loading skeleton component
  const MetricsSkeleton = () => (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      {[...Array(4)].map((_, index) => (
        <Card key={index} className="p-6">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
            <div className="h-8 bg-gray-200 rounded w-1/2 mb-2"></div>
            <div className="h-3 bg-gray-200 rounded w-1/4"></div>
          </div>
        </Card>
      ))}
    </div>
  )

  // Error component
  const ErrorDisplay = () => (
    <Card className="p-6 border-red-200 bg-red-50 dark:bg-red-900/20">
      <div className="flex items-center space-x-2 text-red-600 dark:text-red-400">
        <AlertCircle className="h-5 w-5" />
        <span className="font-medium">Error loading dashboard</span>
      </div>
      <p className="text-red-600 dark:text-red-400 mt-2">{error}</p>
      <Button 
        onClick={fetchMetrics} 
        variant="outline" 
        size="sm" 
        className="mt-4"
      >
        Try Again
      </Button>
    </Card>
  )

  // Define metric cards data
  const metricCards = [
    {
      title: 'Total Students',
      value: metrics?.totalStudents || 0,
      change: metrics?.newStudentsLast7Days || 0,
      changeLabel: 'New this week',
      icon: Users,
      color: 'text-blue-600 dark:text-blue-400',
      bgColor: 'bg-blue-100 dark:bg-blue-900'
    },
    {
      title: 'Total Teachers',
      value: metrics?.totalTeachers || 0,
      change: metrics?.newTeachersLast7Days || 0,
      changeLabel: 'New this week',
      icon: GraduationCap,
      color: 'text-green-600 dark:text-green-400',
      bgColor: 'bg-green-100 dark:bg-green-900'
    },
    {
      title: 'Active Students',
      value: metrics?.activeStudents || 0,
      change: metrics?.activeStudents || 0,
      changeLabel: 'Currently active',
      icon: CheckCircle,
      color: 'text-emerald-600 dark:text-emerald-400',
      bgColor: 'bg-emerald-100 dark:bg-emerald-900'
    },
    {
      title: 'Active Teachers',
      value: metrics?.activeTeachers || 0,
      change: metrics?.activeTeachers || 0,
      changeLabel: 'Currently active',
      icon: CheckCircle,
      color: 'text-purple-600 dark:text-purple-400',
      bgColor: 'bg-purple-100 dark:bg-purple-900'
    }
  ]

  return (
    <AuthGuard requiredRoles={['ADMIN']}>
      <SessionManager showSessionInfo={true}>
        <AdminLayoutSimple>
          <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Dashboard</h1>
                <p className="text-gray-600 dark:text-gray-400">Welcome back, Admin! Here's what's happening.</p>
              </div>
              <div className="flex items-center space-x-2 text-sm text-gray-500 dark:text-gray-400">
                <Clock className="h-4 w-4" />
                <span>Last updated: {new Date().toLocaleTimeString()}</span>
              </div>
            </div>

            {/* Metrics Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6" data-testid="dashboard-metrics">
              {loading ? (
                <MetricsSkeleton />
              ) : error ? (
                <div className="col-span-full">
                  <ErrorDisplay />
                </div>
              ) : (
                metricCards.map((card, index) => (
                  <Card key={index} className="p-6 hover:shadow-lg transition-shadow">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-600 dark:text-gray-400">{card.title}</p>
                        <p className="text-2xl font-bold text-gray-900 dark:text-white">{card.value}</p>
                        <p className="text-xs text-gray-500 dark:text-gray-400">{card.changeLabel}: {card.change}</p>
                      </div>
                      <div className={`p-3 rounded-full ${card.bgColor}`}>
                        <card.icon className={`h-6 w-6 ${card.color}`} />
                      </div>
                    </div>
                  </Card>
                ))
              )}
            </div>

            {/* Quick Stats */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card className="p-6">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Content Statistics</h3>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Total Boards</span>
                    <span className="font-semibold text-gray-900 dark:text-white">{metrics?.totalBoards || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Total Subjects</span>
                    <span className="font-semibold text-gray-900 dark:text-white">{metrics?.totalSubjects || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Total Chapters</span>
                    <span className="font-semibold text-gray-900 dark:text-white">{metrics?.totalChapters || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Total Topics</span>
                    <span className="font-semibold text-gray-900 dark:text-white">{metrics?.totalTopics || 0}</span>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">User Activity</h3>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">New Students (7 days)</span>
                    <span className="font-semibold text-gray-900 dark:text-white">{metrics?.newStudentsLast7Days || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">New Teachers (7 days)</span>
                    <span className="font-semibold text-gray-900 dark:text-white">{metrics?.newTeachersLast7Days || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">New Students (30 days)</span>
                    <span className="font-semibold text-gray-900 dark:text-white">{metrics?.newStudentsLast30Days || 0}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600 dark:text-gray-400">New Teachers (30 days)</span>
                    <span className="font-semibold text-gray-900 dark:text-white">{metrics?.newTeachersLast30Days || 0}</span>
                  </div>
                </div>
              </Card>
            </div>
          </div>
        </AdminLayoutSimple>
      </SessionManager>
    </AuthGuard>
  )
}