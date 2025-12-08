'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { 
  Calendar, 
  Clock, 
  User, 
  Video,
  MessageSquare,
  AlertCircle,
  CheckCircle,
  XCircle,
  MoreHorizontal,
  Loader2,
  BookOpen
} from 'lucide-react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { bookingService, CalendarEvent } from '@/services/bookingService';
import { toast } from 'sonner';
import { StudentRoute } from '@/components/route-guard';

export default function StudentCalendarPage() {
  const router = useRouter();
  const [bookings, setBookings] = useState<CalendarEvent[]>([]);
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [isLoading, setIsLoading] = useState(true);
  const [cancelling, setCancelling] = useState<number | null>(null);

  useEffect(() => {
    loadCalendarEvents();
  }, []);

  const loadCalendarEvents = async () => {
    try {
      setIsLoading(true);
      // Load calendar for current month
      const startOfMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
      const endOfMonth = new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0);
      
      const fromDate = startOfMonth.toISOString().split('T')[0];
      const toDate = endOfMonth.toISOString().split('T')[0];
      
      const events = await bookingService.getCalendarEvents(fromDate, toDate);
      setBookings(events);
    } catch (error) {
      console.error('Failed to load calendar events:', error);
      toast.error('Failed to load calendar events');
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'REQUESTED':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'COMPLETED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'DECLINED':
        return 'bg-red-100 text-red-800 border-red-200';
      case 'CANCELLED':
        return 'bg-gray-100 text-gray-800 border-gray-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
        return <CheckCircle className="h-4 w-4" />;
      case 'REQUESTED':
        return <Clock className="h-4 w-4" />;
      case 'COMPLETED':
        return <CheckCircle className="h-4 w-4" />;
      case 'DECLINED':
        return <XCircle className="h-4 w-4" />;
      case 'CANCELLED':
        return <XCircle className="h-4 w-4" />;
      default:
        return <AlertCircle className="h-4 w-4" />;
    }
  };

  const getBookingsForDate = (date: string) => {
    return bookings.filter(booking => 
      booking.start.startsWith(date)
    );
  };

  const handleJoinClass = (booking: CalendarEvent) => {
    // Navigate to booking details page
    router.push(`/student/bookings/${booking.id}`);
  };

  const handleReschedule = (booking: CalendarEvent) => {
    router.push(`/student/booking?reschedule=${booking.id}`);
  };

  const handleCancel = async (booking: CalendarEvent) => {
    const reason = prompt(`Please provide a reason for cancelling "${booking.topicTitle || booking.title}":`);
    if (!reason) {
      return;
    }

    try {
      setCancelling(booking.id);
      await bookingService.cancelBooking(booking.id, { reason });
      toast.success('Booking cancelled successfully');
      loadCalendarEvents(); // Refresh the calendar
    } catch (error: any) {
      console.error('Failed to cancel booking:', error);
      toast.error(error.response?.data?.message || 'Failed to cancel booking');
    } finally {
      setCancelling(null);
    }
  };

  const handleAddNotes = (booking: CalendarEvent) => {
    router.push(`/student/bookings/${booking.id}`);
  };

  const selectedDateBookings = getBookingsForDate(selectedDate);

  if (isLoading) {
    return (
      <StudentRoute>
        <div className="flex items-center justify-center min-h-screen">
          <Loader2 className="h-8 w-8 animate-spin text-emerald-600" />
        </div>
      </StudentRoute>
    );
  }

  return (
    <StudentRoute>
      <div className="p-6 space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">My Calendar</h1>
            <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">VIEW AND MANAGE YOUR SCHEDULED CLASSES</p>
          </div>
          <Button 
            onClick={() => router.push('/student/booking')}
            className="bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 text-white shadow-md"
          >
            <BookOpen className="h-4 w-4 mr-2" />
            Book New Class
          </Button>
        </div>

      {/* Calendar Navigation */}
      <Card className="rounded-2xl border-gray-100 dark:border-gray-800 shadow-sm">
        <CardHeader className="border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <CardTitle className="flex items-center text-base">
            <Calendar className="h-5 w-5 mr-2 text-emerald-600" />
            Calendar View
          </CardTitle>
          <CardDescription>Select a date to view your classes</CardDescription>
        </CardHeader>
        <CardContent className="p-6">
          <div className="flex items-center space-x-4">
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500 dark:bg-gray-800 dark:text-white"
            />
            <div className="text-sm text-gray-600 dark:text-gray-400">
              {selectedDateBookings.length} class{selectedDateBookings.length !== 1 ? 'es' : ''} on{' '}
              {new Date(selectedDate).toLocaleDateString('en-US', { 
                weekday: 'long', 
                year: 'numeric', 
                month: 'long', 
                day: 'numeric' 
              })}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Bookings for Selected Date */}
      {selectedDateBookings.length === 0 ? (
        <Card className="rounded-2xl border-gray-100 dark:border-gray-800 shadow-sm">
          <CardContent className="p-12 text-center">
            <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center">
              <Calendar className="h-8 w-8 text-gray-400" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">No Classes Scheduled</h3>
            <p className="text-gray-600 dark:text-gray-400 mb-4">
              You don&apos;t have any classes scheduled for this date.
            </p>
            <Button 
              onClick={() => router.push('/student/booking')}
              className="bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600"
            >
              Book a Class
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {selectedDateBookings.map((booking) => (
            <Card key={booking.id} className="rounded-2xl border-gray-100 dark:border-gray-800 shadow-sm hover:shadow-md transition-shadow">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">{booking.topicTitle || booking.title}</CardTitle>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="sm">
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      {booking.status === 'ACCEPTED' && (
                        <DropdownMenuItem onClick={() => handleJoinClass(booking)}>
                          <Video className="h-4 w-4 mr-2" />
                          View Details
                        </DropdownMenuItem>
                      )}
                      {(booking.status === 'REQUESTED' || booking.status === 'ACCEPTED') && (
                        <>
                          <DropdownMenuItem onClick={() => handleReschedule(booking)}>
                            <Calendar className="h-4 w-4 mr-2" />
                            Reschedule
                          </DropdownMenuItem>
                          <DropdownMenuItem 
                            onClick={() => handleCancel(booking)}
                            disabled={cancelling === booking.id}
                          >
                            <XCircle className="h-4 w-4 mr-2" />
                            {cancelling === booking.id ? 'Cancelling...' : 'Cancel'}
                          </DropdownMenuItem>
                        </>
                      )}
                      <DropdownMenuItem onClick={() => handleAddNotes(booking)}>
                        <MessageSquare className="h-4 w-4 mr-2" />
                        View Details
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
                {booking.teacherName && (
                  <CardDescription>
                    <div className="flex items-center space-x-2">
                      <User className="h-4 w-4 text-gray-500" />
                      <span>{booking.teacherName}</span>
                    </div>
                  </CardDescription>
                )}
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2 text-sm text-gray-600 dark:text-gray-400">
                      <Clock className="h-4 w-4" />
                      <span>
                        {new Date(booking.start).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} -{' '}
                        {new Date(booking.end).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <Badge className={getStatusColor(booking.status)}>
                      <div className="flex items-center space-x-1">
                        {getStatusIcon(booking.status)}
                        <span>{booking.status}</span>
                      </div>
                    </Badge>
                  </div>

                  <div className="flex items-center space-x-2 text-sm text-gray-600 dark:text-gray-400">
                    <span>
                      {Math.round((new Date(booking.end).getTime() - new Date(booking.start).getTime()) / 60000)} minutes
                    </span>
                  </div>

                  {booking.status === 'ACCEPTED' && (
                    <div className="bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-200 dark:border-emerald-800 rounded-lg p-3 flex items-start space-x-2">
                      <Video className="h-4 w-4 text-emerald-600 mt-0.5" />
                      <p className="text-sm text-emerald-800 dark:text-emerald-300">
                        Your class is confirmed! Click &quot;View Details&quot; to join when it&apos;s time.
                      </p>
                    </div>
                  )}

                  {booking.status === 'REQUESTED' && (
                    <div className="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-lg p-3 flex items-start space-x-2">
                      <Clock className="h-4 w-4 text-amber-600 mt-0.5" />
                      <p className="text-sm text-amber-800 dark:text-amber-300">
                        Your booking request has been sent to the teacher. You&apos;ll be notified once they respond.
                      </p>
                    </div>
                  )}

                  {booking.status === 'COMPLETED' && (
                    <div className="flex space-x-2">
                      <Button size="sm" variant="outline" onClick={() => router.push(`/student/bookings/${booking.id}`)}>
                        View Details
                      </Button>
                      <Button size="sm" variant="outline" onClick={() => router.push(`/student/history`)}>
                        Rate & Review
                      </Button>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Upcoming Classes Summary */}
      <Card className="rounded-2xl border-gray-100 dark:border-gray-800 shadow-sm">
        <CardHeader className="border-b border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-900/50">
          <CardTitle className="text-base">Upcoming Classes</CardTitle>
          <CardDescription>Your next scheduled sessions</CardDescription>
        </CardHeader>
        <CardContent className="p-6">
          <div className="space-y-3">
            {bookings
              .filter(booking => 
                booking.status === 'ACCEPTED' && 
                new Date(booking.start) > new Date()
              )
              .slice(0, 3)
              .map((booking) => (
                <div key={booking.id} className="flex items-center justify-between p-3 bg-emerald-50 dark:bg-emerald-900/20 rounded-lg">
                  <div>
                    <h4 className="font-medium text-gray-900 dark:text-white">{booking.topicTitle || booking.title}</h4>
                    <p className="text-sm text-gray-600 dark:text-gray-400">{booking.teacherName}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-medium text-gray-900 dark:text-white">
                      {new Date(booking.start).toLocaleDateString()}
                    </p>
                    <p className="text-sm text-gray-600 dark:text-gray-400">
                      {new Date(booking.start).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </p>
                  </div>
                </div>
              ))}
          </div>
        </CardContent>
      </Card>
    </div>
    </StudentRoute>
  );
}