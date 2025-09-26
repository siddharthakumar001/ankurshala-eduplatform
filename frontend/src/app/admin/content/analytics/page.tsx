'use client'

import { useState, useEffect } from 'react'
import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { 
  BarChart3, 
  TrendingUp, 
  Users, 
  BookOpen, 
  FileText, 
  Upload,
  GraduationCap,
  Activity,
  AlertCircle,
  CheckCircle
} from 'lucide-react'
import { 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell
} from 'recharts'

interface AnalyticsOverview {
  totalStudents: number
  totalTeachers: number
  activeStudents: number
  activeTeachers: number
  totalBoards: number
  totalSubjects: number
  totalChapters: number
  totalTopics: number
  totalImports: number
  successfulImports: number
  failedImports: number
  newStudents: number
  newTeachers: number
}

interface UserAnalytics {
  totalStudents: number
  totalTeachers: number
  activeStudents: number
  activeTeachers: number
  newStudents: number
  newTeachers: number
  boardDistribution: Record<string, number>
}

interface ContentAnalytics {
  totalBoards: number
  totalSubjects: number
  totalChapters: number
  totalTopics: number
  activeBoards: number
  activeSubjects: number
  activeChapters: number
  activeTopics: number
}

interface ImportAnalytics {
  totalImports: number
  successfulImports: number
  failedImports: number
  pendingImports: number
  runningImports: number
}

