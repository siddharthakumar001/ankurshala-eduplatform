'use client'

import { useState, useEffect } from 'react'
import { AdminRoute } from '@/components/route-guard'
import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { 
  Users, 
  GraduationCap, 
  FileText, 
  TrendingUp,
  Clock,
  CheckCircle,
  AlertCircle
} from 'lucide-react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import dynamic from 'next/dynamic'

// Dynamically import chart components to prevent SSR issues
const DynamicLineChart = dynamic(() => import('recharts').then(mod => ({ default: mod.LineChart })), { ssr: false })
const DynamicResponsiveContainer = dynamic(() => import('recharts').then(mod => ({ default: mod.ResponsiveContainer })), { ssr: false })

interface DashboardMetrics {
  // User counts
  totalStudents: number
  totalTeachers: number
  activeStudents: number
  activeTeachers: number
  inactiveStudents: number
  inactiveTeachers: number
  
  // Registration trends
  newStudentsLast7Days: number
  newStudentsLast30Days: number
  newTeachersLast7Days: number
  newTeachersLast30Days: number
  
  // Content counts (placeholders for now)
  totalBoards: number
  totalGrades: number
  totalSubjects: number
  totalChapters: number
  totalTopics: number
  
  // Course counts (placeholders for now)
  activeCourses: number
  completedCourses: number
}

interface DashboardSeries {
  date: string
  students: number
  teachers: number
}

