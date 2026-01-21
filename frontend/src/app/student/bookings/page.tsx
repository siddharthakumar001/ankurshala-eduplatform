"use client"

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { StudentRoute } from '@/components/route-guard'
import { bookingService, BookingResponse } from '@/services/bookingService'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { toast } from 'sonner'
import {
  Calendar,
  Clock,
  Video,
  ArrowUpRight,
  Loader2,
  History,
  PlusCircle
} from 'lucide-react'

export default function StudentBookingsPage() {
  return (
    <StudentRoute>
      <BookingsContent />
    </StudentRoute>
  )
}

function BookingsContent() {
  const router = useRouter()
  const [loading, setLoading] = useState(true)
  const [upcomingBookings, setUpcomingBookings] = useState<BookingResponse[]>([])
  const [recentHistory, setRecentHistory] = useState<BookingResponse[]>([])
  const [joiningId, setJoiningId] = useState<number | null>(null)

  useEffect(() => {
    const loadBookings = async () => {
      try {
        setLoading(true)
        const [upcoming, history] = await Promise.all([
          bookingService.getUpcomingBookings(),
          bookingService.getBookingHistory(0, 5)
        ])
        setUpcomingBookings(upcoming || [])
        setRecentHistory(history.content || [])
      } catch (error) {
        console.error('Failed to load bookings:', error)
        toast.error('Failed to load bookings')
      } finally {
        setLoading(false)
      }
    }

    loadBookings()
  }, [])

  const formatDateTime = (startTime: string, endTime: string) => {
    const start = new Date(startTime)
    const end = new Date(endTime)
    return `${start.toLocaleDateString('en-IN', { month: 'short', day: 'numeric' })} · ${start.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - ${end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
      case 'CONFIRMED':
        return <Badge className="badge-success">Confirmed</Badge>
      case 'PENDING':
        return <Badge className="badge-warning">Pending</Badge>
      case 'IN_PROGRESS':
        return <Badge className="badge-info">In Progress</Badge>
      case 'COMPLETED':
        return <Badge className="badge-success">Completed</Badge>
      case 'CANCELLED':
        return <Badge className="badge-error">Cancelled</Badge>
      default:
        return <Badge variant="outline" className="border-white/40 bg-white/70 text-slate-700">{status}</Badge>
    }
  }

  const handleJoin = async (bookingId: number) => {
    try {
      setJoiningId(bookingId)
      const response = await bookingService.joinSession(bookingId)
      if (response.sessionUrl) {
        window.open(response.sessionUrl, '_blank')
      } else {
        toast.message('Session details received', { description: response.sessionId })
      }
    } catch (error) {
      console.error('Failed to join session:', error)
      toast.error('Failed to join session')
    } finally {
      setJoiningId(null)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-transparent py-8">
        <div className="max-w-6xl mx-auto px-4">
          <div className="flex items-center justify-center h-64">
            <div className="glass-panel rounded-2xl border border-white/40 px-6 py-4 flex items-center">
              <Loader2 className="h-6 w-6 animate-spin text-ankur-primary" />
              <span className="ml-2 text-slate-600 dark:text-slate-200">Loading bookings...</span>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-transparent py-8">
      <div className="max-w-6xl mx-auto px-4 space-y-6">
          <div className="page-header">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
              <div>
                <h1 className="text-3xl font-bold text-white">My Bookings</h1>
                <p className="text-white/80">Track upcoming classes and recent activity</p>
              </div>
              <div className="flex flex-wrap gap-2">
                <Button className="btn-primary" onClick={() => router.push('/student/booking')}>
                  <PlusCircle className="h-4 w-4 mr-2" />
                  New Booking
                </Button>
                <Button variant="outline" className="btn-outline" onClick={() => router.push('/student/calendar')}>
                  <Calendar className="h-4 w-4 mr-2" />
                  Calendar
                </Button>
                <Button variant="outline" className="btn-outline" onClick={() => router.push('/student/history')}>
                  <History className="h-4 w-4 mr-2" />
                  History
                </Button>
              </div>
            </div>
          </div>

          <Card className="glass-panel border border-white/40">
            <CardHeader>
              <CardTitle>Upcoming Classes</CardTitle>
              <CardDescription>Your next sessions and requests</CardDescription>
            </CardHeader>
            <CardContent>
              {upcomingBookings.length === 0 ? (
                <div className="text-center py-10">
                  <Calendar className="h-12 w-12 text-slate-300 mx-auto mb-4" />
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">No upcoming classes</h3>
                  <p className="text-slate-500 dark:text-slate-300">
                    Book a session to get started.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {upcomingBookings.map((booking) => (
                    <div key={booking.id} className="glass rounded-2xl p-4 border border-white/30">
                      <div className="flex items-center justify-between mb-2">
                        <h3 className="font-semibold text-slate-900 dark:text-white">{booking.topicTitle}</h3>
                        {getStatusBadge(booking.status)}
                      </div>
                      <p className="text-sm text-slate-600 dark:text-slate-300 mb-2">
                        {booking.teacherName || 'Teacher matching in progress'}
                      </p>
                      <div className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-300">
                        <span className="flex items-center gap-1">
                          <Clock className="h-4 w-4" />
                          {formatDateTime(booking.startTime, booking.endTime)}
                        </span>
                      </div>
                      <div className="flex flex-wrap gap-2 mt-4">
                        <Button
                          variant="outline"
                          className="btn-outline"
                          onClick={() => router.push(`/student/bookings/${booking.id}`)}
                        >
                          View Details
                          <ArrowUpRight className="h-4 w-4 ml-2" />
                        </Button>
                        {(booking.status === 'ACCEPTED' || booking.status === 'CONFIRMED') && (
                          <Button
                            className="btn-primary"
                            onClick={() => handleJoin(booking.id)}
                            disabled={joiningId === booking.id}
                          >
                            {joiningId === booking.id ? (
                              <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                              <>
                                <Video className="h-4 w-4 mr-2" />
                                Join Session
                              </>
                            )}
                          </Button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card className="glass-panel border border-white/40">
            <CardHeader>
              <CardTitle>Recent History</CardTitle>
              <CardDescription>Most recent completed or past sessions</CardDescription>
            </CardHeader>
            <CardContent>
              {recentHistory.length === 0 ? (
                <p className="text-slate-500 dark:text-slate-300 text-center py-6">
                  No previous bookings yet.
                </p>
              ) : (
                <div className="space-y-3">
                  {recentHistory.map((booking) => (
                    <div key={booking.id} className="glass rounded-2xl p-4 border border-white/30 flex items-center justify-between">
                      <div>
                        <p className="font-medium text-slate-900 dark:text-white">{booking.topicTitle}</p>
                        <p className="text-sm text-slate-600 dark:text-slate-300">
                          {formatDateTime(booking.startTime, booking.endTime)}
                        </p>
                      </div>
                      <Button
                        variant="outline"
                        className="btn-outline"
                        onClick={() => router.push(`/student/bookings/${booking.id}`)}
                      >
                        View
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
      </div>
    </div>
  )
}