export default function AdminAnalyticsPage() {
  const [activeTab, setActiveTab] = useState('overview')
  const [dateRange, setDateRange] = useState('30')
  const [loading, setLoading] = useState(true)
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null)
  const [userAnalytics, setUserAnalytics] = useState<UserAnalytics | null>(null)
  const [contentAnalytics, setContentAnalytics] = useState<ContentAnalytics | null>(null)
  const [importAnalytics, setImportAnalytics] = useState<ImportAnalytics | null>(null)

  useEffect(() => {
    // Wait for authentication token to be available
    const checkAuthAndFetch = () => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        fetchAnalytics()
      } else {
        // Retry after a short delay
        setTimeout(checkAuthAndFetch, 100)
      }
    }
    checkAuthAndFetch()
  }, [dateRange])

  const fetchAnalytics = async () => {
    try {
      setLoading(true)
      const token = localStorage.getItem('accessToken')
      if (!token) {
        console.error('No access token found')
        return
      }

      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
      const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }

      // Fetch all analytics data in parallel
      const [overviewRes, usersRes, contentRes, importsRes] = await Promise.all([
        fetch(`${baseUrl}/api/admin/analytics/overview?from=${getDateFromRange(dateRange)}`, { headers }),
        fetch(`${baseUrl}/api/admin/analytics/users?from=${getDateFromRange(dateRange)}`, { headers }),
        fetch(`${baseUrl}/api/admin/analytics/content?from=${getDateFromRange(dateRange)}`, { headers }),
        fetch(`${baseUrl}/api/admin/analytics/imports?from=${getDateFromRange(dateRange)}`, { headers })
      ])

      if (overviewRes.ok) {
        const overviewData = await overviewRes.json()
        setOverview(overviewData)
      }

      if (usersRes.ok) {
        const usersData = await usersRes.json()
        setUserAnalytics(usersData)
      }

      if (contentRes.ok) {
        const contentData = await contentRes.json()
        setContentAnalytics(contentData)
      }

      if (importsRes.ok) {
        const importsData = await importsRes.json()
        setImportAnalytics(importsData)
      }
    } catch (error) {
      console.error('Error fetching analytics:', error)
    } finally {
      setLoading(false)
    }
  }

  const getDateFromRange = (range: string) => {
    const now = new Date()
    const days = parseInt(range)
    const fromDate = new Date(now.getTime() - (days * 24 * 60 * 60 * 1000))
    return fromDate.toISOString().split('T')[0]
  }

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat().format(num)
  }

  const getPercentageChange = (current: number, previous: number) => {
    if (previous === 0) return current > 0 ? '+100%' : '0%'
    const change = ((current - previous) / previous) * 100
    return `${change >= 0 ? '+' : ''}${change.toFixed(1)}%`
  }

  // Chart data preparation
  const userGrowthData = [
    { name: 'Week 1', students: 45, teachers: 12 },
    { name: 'Week 2', students: 52, teachers: 15 },
    { name: 'Week 3', students: 48, teachers: 18 },
    { name: 'Week 4', students: 61, teachers: 22 },
  ]

  const contentDistributionData = contentAnalytics ? [
    { name: 'Boards', value: contentAnalytics.totalBoards, color: '#8884d8' },
    { name: 'Subjects', value: contentAnalytics.totalSubjects, color: '#82ca9d' },
    { name: 'Chapters', value: contentAnalytics.totalChapters, color: '#ffc658' },
    { name: 'Topics', value: contentAnalytics.totalTopics, color: '#ff7300' },
  ] : []

  const boardDistributionData = userAnalytics?.boardDistribution ? 
    Object.entries(userAnalytics.boardDistribution).map(([board, count]) => ({
      name: board,
      value: count
    })) : []

  if (loading) {
    return (
      <AdminLayoutSimple>
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <Activity className="h-8 w-8 animate-spin text-blue-600 mx-auto mb-2" />
            <p className="text-gray-600 dark:text-gray-400">Loading analytics...</p>
          </div>
        </div>
      </AdminLayoutSimple>
    )
  }

  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Analytics</h1>
            <p className="text-gray-600 dark:text-gray-400">Platform insights and performance metrics</p>
          </div>
          <div className="flex space-x-2">
            <Button 
              variant={dateRange === '7' ? 'default' : 'outline'}
              onClick={() => setDateRange('7')}
            >
              Last 7 days
            </Button>
            <Button 
              variant={dateRange === '30' ? 'default' : 'outline'}
              onClick={() => setDateRange('30')}
            >
              Last 30 days
            </Button>
            <Button 
              variant={dateRange === '90' ? 'default' : 'outline'}
              onClick={() => setDateRange('90')}
            >
              Last 90 days
            </Button>
          </div>
        </div>

        {/* KPI Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card className="p-6">
            <div className="flex items-center">
              <div className="p-2 bg-blue-100 dark:bg-blue-900 rounded-lg">
                <Users className="h-6 w-6 text-blue-600 dark:text-blue-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total Users</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {overview ? formatNumber(overview.totalStudents + overview.totalTeachers) : '0'}
                </p>
                <p className="text-xs text-green-600 dark:text-green-400">
                  +{overview ? (overview.newStudents + overview.newTeachers) : 0} new this period
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center">
              <div className="p-2 bg-green-100 dark:bg-green-900 rounded-lg">
                <BookOpen className="h-6 w-6 text-green-600 dark:text-green-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Content Items</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {overview ? formatNumber(overview.totalTopics) : '0'}
                </p>
                <p className="text-xs text-gray-600 dark:text-gray-400">
                  {overview ? formatNumber(overview.totalChapters) : '0'} chapters
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center">
              <div className="p-2 bg-yellow-100 dark:bg-yellow-900 rounded-lg">
                <Upload className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Import Success</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {overview && overview.totalImports > 0 
                    ? Math.round((overview.successfulImports / overview.totalImports) * 100)
                    : 0}%
                </p>
                <p className="text-xs text-gray-600 dark:text-gray-400">
                  {overview ? formatNumber(overview.successfulImports) : '0'} successful
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center">
              <div className="p-2 bg-purple-100 dark:bg-purple-900 rounded-lg">
                <Activity className="h-6 w-6 text-purple-600 dark:text-purple-400" />
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Active Users</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">
                  {overview ? formatNumber(overview.activeStudents + overview.activeTeachers) : '0'}
                </p>
                <p className="text-xs text-gray-600 dark:text-gray-400">
                  {overview ? Math.round(((overview.activeStudents + overview.activeTeachers) / (overview.totalStudents + overview.totalTeachers)) * 100) : 0}% of total
                </p>
              </div>
            </div>
          </Card>
        </div>

        {/* Analytics Tabs */}
        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="users">Users</TabsTrigger>
            <TabsTrigger value="content">Content</TabsTrigger>
            <TabsTrigger value="imports">Imports</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="space-y-6">
            {/* Charts */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* User Growth Chart */}
              <Card className="p-6">
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">User Growth</h3>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={userGrowthData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="students" stroke="#8884d8" name="Students" />
                        <Line type="monotone" dataKey="teachers" stroke="#82ca9d" name="Teachers" />
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </Card>

              {/* Content Distribution */}
              <Card className="p-6">
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Content Distribution</h3>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={contentDistributionData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                          outerRadius={80}
                          fill="#8884d8"
                          dataKey="value"
                        >
                          {contentDistributionData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={entry.color} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </Card>
            </div>

            {/* Overview Table */}
            <Card className="p-6">
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Key Metrics</h3>
                <div className="overflow-hidden border border-gray-200 dark:border-gray-700 rounded-lg">
                  <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-800">
                      <tr>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Metric
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Current
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          Active
                        </th>
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">
                          New This Period
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
                      <tr>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">
                          Students
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                          {overview ? formatNumber(overview.totalStudents) : '0'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                          {overview ? formatNumber(overview.activeStudents) : '0'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-green-600 dark:text-green-400">
                          +{overview ? formatNumber(overview.newStudents) : '0'}
                        </td>
                      </tr>
                      <tr>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900 dark:text-white">
                          Teachers
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                          {overview ? formatNumber(overview.totalTeachers) : '0'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                          {overview ? formatNumber(overview.activeTeachers) : '0'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-green-600 dark:text-green-400">
                          +{overview ? formatNumber(overview.newTeachers) : '0'}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </Card>
          </TabsContent>

          <TabsContent value="users" className="space-y-6">
            {/* User Analytics */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card className="p-6">
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Board Distribution</h3>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={boardDistributionData}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Bar dataKey="value" fill="#8884d8" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">User Statistics</h3>
                  <div className="space-y-4">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Total Students</span>
                      <span className="font-semibold">{userAnalytics ? formatNumber(userAnalytics.totalStudents) : '0'}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Active Students</span>
                      <span className="font-semibold">{userAnalytics ? formatNumber(userAnalytics.activeStudents) : '0'}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Total Teachers</span>
                      <span className="font-semibold">{userAnalytics ? formatNumber(userAnalytics.totalTeachers) : '0'}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600 dark:text-gray-400">Active Teachers</span>
                      <span className="font-semibold">{userAnalytics ? formatNumber(userAnalytics.activeTeachers) : '0'}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600 dark:text-gray-400">New Students</span>
                      <span className="font-semibold text-green-600">+{userAnalytics ? formatNumber(userAnalytics.newStudents) : '0'}</span>
                    </div>
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600 dark:text-gray-400">New Teachers</span>
                      <span className="font-semibold text-green-600">+{userAnalytics ? formatNumber(userAnalytics.newTeachers) : '0'}</span>
                    </div>
                  </div>
                </div>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="content" className="space-y-6">
            {/* Content Analytics */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-blue-100 dark:bg-blue-900 rounded-lg">
                    <BookOpen className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Boards</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {contentAnalytics ? formatNumber(contentAnalytics.totalBoards) : '0'}
                    </p>
                    <p className="text-xs text-gray-600 dark:text-gray-400">
                      {contentAnalytics ? formatNumber(contentAnalytics.activeBoards) : '0'} active
                    </p>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-green-100 dark:bg-green-900 rounded-lg">
                    <GraduationCap className="h-6 w-6 text-green-600 dark:text-green-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Subjects</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {contentAnalytics ? formatNumber(contentAnalytics.totalSubjects) : '0'}
                    </p>
                    <p className="text-xs text-gray-600 dark:text-gray-400">
                      {contentAnalytics ? formatNumber(contentAnalytics.activeSubjects) : '0'} active
                    </p>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-yellow-100 dark:bg-yellow-900 rounded-lg">
                    <FileText className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Chapters</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {contentAnalytics ? formatNumber(contentAnalytics.totalChapters) : '0'}
                    </p>
                    <p className="text-xs text-gray-600 dark:text-gray-400">
                      {contentAnalytics ? formatNumber(contentAnalytics.activeChapters) : '0'} active
                    </p>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-purple-100 dark:bg-purple-900 rounded-lg">
                    <BookOpen className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Topics</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {contentAnalytics ? formatNumber(contentAnalytics.totalTopics) : '0'}
                    </p>
                    <p className="text-xs text-gray-600 dark:text-gray-400">
                      {contentAnalytics ? formatNumber(contentAnalytics.activeTopics) : '0'} active
                    </p>
                  </div>
                </div>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="imports" className="space-y-6">
            {/* Import Analytics */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-blue-100 dark:bg-blue-900 rounded-lg">
                    <Upload className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Total</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {importAnalytics ? formatNumber(importAnalytics.totalImports) : '0'}
                    </p>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-green-100 dark:bg-green-900 rounded-lg">
                    <CheckCircle className="h-6 w-6 text-green-600 dark:text-green-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Successful</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {importAnalytics ? formatNumber(importAnalytics.successfulImports) : '0'}
                    </p>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-red-100 dark:bg-red-900 rounded-lg">
                    <AlertCircle className="h-6 w-6 text-red-600 dark:text-red-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Failed</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {importAnalytics ? formatNumber(importAnalytics.failedImports) : '0'}
                    </p>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-yellow-100 dark:bg-yellow-900 rounded-lg">
                    <Activity className="h-6 w-6 text-yellow-600 dark:text-yellow-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Pending</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {importAnalytics ? formatNumber(importAnalytics.pendingImports) : '0'}
                    </p>
                  </div>
                </div>
              </Card>

              <Card className="p-6">
                <div className="flex items-center">
                  <div className="p-2 bg-purple-100 dark:bg-purple-900 rounded-lg">
                    <TrendingUp className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                  </div>
                  <div className="ml-4">
                    <p className="text-sm font-medium text-gray-600 dark:text-gray-400">Running</p>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">
                      {importAnalytics ? formatNumber(importAnalytics.runningImports) : '0'}
                    </p>
                  </div>
                </div>
              </Card>
            </div>

            {/* Success Rate Chart */}
            <Card className="p-6">
              <div className="space-y-4">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Import Success Rate</h3>
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={[
                      { name: 'Successful', value: importAnalytics?.successfulImports || 0, color: '#10b981' },
                      { name: 'Failed', value: importAnalytics?.failedImports || 0, color: '#ef4444' },
                      { name: 'Pending', value: importAnalytics?.pendingImports || 0, color: '#f59e0b' },
                      { name: 'Running', value: importAnalytics?.runningImports || 0, color: '#8b5cf6' }
                    ]}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Bar dataKey="value" fill="#8884d8" />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </AdminLayoutSimple>
  )
}