export default function AdminDashboard() {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null)
  const [series, setSeries] = useState<DashboardSeries[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [mounted, setMounted] = useState(false)

  const fetchMetrics = async () => {
    try {
      setLoading(true)
      setError(null)
      
      const token = localStorage.getItem('accessToken')
      if (!token) {
        throw new Error('No access token found')
      }

      // Fetch metrics and series data in parallel
      const [metricsResponse, seriesResponse] = await Promise.all([
        fetch('http://localhost:8080/api/admin/dashboard/metrics', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        }),
        fetch('http://localhost:8080/api/admin/dashboard/series', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        })
      ])

      if (!metricsResponse.ok) {
        throw new Error(`Failed to fetch metrics: ${metricsResponse.statusText}`)
      }
      
      if (!seriesResponse.ok) {
        throw new Error(`Failed to fetch series: ${seriesResponse.statusText}`)
      }

      const metricsData = await metricsResponse.json()
      const seriesData = await seriesResponse.json()
      
      setMetrics(metricsData)
      setSeries(seriesData)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load dashboard metrics')
      console.error('Error fetching metrics:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    setMounted(true)
    // Wait for authentication token to be available
    const checkAuthAndFetch = () => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        fetchMetrics()
      } else {
        // Retry after a short delay
        setTimeout(checkAuthAndFetch, 100)
      }
    }
    checkAuthAndFetch()

    // Cleanup function to prevent memory leaks
    return () => {
      setMounted(false)
    }
  }, [])

  // Prevent hydration issues
  if (!mounted) {
    return null
  }

  if (loading) {
    return (
      <AdminRoute>
        <AdminLayoutSimple>
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {[...Array(4)].map((_, i) => (
                <Card key={i} className="p-6">
                  <div className="animate-pulse">
                    <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mb-2"></div>
                    <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-1/2"></div>
                  </div>
                </Card>
              ))}
            </div>
            <Card className="p-6">
              <div className="animate-pulse">
                <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-1/4 mb-4"></div>
                <div className="h-64 bg-gray-200 dark:bg-gray-700 rounded"></div>
              </div>
            </Card>
          </div>
        </AdminLayoutSimple>
      </AdminRoute>
    )
  }

  if (error) {
    return (
      <AdminRoute>
        <AdminLayoutSimple>
          <Card className="p-6">
            <div className="flex items-center space-x-3 text-red-600 dark:text-red-400">
              <AlertCircle className="h-5 w-5" />
              <p>{error}</p>
            </div>
            <Button onClick={fetchMetrics} className="mt-4">
              Retry
            </Button>
          </Card>
        </AdminLayoutSimple>
      </AdminRoute>
    )
  }

  if (!metrics) {
    return (
      <AdminRoute>
        <AdminLayoutSimple>
          <Card className="p-6">
            <p className="text-gray-500 dark:text-gray-400">No data available</p>
          </Card>
        </AdminLayoutSimple>
      </AdminRoute>
    )
  }

  const metricCards = [
    {
      title: 'Total Students',
      value: metrics.totalStudents,
      icon: GraduationCap,
      color: 'text-blue-600 dark:text-blue-400',
      bgColor: 'bg-blue-100 dark:bg-blue-900'
    },
    {
      title: 'Total Teachers',
      value: metrics.totalTeachers,
      icon: Users,
      color: 'text-green-600 dark:text-green-400',
      bgColor: 'bg-green-100 dark:bg-green-900'
    },
    {
      title: 'New Students (7 days)',
      value: metrics.newStudentsLast7Days,
      icon: TrendingUp,
      color: 'text-emerald-600 dark:text-emerald-400',
      bgColor: 'bg-emerald-100 dark:bg-emerald-900'
    },
    {
      title: 'New Teachers (7 days)',
      value: metrics.newTeachersLast7Days,
      icon: CheckCircle,
      color: 'text-purple-600 dark:text-purple-400',
      bgColor: 'bg-purple-100 dark:bg-purple-900'
    }
  ]

  return (
    <AdminRoute>
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
          {metricCards.map((card, index) => {
            const Icon = card.icon
            return (
              <Card key={index} className="p-6 hover:shadow-lg transition-shadow">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">{card.title}</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">{card.value.toLocaleString()}</p>
                  </div>
                  <div className={`p-3 rounded-full ${card.bgColor}`}>
                    <Icon className={`h-6 w-6 ${card.color}`} />
                  </div>
                </div>
              </Card>
            )
          })}
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card className="p-6">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Recent Activity</h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="p-2 bg-blue-100 dark:bg-blue-900 rounded-full">
                    <TrendingUp className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900 dark:text-white">New Students (7 days)</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">Recent registrations</p>
                  </div>
                </div>
                <span className="text-lg font-bold text-gray-900 dark:text-white">{metrics.newStudentsLast7Days}</span>
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="p-2 bg-green-100 dark:bg-green-900 rounded-full">
                    <TrendingUp className="h-4 w-4 text-green-600 dark:text-green-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900 dark:text-white">New Teachers (7 days)</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">Recent registrations</p>
                  </div>
                </div>
                <span className="text-lg font-bold text-gray-900 dark:text-white">{metrics.newTeachersLast7Days}</span>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Course Status</h3>
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="p-2 bg-emerald-100 dark:bg-emerald-900 rounded-full">
                    <CheckCircle className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900 dark:text-white">Active Courses</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">Currently running</p>
                  </div>
                </div>
                <span className="text-lg font-bold text-gray-900 dark:text-white">{metrics.activeCourses}</span>
              </div>
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="p-2 bg-purple-100 dark:bg-purple-900 rounded-full">
                    <FileText className="h-4 w-4 text-purple-600 dark:text-purple-400" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900 dark:text-white">Completed Courses</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">Finished courses</p>
                  </div>
                </div>
                <span className="text-lg font-bold text-gray-900 dark:text-white">{metrics.completedCourses}</span>
              </div>
            </div>
          </Card>
        </div>

        {/* Charts Section */}
        <Card className="p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Registration Trends (Last 30 Days)</h3>
          <div className="h-64">
            {series.length > 0 && mounted ? (
              <DynamicResponsiveContainer width="100%" height="100%">
                <DynamicLineChart data={series}>
                  <CartesianGrid strokeDasharray="3 3" className="opacity-30" />
                  <XAxis 
                    dataKey="date" 
                    tick={{ fontSize: 12 }}
                    tickFormatter={(value) => {
                      try {
                        const date = new Date(value)
                        return `${date.getMonth() + 1}/${date.getDate()}`
                      } catch (error) {
                        return value
                      }
                    }}
                  />
                  <YAxis tick={{ fontSize: 12 }} />
                  <Tooltip 
                    labelFormatter={(value) => {
                      try {
                        const date = new Date(value)
                        return date.toLocaleDateString()
                      } catch (error) {
                        return value
                      }
                    }}
                    formatter={(value, name) => [value, name === 'students' ? 'Students' : 'Teachers']}
                    contentStyle={{
                      backgroundColor: 'var(--background)',
                      border: '1px solid var(--border)',
                      borderRadius: '6px'
                    }}
                  />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="students" 
                    stroke="#3b82f6" 
                    strokeWidth={2}
                    name="Students"
                    dot={{ fill: '#3b82f6', strokeWidth: 2, r: 4 }}
                  />
                  <Line 
                    type="monotone" 
                    dataKey="teachers" 
                    stroke="#10b981" 
                    strokeWidth={2}
                    name="Teachers"
                    dot={{ fill: '#10b981', strokeWidth: 2, r: 4 }}
                  />
                </DynamicLineChart>
              </DynamicResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center">
                <p className="text-gray-500 dark:text-gray-400">
                  {loading ? 'Loading chart data...' : 'No data available for charts'}
                </p>
              </div>
            )}
          </div>
        </Card>
      </div>
      </AdminLayoutSimple>
    </AdminRoute>
  )
}
