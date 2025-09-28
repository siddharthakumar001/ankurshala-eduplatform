'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { useAuthStore } from '@/store/auth'
import { StudentRoute } from '@/components/route-guard'
import { Calendar, Clock, BookOpen, TrendingUp, Bell, Plus } from 'lucide-react'

interface DashboardStats {
  upcomingBookings: number
  completedBookings: number
  totalTimeSpent: number
  nextClassTime?: string
  nextClassTopic?: string
  nextClassTeacher?: string
}

interface UpcomingBooking {
  id: number
  topicTitle: string
  teacherName: string
  startTime: string
  endTime: string
  status: string
  canJoin: boolean
}

export default function StudentDashboard() {
  const [stats, setStats] = useState<DashboardStats>({
    upcomingBookings: 0,
    completedBookings: 0,
    totalTimeSpent: 0
  })
  const [upcomingBookings, setUpcomingBookings] = useState<UpcomingBooking[]>([])
  const [loading, setLoading] = useState(true)
  
  const router = useRouter()
  const { user, logout } = useAuthStore()

  useEffect(() => {
    if (!user) {
      router.push('/login')
      return
    }
    loadDashboardData()
  }, [user, router])

  const loadDashboardData = async () => {
    try {
      // TODO: Replace with actual API calls
      // const statsData = await studentAPI.getDashboardStats()
      // const upcomingData = await studentAPI.getUpcomingBookings()
      
      // Mock data for now
      setStats({
        upcomingBookings: 3,
        completedBookings: 12,
        totalTimeSpent: 24,
        nextClassTime: '2024-01-15T14:00:00Z',
        nextClassTopic: 'Quadratic Equations',
        nextClassTeacher: 'Dr. Smith'
      })
      
      setUpcomingBookings([
        {
          id: 1,
          topicTitle: 'Quadratic Equations',
          teacherName: 'Dr. Smith',
          startTime: '2024-01-15T14:00:00Z',
          endTime: '2024-01-15T15:00:00Z',
          status: 'ACCEPTED',
          canJoin: false
        },
        {
          id: 2,
          topicTitle: 'Trigonometry Basics',
          teacherName: 'Ms. Johnson',
          startTime: '2024-01-16T10:00:00Z',
          endTime: '2024-01-16T11:00:00Z',
          status: 'REQUESTED',
          canJoin: false
        }
      ])
    } catch (error) {
      console.error('Failed to load dashboard data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = async () => {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        // await authAPI.logout(refreshToken)
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      logout()
      router.push('/login')
    }
  }

  const formatTime = (timeString: string) => {
    return new Date(timeString).toLocaleString()
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
        return 'bg-emerald-100 text-emerald-800'
      case 'REQUESTED':
        return 'bg-yellow-100 text-yellow-800'
      case 'RESCHEDULED':
        return 'bg-blue-100 text-blue-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading dashboard...</div>
      </div>
    )
  }

  return (
    <StudentRoute>
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="mb-8 flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Student Dashboard</h1>
              <p className="text-gray-600">Welcome back, {user?.name}</p>
            </div>
            <div className="flex space-x-4">
              <Button 
                variant="outline" 
                onClick={() => router.push('/student/booking')}
                className="flex items-center space-x-2"
              >
                <Plus className="h-4 w-4" />
                <span>Book Class</span>
              </Button>
              <Button variant="outline" onClick={handleLogout}>
                Logout
              </Button>
            </div>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Upcoming Classes</CardTitle>
                <Calendar className="h-4 w-4 text-emerald-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-emerald-600">{stats.upcomingBookings}</div>
                <p className="text-xs text-muted-foreground">Classes scheduled</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Completed Classes</CardTitle>
                <BookOpen className="h-4 w-4 text-blue-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-blue-600">{stats.completedBookings}</div>
                <p className="text-xs text-muted-foreground">Total completed</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Time Spent</CardTitle>
                <Clock className="h-4 w-4 text-purple-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-purple-600">{stats.totalTimeSpent}h</div>
                <p className="text-xs text-muted-foreground">Total study time</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Progress</CardTitle>
                <TrendingUp className="h-4 w-4 text-orange-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-orange-600">85%</div>
                <p className="text-xs text-muted-foreground">Course completion</p>
              </CardContent>
            </Card>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Next Class Card */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Bell className="h-5 w-5 text-emerald-600" />
                  <span>Next Class</span>
                </CardTitle>
                <CardDescription>Your upcoming session</CardDescription>
              </CardHeader>
              <CardContent>
                {stats.nextClassTime ? (
                  <div className="space-y-4">
                    <div>
                      <h3 className="font-semibold text-lg">{stats.nextClassTopic}</h3>
                      <p className="text-gray-600">with {stats.nextClassTeacher}</p>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-500">
                      <Clock className="h-4 w-4" />
                      <span>{formatTime(stats.nextClassTime)}</span>
                    </div>
                    <div className="flex space-x-2">
                      <Button size="sm" className="bg-emerald-600 hover:bg-emerald-700">
                        Join Class
                      </Button>
                      <Button size="sm" variant="outline">
                        View Details
                      </Button>
                    </div>
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <Calendar className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                    <p>No upcoming classes</p>
                    <p className="text-sm">Book your first class to get started</p>
                    <Button 
                      className="mt-4 bg-emerald-600 hover:bg-emerald-700"
                      onClick={() => router.push('/student/booking')}
                    >
                      Book Class
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Upcoming Bookings */}
            <Card>
              <CardHeader>
                <CardTitle>Upcoming Classes</CardTitle>
                <CardDescription>Your scheduled sessions</CardDescription>
              </CardHeader>
              <CardContent>
                {upcomingBookings.length > 0 ? (
                  <div className="space-y-4">
                    {upcomingBookings.map((booking) => (
                      <div key={booking.id} className="border rounded-lg p-4">
                        <div className="flex justify-between items-start mb-2">
                          <h4 className="font-medium">{booking.topicTitle}</h4>
                          <Badge className={getStatusColor(booking.status)}>
                            {booking.status}
                          </Badge>
                        </div>
                        <p className="text-sm text-gray-600 mb-2">with {booking.teacherName}</p>
                        <div className="flex items-center space-x-2 text-sm text-gray-500 mb-3">
                          <Clock className="h-4 w-4" />
                          <span>{formatTime(booking.startTime)} - {formatTime(booking.endTime)}</span>
                        </div>
                        <div className="flex space-x-2">
                          {booking.canJoin && (
                            <Button size="sm" className="bg-emerald-600 hover:bg-emerald-700">
                              Join Now
                            </Button>
                          )}
                          <Button size="sm" variant="outline">
                            View Details
                          </Button>
                        </div>
                      </div>
                    ))}
                    <Button 
                      variant="outline" 
                      className="w-full"
                      onClick={() => router.push('/student/calendar')}
                    >
                      View All Classes
                    </Button>
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <Calendar className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                    <p>No upcoming classes</p>
                    <p className="text-sm">Book your first class to get started</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Quick Actions */}
          <Card className="mt-8">
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
              <CardDescription>Common tasks and navigation</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Button 
                  variant="outline" 
                  className="h-20 flex flex-col items-center justify-center space-y-2"
                  onClick={() => router.push('/student/booking')}
                >
                  <Plus className="h-6 w-6" />
                  <span>Book New Class</span>
                </Button>
                <Button 
                  variant="outline" 
                  className="h-20 flex flex-col items-center justify-center space-y-2"
                  onClick={() => router.push('/student/calendar')}
                >
                  <Calendar className="h-6 w-6" />
                  <span>View Calendar</span>
                </Button>
                <Button 
                  variant="outline" 
                  className="h-20 flex flex-col items-center justify-center space-y-2"
                  onClick={() => router.push('/student/history')}
                >
                  <BookOpen className="h-6 w-6" />
                  <span>Study History</span>
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </StudentRoute>
  )
}
