'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Clock, 
  CheckCircle,
  XCircle,
  AlertCircle,
  Star,
  User,
  Calendar,
  DollarSign,
  MoreHorizontal
} from 'lucide-react';
import { api } from '@/utils/api';
import { toast } from 'sonner';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

interface Booking {
  id: number;
  studentName?: string;
  studentId: number;
  topicTitle: string;
  startTime: string;
  endTime: string;
  status: string;
  priceMin?: number | null;
  priceCurrency?: string | null;
  studentNotes?: string | null;
  rating?: number | null;
  studentFeedback?: string | null;
}

export default function TeacherBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('pending');

  useEffect(() => {
    const loadBookings = async () => {
      try {
        setIsLoading(true);
        const [pendingResponse, acceptedResponse, completedResponse] = await Promise.all([
          api.get<Booking[]>('/teacher/bookings/pending'),
          api.get<Booking[]>('/teacher/bookings/accepted'),
          api.get<Booking[]>('/teacher/bookings/completed')
        ]);

        setBookings([
          ...(pendingResponse.data || []),
          ...(acceptedResponse.data || []),
          ...(completedResponse.data || [])
        ]);
      } catch (error) {
        console.error('Failed to load bookings:', error);
        toast.error('Failed to load bookings');
      } finally {
        setIsLoading(false);
      }
    };

    loadBookings();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'CONFIRMED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'IN_PROGRESS':
        return 'bg-indigo-100 text-indigo-800 border-indigo-200';
      case 'COMPLETED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
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
      case 'PENDING':
        return <AlertCircle className="h-4 w-4" />;
      case 'CONFIRMED':
        return <CheckCircle className="h-4 w-4" />;
      case 'IN_PROGRESS':
        return <Clock className="h-4 w-4" />;
      case 'COMPLETED':
        return <CheckCircle className="h-4 w-4" />;
      case 'CANCELLED':
        return <XCircle className="h-4 w-4" />;
      default:
        return <AlertCircle className="h-4 w-4" />;
    }
  };

  const reloadBookings = async () => {
    const [pendingResponse, acceptedResponse, completedResponse] = await Promise.all([
      api.get<Booking[]>('/teacher/bookings/pending'),
      api.get<Booking[]>('/teacher/bookings/accepted'),
      api.get<Booking[]>('/teacher/bookings/completed')
    ]);
    setBookings([
      ...(pendingResponse.data || []),
      ...(acceptedResponse.data || []),
      ...(completedResponse.data || [])
    ]);
  };

  const handleAcceptBooking = async (bookingId: number) => {
    try {
      await api.post(`/teacher/bookings/${bookingId}/accept?acceptanceToken=manual`, {});
      await reloadBookings();
      toast.success('Booking accepted');
    } catch (error: any) {
      console.error('Accept booking failed:', error);
      const message = error?.response?.data?.error || error?.response?.data?.message || 'Failed to accept booking';
      toast.error(message);
    }
  };

  const handleDeclineBooking = async (bookingId: number) => {
    try {
      await api.post(`/teacher/bookings/${bookingId}/decline`, {});
      await reloadBookings();
      toast.success('Booking declined');
    } catch (error: any) {
      console.error('Decline booking failed:', error);
      const message = error?.response?.data?.error || error?.response?.data?.message || 'Failed to decline booking';
      toast.error(message);
    }
  };

  const handleStartSession = async (bookingId: number) => {
    try {
      await api.post(`/teacher/sessions/${bookingId}/start`, {});
      await reloadBookings();
      toast.success('Class started');
    } catch (error) {
      console.error('Start session failed:', error);
      toast.error('Failed to start class');
    }
  };

  const handleEndSession = async (bookingId: number) => {
    try {
      await api.post(`/teacher/sessions/${bookingId}/end`, { sessionNotes: '' });
      await reloadBookings();
      toast.success('Class completed');
    } catch (error) {
      console.error('End session failed:', error);
      toast.error('Failed to complete class');
    }
  };

  const handleCancelBooking = async (bookingId: number) => {
    try {
      await api.post(`/teacher/bookings/${bookingId}/decline`, {});
      await reloadBookings();
      toast.success('Booking cancelled');
    } catch (error) {
      console.error('Cancel booking failed:', error);
      toast.error('Failed to cancel booking');
    }
  };

  const getBookingsByStatus = (status: string) => {
    return bookings.filter(booking => booking.status === status);
  };

  const getUpcomingBookings = () => {
    return bookings.filter(booking => 
      (booking.status === 'ACCEPTED' || booking.status === 'CONFIRMED' || booking.status === 'IN_PROGRESS' || booking.status === 'PENDING') &&
      new Date(booking.startTime) > new Date()
    );
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="glass-panel p-6">
              <div className="skeleton h-4 rounded w-3/4 mb-2"></div>
              <div className="skeleton h-4 rounded w-1/2 mb-2"></div>
              <div className="skeleton h-4 rounded w-2/3"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="page-header">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-white">Bookings Management</h1>
            <p className="text-white/80">Manage your teaching sessions and student requests</p>
          </div>
          <div className="flex items-center space-x-4">
            <Badge variant="outline" className="border-white/40 bg-white/10 text-white">
              {getBookingsByStatus('PENDING').length} Pending
            </Badge>
            <Badge variant="outline" className="border-white/40 bg-white/10 text-white">
              {getBookingsByStatus('ACCEPTED').length} Accepted
            </Badge>
          </div>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="glass-panel grid w-full grid-cols-4 p-1">
          <TabsTrigger value="pending">Pending</TabsTrigger>
          <TabsTrigger value="upcoming">Upcoming</TabsTrigger>
          <TabsTrigger value="completed">Completed</TabsTrigger>
          <TabsTrigger value="all">All</TabsTrigger>
        </TabsList>

        <TabsContent value="pending" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {getBookingsByStatus('PENDING').map((booking) => (
              <Card key={booking.id} className="glass-panel border border-white/40">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{booking.topicTitle}</CardTitle>
                    <Badge className={getStatusColor(booking.status)}>
                      <div className="flex items-center space-x-1">
                        {getStatusIcon(booking.status)}
                        <span>{booking.status}</span>
                      </div>
                    </Badge>
                  </div>
                  <CardDescription>
                    <div className="flex items-center space-x-2">
                      <User className="h-4 w-4 text-slate-400" />
                      <span>{booking.studentName}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <Calendar className="h-4 w-4" />
                      <span>
                        {new Date(booking.startTime).toLocaleDateString()} at{' '}
                        {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <Clock className="h-4 w-4" />
                      <span>
                        {Math.round((new Date(booking.endTime).getTime() - new Date(booking.startTime).getTime()) / 60000)} minutes
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <DollarSign className="h-4 w-4" />
                      <span className="font-semibold text-emerald-500">
                        {booking.priceCurrency || 'INR'} {booking.priceMin ?? 0}
                      </span>
                    </div>
                    {booking.studentNotes && (
                      <div className="glass p-3 rounded-lg border border-white/30">
                        <p className="text-sm text-slate-700 dark:text-slate-200">
                          <strong>Notes:</strong> {booking.studentNotes}
                        </p>
                      </div>
                    )}
                    <div className="flex space-x-2">
                      <Button 
                        size="sm" 
                        className="btn-primary h-9 px-4 text-sm flex-1"
                        onClick={() => handleAcceptBooking(booking.id)}
                      >
                        <CheckCircle className="h-4 w-4 mr-1" />
                        Accept
                      </Button>
                      <Button 
                        size="sm" 
                        variant="outline"
                        className="btn-outline h-9 px-4 text-sm flex-1"
                        onClick={() => handleDeclineBooking(booking.id)}
                      >
                        <XCircle className="h-4 w-4 mr-1" />
                        Decline
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
          {getBookingsByStatus('PENDING').length === 0 && (
            <Card className="glass-panel border border-white/40">
              <CardContent className="p-12 text-center">
                <AlertCircle className="h-16 w-16 text-slate-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">No Pending Requests</h3>
                <p className="text-slate-600 dark:text-slate-300">
                  You don't have any pending booking requests at the moment.
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="upcoming" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {getUpcomingBookings().map((booking) => (
              <Card key={booking.id} className="glass-panel border border-white/40">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{booking.topicTitle}</CardTitle>
                    <Badge className={getStatusColor(booking.status)}>
                      <div className="flex items-center space-x-1">
                        {getStatusIcon(booking.status)}
                        <span>{booking.status}</span>
                      </div>
                    </Badge>
                  </div>
                  <CardDescription>
                    <div className="flex items-center space-x-2">
                      <User className="h-4 w-4 text-slate-400" />
                      <span>{booking.studentName}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <Calendar className="h-4 w-4" />
                      <span>
                        {new Date(booking.startTime).toLocaleDateString()} at{' '}
                        {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <Clock className="h-4 w-4" />
                      <span>
                        {Math.round((new Date(booking.endTime).getTime() - new Date(booking.startTime).getTime()) / 60000)} minutes
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <DollarSign className="h-4 w-4" />
                      <span className="font-semibold text-emerald-500">
                        {booking.priceCurrency || 'INR'} {booking.priceMin ?? 0}
                      </span>
                    </div>
                    {booking.studentNotes && (
                      <div className="glass p-3 rounded-lg border border-white/30">
                        <p className="text-sm text-slate-700 dark:text-slate-200">
                          <strong>Notes:</strong> {booking.studentNotes}
                        </p>
                      </div>
                    )}
                    <div className="flex space-x-2">
                      {booking.status === 'ACCEPTED' && (
                        <Button 
                          size="sm" 
                          className="btn-primary h-9 px-4 text-sm flex-1"
                          onClick={() => handleStartSession(booking.id)}
                        >
                          Start Class
                        </Button>
                      )}
                      {booking.status === 'IN_PROGRESS' && (
                        <Button 
                          size="sm" 
                          className="btn-primary h-9 px-4 text-sm flex-1"
                          onClick={() => handleEndSession(booking.id)}
                        >
                          End Class
                        </Button>
                      )}
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="outline" size="sm" className="btn-outline h-9 px-3 text-sm">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => handleCancelBooking(booking.id)}>
                            <XCircle className="h-4 w-4 mr-2" />
                            Cancel
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
          {getUpcomingBookings().length === 0 && (
            <Card className="glass-panel border border-white/40">
              <CardContent className="p-12 text-center">
                <Calendar className="h-16 w-16 text-slate-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">No Upcoming Classes</h3>
                <p className="text-slate-600 dark:text-slate-300">
                  You don't have any upcoming classes scheduled.
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="completed" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {getBookingsByStatus('COMPLETED').map((booking) => (
              <Card key={booking.id} className="glass-panel border border-white/40">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{booking.topicTitle}</CardTitle>
                    <Badge className={getStatusColor(booking.status)}>
                      <div className="flex items-center space-x-1">
                        {getStatusIcon(booking.status)}
                        <span>{booking.status}</span>
                      </div>
                    </Badge>
                  </div>
                  <CardDescription>
                    <div className="flex items-center space-x-2">
                      <User className="h-4 w-4 text-slate-400" />
                      <span>{booking.studentName}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <Calendar className="h-4 w-4" />
                      <span>
                        {new Date(booking.startTime).toLocaleDateString()} at{' '}
                        {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <DollarSign className="h-4 w-4" />
                      <span className="font-semibold text-emerald-500">
                        {booking.priceCurrency || 'INR'} {booking.priceMin ?? 0}
                      </span>
                    </div>
                    {booking.rating && (
                      <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                        <Star className="h-4 w-4 text-yellow-500 fill-current" />
                        <span>{booking.rating}/5 rating</span>
                      </div>
                    )}
                    {booking.studentFeedback && (
                      <div className="glass p-3 rounded-lg border border-white/30">
                        <p className="text-sm text-slate-700 dark:text-slate-200">
                          <strong>Student Feedback:</strong> {booking.studentFeedback}
                        </p>
                      </div>
                    )}
                    <div className="flex space-x-2">
                      <Button size="sm" variant="outline" className="btn-outline h-9 px-4 text-sm flex-1">
                        View Details
                      </Button>
                      <Button size="sm" variant="outline" className="btn-outline h-9 px-4 text-sm">
                        Add Notes
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
          {getBookingsByStatus('COMPLETED').length === 0 && (
            <Card className="glass-panel border border-white/40">
              <CardContent className="p-12 text-center">
                <CheckCircle className="h-16 w-16 text-slate-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">No Completed Classes</h3>
                <p className="text-slate-600 dark:text-slate-300">
                  You haven't completed any classes yet.
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="all" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {bookings.map((booking) => (
              <Card key={booking.id} className="glass-panel border border-white/40">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="text-lg">{booking.topicTitle}</CardTitle>
                    <Badge className={getStatusColor(booking.status)}>
                      <div className="flex items-center space-x-1">
                        {getStatusIcon(booking.status)}
                        <span>{booking.status}</span>
                      </div>
                    </Badge>
                  </div>
                  <CardDescription>
                    <div className="flex items-center space-x-2">
                      <User className="h-4 w-4 text-slate-400" />
                      <span>{booking.studentName}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <Calendar className="h-4 w-4" />
                      <span>
                        {new Date(booking.startTime).toLocaleDateString()} at{' '}
                        {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                      <DollarSign className="h-4 w-4" />
                      <span className="font-semibold text-emerald-500">
                        {booking.priceCurrency || 'INR'} {booking.priceMin ?? 0}
                      </span>
                    </div>
                    {booking.rating && (
                      <div className="flex items-center space-x-2 text-sm text-slate-600 dark:text-slate-300">
                        <Star className="h-4 w-4 text-yellow-500 fill-current" />
                        <span>{booking.rating}/5 rating</span>
                      </div>
                    )}
                    <div className="flex space-x-2">
                      <Button size="sm" variant="outline" className="btn-outline h-9 px-4 text-sm flex-1">
                        View Details
                      </Button>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="outline" size="sm" className="btn-outline h-9 px-3 text-sm">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          {booking.status === 'PENDING' && (
                            <>
                              <DropdownMenuItem onClick={() => handleAcceptBooking(booking.id)}>
                                <CheckCircle className="h-4 w-4 mr-2" />
                                Accept
                              </DropdownMenuItem>
                              <DropdownMenuItem onClick={() => handleDeclineBooking(booking.id)}>
                                <XCircle className="h-4 w-4 mr-2" />
                                Decline
                              </DropdownMenuItem>
                            </>
                          )}
                          {booking.status === 'ACCEPTED' && (
                            <DropdownMenuItem onClick={() => handleStartSession(booking.id)}>
                              <CheckCircle className="h-4 w-4 mr-2" />
                              Start Class
                            </DropdownMenuItem>
                          )}
                          {booking.status === 'IN_PROGRESS' && (
                            <DropdownMenuItem onClick={() => handleEndSession(booking.id)}>
                              <CheckCircle className="h-4 w-4 mr-2" />
                              End Class
                            </DropdownMenuItem>
                          )}
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}
