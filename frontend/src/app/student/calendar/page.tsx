'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useAuthStore } from '@/store/auth'
import { StudentRoute } from '@/components/route-guard'
import { Calendar, Clock, User, ArrowLeft, ArrowRight, Plus, MoreHorizontal } from 'lucide-react'

interface CalendarEvent {
  id: number
  title: string
  start: string
  end: string
  status: string
  color: string
  teacherName: string
  topicTitle: string
  canReschedule: boolean
  canCancel: boolean
  canJoin: boolean
}

export default function StudentCalendarPage() {
  const [currentDate, setCurrentDate] = useState(new Date())
  const [view, setView] = useState<'day' | 'week' | 'month'>('month')
  const [events, setEvents] = useState<CalendarEvent[]>([])
  const [selectedEvent, setSelectedEvent] = useState<CalendarEvent | null>(null)
  const [loading, setLoading] = useState(true)
  
  const router = useRouter()
  const { user } = useAuthStore()

  useEffect(() => {
    if (!user) {
      router.push('/login')
      return
    }
    loadCalendarEvents()
  }, [user, router, currentDate])

  const loadCalendarEvents = async () => {
    try {
      // TODO: Replace with actual API call
      // const eventsData = await studentAPI.getCalendarEvents(
      //   getStartOfPeriod(),
      //   getEndOfPeriod()
      // )
      
      // Mock data for now
      const mockEvents: CalendarEvent[] = [
        {
          id: 1,
          title: 'Quadratic Equations',
          start: '2024-01-15T14:00:00Z',
          end: '2024-01-15T15:00:00Z',
          status: 'ACCEPTED',
          color: '#10B981',
          teacherName: 'Dr. Smith',
          topicTitle: 'Quadratic Equations',
          canReschedule: true,
          canCancel: true,
          canJoin: false
        },
        {
          id: 2,
          title: 'Trigonometry Basics',
          start: '2024-01-16T10:00:00Z',
          end: '2024-01-16T11:30:00Z',
          status: 'REQUESTED',
          color: '#FACC15',
          teacherName: 'Ms. Johnson',
          topicTitle: 'Trigonometry Basics',
          canReschedule: false,
          canCancel: true,
          canJoin: false
        },
        {
          id: 3,
          title: 'Chemical Bonding',
          start: '2024-01-17T16:00:00Z',
          end: '2024-01-17T17:15:00Z',
          status: 'ACCEPTED',
          color: '#10B981',
          teacherName: 'Prof. Brown',
          topicTitle: 'Chemical Bonding',
          canReschedule: true,
          canCancel: true,
          canJoin: true
        }
      ]
      
      setEvents(mockEvents)
    } catch (error) {
      console.error('Failed to load calendar events:', error)
    } finally {
      setLoading(false)
    }
  }

  const getStartOfPeriod = () => {
    const date = new Date(currentDate)
    switch (view) {
      case 'day':
        date.setHours(0, 0, 0, 0)
        break
      case 'week':
        const day = date.getDay()
        date.setDate(date.getDate() - day)
        date.setHours(0, 0, 0, 0)
        break
      case 'month':
        date.setDate(1)
        date.setHours(0, 0, 0, 0)
        break
    }
    return date.toISOString()
  }

  const getEndOfPeriod = () => {
    const date = new Date(currentDate)
    switch (view) {
      case 'day':
        date.setHours(23, 59, 59, 999)
        break
      case 'week':
        const day = date.getDay()
        date.setDate(date.getDate() + (6 - day))
        date.setHours(23, 59, 59, 999)
        break
      case 'month':
        date.setMonth(date.getMonth() + 1)
        date.setDate(0)
        date.setHours(23, 59, 59, 999)
        break
    }
    return date.toISOString()
  }

  const navigateDate = (direction: 'prev' | 'next') => {
    const newDate = new Date(currentDate)
    switch (view) {
      case 'day':
        newDate.setDate(newDate.getDate() + (direction === 'next' ? 1 : -1))
        break
      case 'week':
        newDate.setDate(newDate.getDate() + (direction === 'next' ? 7 : -7))
        break
      case 'month':
        newDate.setMonth(newDate.getMonth() + (direction === 'next' ? 1 : -1))
        break
    }
    setCurrentDate(newDate)
  }

  const formatTime = (timeString: string) => {
    return new Date(timeString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  const formatDate = (timeString: string) => {
    return new Date(timeString).toLocaleDateString()
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
        return 'bg-emerald-100 text-emerald-800'
      case 'REQUESTED':
        return 'bg-yellow-100 text-yellow-800'
      case 'RESCHEDULED':
        return 'bg-blue-100 text-blue-800'
      case 'CANCELLED':
        return 'bg-red-100 text-red-800'
      case 'COMPLETED':
        return 'bg-gray-100 text-gray-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const renderDayView = () => {
    const dayEvents = events.filter(event => {
      const eventDate = new Date(event.start)
      return eventDate.toDateString() === currentDate.toDateString()
    })

    return (
      <div className="space-y-4">
        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold">{currentDate.toLocaleDateString('en-US', { 
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
          })}</h2>
        </div>
        
        {dayEvents.length > 0 ? (
          <div className="space-y-3">
            {dayEvents.map((event) => (
              <Card key={event.id} className="cursor-pointer hover:shadow-md transition-shadow">
                <CardContent className="p-4">
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <h3 className="font-semibold text-lg">{event.title}</h3>
                      <p className="text-gray-600 mb-2">with {event.teacherName}</p>
                      <div className="flex items-center space-x-4 text-sm text-gray-500">
                        <div className="flex items-center space-x-1">
                          <Clock className="h-4 w-4" />
                          <span>{formatTime(event.start)} - {formatTime(event.end)}</span>
                        </div>
                        <Badge className={getStatusColor(event.status)}>
                          {event.status}
                        </Badge>
                      </div>
                    </div>
                    <div className="flex space-x-2">
                      {event.canJoin && (
                        <Button size="sm" className="bg-emerald-600 hover:bg-emerald-700">
                          Join
                        </Button>
                      )}
                      <Button size="sm" variant="outline">
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <div className="text-center py-12 text-gray-500">
            <Calendar className="h-12 w-12 mx-auto mb-4 text-gray-300" />
            <p>No classes scheduled for this day</p>
            <Button 
              className="mt-4 bg-emerald-600 hover:bg-emerald-700"
              onClick={() => router.push('/student/booking')}
            >
              Book a Class
            </Button>
          </div>
        )}
      </div>
    )
  }

  const renderWeekView = () => {
    const startOfWeek = new Date(currentDate)
    const day = startOfWeek.getDay()
    startOfWeek.setDate(startOfWeek.getDate() - day)
    
    const days = Array.from({ length: 7 }, (_, i) => {
      const date = new Date(startOfWeek)
      date.setDate(startOfWeek.getDate() + i)
      return date
    })

    return (
      <div className="space-y-4">
        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold">
            {startOfWeek.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
          </h2>
        </div>
        
        <div className="grid grid-cols-7 gap-4">
          {days.map((day, index) => {
            const dayEvents = events.filter(event => {
              const eventDate = new Date(event.start)
              return eventDate.toDateString() === day.toDateString()
            })

            return (
              <div key={index} className="border rounded-lg p-3">
                <div className="text-center mb-3">
                  <div className="text-sm font-medium text-gray-600">
                    {day.toLocaleDateString('en-US', { weekday: 'short' })}
                  </div>
                  <div className={`text-lg font-bold ${
                    day.toDateString() === new Date().toDateString() 
                      ? 'text-emerald-600' 
                      : 'text-gray-900'
                  }`}>
                    {day.getDate()}
                  </div>
                </div>
                
                <div className="space-y-2">
                  {dayEvents.map((event) => (
                    <div 
                      key={event.id}
                      className="text-xs p-2 rounded cursor-pointer hover:bg-gray-50"
                      style={{ backgroundColor: event.color + '20', borderLeft: `3px solid ${event.color}` }}
                      onClick={() => setSelectedEvent(event)}
                    >
                      <div className="font-medium truncate">{event.title}</div>
                      <div className="text-gray-600">{formatTime(event.start)}</div>
                    </div>
                  ))}
                </div>
              </div>
            )
          })}
        </div>
      </div>
    )
  }

  const renderMonthView = () => {
    const year: number = currentDate.getFullYear()
    const month: number = currentDate.getMonth()
    
    const firstDay = new Date(year, month, 1)
    const lastDay = new Date(year, month + 1, 0)
    const startDate = new Date(firstDay)
    startDate.setDate(startDate.getDate() - firstDay.getDay())
    
    const weeks = []
    let currentWeek = []
    let date = new Date(startDate)
    
    for (let i = 0; i < 42; i++) {
      currentWeek.push(new Date(date))
      date.setDate(date.getDate() + 1)
      
      if (currentWeek.length === 7) {
        weeks.push([...currentWeek])
        currentWeek = []
      }
    }

    return (
      <div className="space-y-4">
        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold">
            {currentDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
          </h2>
        </div>
        
        <div className="grid grid-cols-7 gap-1">
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day) => (
            <div key={day} className="p-2 text-center text-sm font-medium text-gray-600">
              {day}
            </div>
          ))}
          
          {weeks.map((week, weekIndex) =>
            week.map((day, dayIndex) => {
              const dayEvents = events.filter(event => {
                const eventDate = new Date(event.start)
                return eventDate.toDateString() === day.toDateString()
              })

              return (
                <div 
                  key={`${weekIndex}-${dayIndex}`}
                  className={`min-h-[100px] p-2 border border-gray-200 ${
                    day.getMonth() !== month ? 'bg-gray-50 text-gray-400' : 'bg-white'
                  } ${
                    day.toDateString() === new Date().toDateString() 
                      ? 'bg-emerald-50 border-emerald-200' 
                      : ''
                  }`}
                >
                  <div className="text-sm font-medium mb-1">{day.getDate()}</div>
                  <div className="space-y-1">
                    {dayEvents.slice(0, 3).map((event) => (
                      <div 
                        key={event.id}
                        className="text-xs p-1 rounded cursor-pointer hover:bg-gray-100"
                        style={{ backgroundColor: event.color + '20' }}
                        onClick={() => setSelectedEvent(event)}
                      >
                        <div className="truncate">{event.title}</div>
                        <div className="text-gray-600">{formatTime(event.start)}</div>
                      </div>
                    ))}
                    {dayEvents.length > 3 && (
                      <div className="text-xs text-gray-500">
                        +{dayEvents.length - 3} more
                      </div>
                    )}
                  </div>
                </div>
              )
            })
          )}
        </div>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading calendar...</div>
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
              <h1 className="text-3xl font-bold text-gray-900">Calendar</h1>
              <p className="text-gray-600">View and manage your classes</p>
            </div>
            <div className="flex space-x-4">
              <Button 
                className="bg-emerald-600 hover:bg-emerald-700"
                onClick={() => router.push('/student/booking')}
              >
                <Plus className="h-4 w-4 mr-2" />
                Book Class
              </Button>
            </div>
          </div>

          {/* Calendar Controls */}
          <Card className="mb-6">
            <CardContent className="p-4">
              <div className="flex justify-between items-center">
                <div className="flex items-center space-x-4">
                  <Select value={view} onValueChange={(value: any) => setView(value)}>
                    <SelectTrigger className="w-32">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="day">Day</SelectItem>
                      <SelectItem value="week">Week</SelectItem>
                      <SelectItem value="month">Month</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                
                <div className="flex items-center space-x-2">
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => navigateDate('prev')}
                  >
                    <ArrowLeft className="h-4 w-4" />
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => setCurrentDate(new Date())}
                  >
                    Today
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => navigateDate('next')}
                  >
                    <ArrowRight className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Calendar View */}
          <Card>
            <CardContent className="p-6">
              {view === 'day' && renderDayView()}
              {view === 'week' && renderWeekView()}
              {view === 'month' && renderMonthView()}
            </CardContent>
          </Card>

          {/* Event Details Modal */}
          {selectedEvent && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <Card className="w-full max-w-md mx-4">
                <CardHeader>
                  <CardTitle>{selectedEvent.title}</CardTitle>
                  <CardDescription>Class Details</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <User className="h-4 w-4 text-gray-400" />
                    <span className="text-sm">Teacher: {selectedEvent.teacherName}</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Clock className="h-4 w-4 text-gray-400" />
                    <span className="text-sm">
                      {formatDate(selectedEvent.start)} at {formatTime(selectedEvent.start)} - {formatTime(selectedEvent.end)}
                    </span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Badge className={getStatusColor(selectedEvent.status)}>
                      {selectedEvent.status}
                    </Badge>
                  </div>
                  
                  <div className="flex space-x-2 pt-4">
                    {selectedEvent.canJoin && (
                      <Button className="bg-emerald-600 hover:bg-emerald-700">
                        Join Class
                      </Button>
                    )}
                    {selectedEvent.canReschedule && (
                      <Button variant="outline">
                        Reschedule
                      </Button>
                    )}
                    {selectedEvent.canCancel && (
                      <Button variant="outline">
                        Cancel
                      </Button>
                    )}
                    <Button 
                      variant="outline" 
                      onClick={() => setSelectedEvent(null)}
                    >
                      Close
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}
        </div>
      </div>
    </StudentRoute>
  )
}
