'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  BookOpen, 
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
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

interface Booking {
  id: number;
  studentName: string;
  studentId: number;
  topicTitle: string;
  startTime: string;
  endTime: string;
  status: 'REQUESTED' | 'ACCEPTED' | 'DECLINED' | 'COMPLETED' | 'CANCELLED';
  priceCents: number;
  category: string;
  notes?: string;
  studentRating?: number;
  studentFeedback?: string;
}

export default function TeacherBookingsPage() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('pending');

  useEffect(() => {
    // Simulate API call
    setTimeout(() => {
      setBookings([
        {
          id: 1,
          studentName: 'Rahul Sharma',
          studentId: 101,
          topicTitle: 'Quadratic Equations',
          startTime: '2024-01-15T10:00:00',
          endTime: '2024-01-15T11:00:00',
          status: 'REQUESTED',
          priceCents: 60000,
          category: 'STANDARD',
          notes: 'Focus on completing the square method'
        },
        {
          id: 2,
          studentName: 'Priya Patel',
          studentId: 102,
          topicTitle: 'Trigonometry',
          startTime: '2024-01-16T14:00:00',
          endTime: '2024-01-16T15:00:00',
          status: 'REQUESTED',
          priceCents: 70000,
          category: 'PREMIUM',
          notes: 'Need help with trigonometric identities'
        },
        {
          id: 3,
          studentName: 'Amit Kumar',
          studentId: 103,
          topicTitle: 'Calculus Basics',
          startTime: '2024-01-17T16:00:00',
          endTime: '2024-01-17T17:30:00',
          status: 'ACCEPTED',
          priceCents: 80000,
          category: 'PREMIUM',
          notes: 'Introduction to derivatives'
        },
        {
          id: 4,
          studentName: 'Sneha Gupta',
          studentId: 104,
          topicTitle: 'Linear Algebra',
          startTime: '2024-01-18T10:00:00',
          endTime: '2024-01-18T11:00:00',
          status: 'ACCEPTED',
          priceCents: 75000,
          category: 'STANDARD',
          notes: 'Matrix operations'
        },
        {
          id: 5,
          studentName: 'Vikram Singh',
          studentId: 105,
          topicTitle: 'Probability',
          startTime: '2024-01-19T15:00:00',
          endTime: '2024-01-19T16:00:00',
          status: 'COMPLETED',
          priceCents: 65000,
          category: 'STANDARD',
          notes: 'Basic probability concepts',
          studentRating: 5,
          studentFeedback: 'Excellent explanation! Very helpful session.'
        },
        {
          id: 6,
          studentName: 'Anita Desai',
          studentId: 106,
          topicTitle: 'Statistics',
          startTime: '2024-01-20T11:00:00',
          endTime: '2024-01-20T12:00:00',
          status: 'COMPLETED',
          priceCents: 70000,
          category: 'PREMIUM',
          notes: 'Descriptive statistics',
          studentRating: 4,
          studentFeedback: 'Good session, helped clarify concepts.'
        },
        {
          id: 7,
          studentName: 'Rajesh Verma',
          studentId: 107,
          topicTitle: 'Geometry',
          startTime: '2024-01-21T13:00:00',
          endTime: '2024-01-21T14:00:00',
          status: 'DECLINED',
          priceCents: 60000,
          category: 'STANDARD',
          notes: 'Unable to accommodate this time slot'
        }
      ]);
      setIsLoading(false);
    }, 1000);
  }, []);

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
        return <AlertCircle className="h-4 w-4" />;
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

  const handleAcceptBooking = (bookingId: number) => {
    setBookings(prev => prev.map(booking => 
      booking.id === bookingId 
        ? { ...booking, status: 'ACCEPTED' as const }
        : booking
    ));
  };

  const handleDeclineBooking = (bookingId: number) => {
    setBookings(prev => prev.map(booking => 
      booking.id === bookingId 
        ? { ...booking, status: 'DECLINED' as const }
        : booking
    ));
  };

  const handleCompleteBooking = (bookingId: number) => {
    setBookings(prev => prev.map(booking => 
      booking.id === bookingId 
        ? { ...booking, status: 'COMPLETED' as const }
        : booking
    ));
  };

  const handleCancelBooking = (bookingId: number) => {
    setBookings(prev => prev.map(booking => 
      booking.id === bookingId 
        ? { ...booking, status: 'CANCELLED' as const }
        : booking
    ));
  };

  const getBookingsByStatus = (status: string) => {
    return bookings.filter(booking => booking.status === status);
  };

  const getUpcomingBookings = () => {
    return bookings.filter(booking => 
      (booking.status === 'ACCEPTED' || booking.status === 'REQUESTED') &&
      new Date(booking.startTime) > new Date()
    );
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <Card key={i} className="animate-pulse">
              <CardContent className="p-6">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2 mb-2"></div>
                <div className="h-4 bg-gray-200 rounded w-2/3"></div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Bookings Management</h1>
          <p className="text-gray-600">Manage your teaching sessions and student requests</p>
        </div>
        <div className="flex items-center space-x-4">
          <Badge variant="outline" className="text-blue-600 border-blue-300">
            {getBookingsByStatus('REQUESTED').length} Pending
          </Badge>
          <Badge variant="outline" className="text-green-600 border-green-300">
            {getBookingsByStatus('ACCEPTED').length} Accepted
          </Badge>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="pending">Pending</TabsTrigger>
          <TabsTrigger value="upcoming">Upcoming</TabsTrigger>
          <TabsTrigger value="completed">Completed</TabsTrigger>
          <TabsTrigger value="all">All</TabsTrigger>
        </TabsList>

        <TabsContent value="pending" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {getBookingsByStatus('REQUESTED').map((booking) => (
              <Card key={booking.id} className="border-yellow-200">
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
                      <User className="h-4 w-4 text-gray-500" />
                      <span>{booking.studentName}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <Calendar className="h-4 w-4" />
                      <span>
                        {new Date(booking.startTime).toLocaleDateString()} at{' '}
                        {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <Clock className="h-4 w-4" />
                      <span>
                        {Math.round((new Date(booking.endTime).getTime() - new Date(booking.startTime).getTime()) / 60000)} minutes
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <DollarSign className="h-4 w-4" />
                      <span className="font-semibold text-green-600">₹{(booking.priceCents / 100).toFixed(0)}</span>
                      <Badge variant="outline" className="text-xs">{booking.category}</Badge>
                    </div>
                    {booking.notes && (
                      <div className="bg-gray-50 p-3 rounded-lg">
                        <p className="text-sm text-gray-700">
                          <strong>Notes:</strong> {booking.notes}
                        </p>
                      </div>
                    )}
                    <div className="flex space-x-2">
                      <Button 
                        size="sm" 
                        className="bg-green-600 hover:bg-green-700 flex-1"
                        onClick={() => handleAcceptBooking(booking.id)}
                      >
                        <CheckCircle className="h-4 w-4 mr-1" />
                        Accept
                      </Button>
                      <Button 
                        size="sm" 
                        variant="outline"
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
          {getBookingsByStatus('REQUESTED').length === 0 && (
            <Card>
              <CardContent className="p-12 text-center">
                <AlertCircle className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">No Pending Requests</h3>
                <p className="text-gray-600">
                  You don't have any pending booking requests at the moment.
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="upcoming" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {getUpcomingBookings().map((booking) => (
              <Card key={booking.id} className="border-green-200">
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
                      <User className="h-4 w-4 text-gray-500" />
                      <span>{booking.studentName}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <Calendar className="h-4 w-4" />
                      <span>
                        {new Date(booking.startTime).toLocaleDateString()} at{' '}
                        {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <Clock className="h-4 w-4" />
                      <span>
                        {Math.round((new Date(booking.endTime).getTime() - new Date(booking.startTime).getTime()) / 60000)} minutes
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <DollarSign className="h-4 w-4" />
                      <span className="font-semibold text-green-600">₹{(booking.priceCents / 100).toFixed(0)}</span>
                      <Badge variant="outline" className="text-xs">{booking.category}</Badge>
                    </div>
                    {booking.notes && (
                      <div className="bg-gray-50 p-3 rounded-lg">
                        <p className="text-sm text-gray-700">
                          <strong>Notes:</strong> {booking.notes}
                        </p>
                      </div>
                    )}
                    <div className="flex space-x-2">
                      {booking.status === 'ACCEPTED' && (
                        <Button 
                          size="sm" 
                          className="bg-blue-600 hover:bg-blue-700 flex-1"
                          onClick={() => handleCompleteBooking(booking.id)}
                        >
                          Start Class
                        </Button>
                      )}
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="outline" size="sm">
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
            <Card>
              <CardContent className="p-12 text-center">
                <Calendar className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">No Upcoming Classes</h3>
                <p className="text-gray-600">
                  You don't have any upcoming classes scheduled.
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="completed" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {getBookingsByStatus('COMPLETED').map((booking) => (
              <Card key={booking.id} className="border-blue-200">
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
                      <User className="h-4 w-4 text-gray-500" />
                      <span>{booking.studentName}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <Calendar className="h-4 w-4" />
                      <span>
                        {new Date(booking.startTime).toLocaleDateString()} at{' '}
                        {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <DollarSign className="h-4 w-4" />
                      <span className="font-semibold text-green-600">₹{(booking.priceCents / 100).toFixed(0)}</span>
                      <Badge variant="outline" className="text-xs">{booking.category}</Badge>
                    </div>
                    {booking.studentRating && (
                      <div className="flex items-center space-x-2 text-sm text-gray-600">
                        <Star className="h-4 w-4 text-yellow-500 fill-current" />
                        <span>{booking.studentRating}/5 rating</span>
                      </div>
                    )}
                    {booking.studentFeedback && (
                      <div className="bg-blue-50 p-3 rounded-lg">
                        <p className="text-sm text-gray-700">
                          <strong>Student Feedback:</strong> {booking.studentFeedback}
                        </p>
                      </div>
                    )}
                    <div className="flex space-x-2">
                      <Button size="sm" variant="outline" className="flex-1">
                        View Details
                      </Button>
                      <Button size="sm" variant="outline">
                        Add Notes
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
          {getBookingsByStatus('COMPLETED').length === 0 && (
            <Card>
              <CardContent className="p-12 text-center">
                <CheckCircle className="h-16 w-16 text-gray-300 mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-gray-900 mb-2">No Completed Classes</h3>
                <p className="text-gray-600">
                  You haven't completed any classes yet.
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="all" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {bookings.map((booking) => (
              <Card key={booking.id} className={`border-${getStatusColor(booking.status).split('-')[1]}-200`}>
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
                      <User className="h-4 w-4 text-gray-500" />
                      <span>{booking.studentName}</span>
                    </div>
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <Calendar className="h-4 w-4" />
                      <span>
                        {new Date(booking.startTime).toLocaleDateString()} at{' '}
                        {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>
                    <div className="flex items-center space-x-2 text-sm text-gray-600">
                      <DollarSign className="h-4 w-4" />
                      <span className="font-semibold text-green-600">₹{(booking.priceCents / 100).toFixed(0)}</span>
                      <Badge variant="outline" className="text-xs">{booking.category}</Badge>
                    </div>
                    {booking.studentRating && (
                      <div className="flex items-center space-x-2 text-sm text-gray-600">
                        <Star className="h-4 w-4 text-yellow-500 fill-current" />
                        <span>{booking.studentRating}/5 rating</span>
                      </div>
                    )}
                    <div className="flex space-x-2">
                      <Button size="sm" variant="outline" className="flex-1">
                        View Details
                      </Button>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="outline" size="sm">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          {booking.status === 'REQUESTED' && (
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
                            <DropdownMenuItem onClick={() => handleCompleteBooking(booking.id)}>
                              <CheckCircle className="h-4 w-4 mr-2" />
                              Mark Complete
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