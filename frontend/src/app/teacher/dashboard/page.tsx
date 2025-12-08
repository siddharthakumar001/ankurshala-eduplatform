'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { 
  BookOpen, 
  Calendar, 
  Clock, 
  DollarSign,
  Star,
  Users,
  TrendingUp,
  AlertCircle,
  CheckCircle,
  XCircle,
  MoreHorizontal,
  ArrowUpRight,
  ArrowDownRight,
  PlayCircle,
  Settings,
  Bell,
} from 'lucide-react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import Link from 'next/link';
import DashboardLayout from '@/components/layout/DashboardLayout';
import MetricCard from '@/components/ui/MetricCard';
import { useAuthStore } from '@/store/auth';

interface DashboardStats {
  pendingRequests: number;
  upcomingClasses: number;
  earningsMTD: number;
  totalEarnings: number;
  averageRating: number;
  totalRatings: number;
  pendingBookings: PendingBooking[];
  upcomingClassesList: UpcomingClass[];
  earningsBreakdown: EarningsBreakdown;
}

interface PendingBooking {
  bookingId: number;
  studentName: string;
  topicTitle: string;
  startTime: string;
  endTime: string;
  priceCents: number;
  category: string;
}

interface UpcomingClass {
  bookingId: number;
  studentName: string;
  topicTitle: string;
  startTime: string;
  endTime: string;
  status: string;
}

interface EarningsBreakdown {
  thisMonth: number;
  lastMonth: number;
  thisYear: number;
  totalClasses: number;
  averagePerClass: number;
}

export default function TeacherDashboardPage() {
  const user = useAuthStore((state) => state.user);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Simulate API call
    setTimeout(() => {
      setStats({
        pendingRequests: 5,
        upcomingClasses: 8,
        earningsMTD: 25000,
        totalEarnings: 125000,
        averageRating: 4.8,
        totalRatings: 156,
        pendingBookings: [
          {
            bookingId: 1,
            studentName: 'Rahul Sharma',
            topicTitle: 'Quadratic Equations',
            startTime: '2024-01-15T10:00:00',
            endTime: '2024-01-15T11:00:00',
            priceCents: 60000,
            category: 'STANDARD'
          },
          {
            bookingId: 2,
            studentName: 'Priya Patel',
            topicTitle: 'Trigonometry',
            startTime: '2024-01-16T14:00:00',
            endTime: '2024-01-16T15:00:00',
            priceCents: 70000,
            category: 'PREMIUM'
          },
          {
            bookingId: 3,
            studentName: 'Amit Kumar',
            topicTitle: 'Calculus Basics',
            startTime: '2024-01-17T16:00:00',
            endTime: '2024-01-17T17:30:00',
            priceCents: 80000,
            category: 'PREMIUM'
          }
        ],
        upcomingClassesList: [
          {
            bookingId: 4,
            studentName: 'Sneha Gupta',
            topicTitle: 'Linear Algebra',
            startTime: '2024-01-18T10:00:00',
            endTime: '2024-01-18T11:00:00',
            status: 'ACCEPTED'
          },
          {
            bookingId: 5,
            studentName: 'Vikram Singh',
            topicTitle: 'Probability',
            startTime: '2024-01-19T15:00:00',
            endTime: '2024-01-19T16:00:00',
            status: 'ACCEPTED'
          }
        ],
        earningsBreakdown: {
          thisMonth: 25000,
          lastMonth: 22000,
          thisYear: 125000,
          totalClasses: 45,
          averagePerClass: 2778
        }
      });
      setIsLoading(false);
    }, 1000);
  }, []);

  const handleAcceptBooking = (bookingId: number) => {
    console.log('Accept booking:', bookingId);
    // Update UI optimistically
    setStats(prev => {
      if (!prev) return prev;
      const updatedPending = prev.pendingBookings.filter(b => b.bookingId !== bookingId);
      return { ...prev, pendingRequests: updatedPending.length, pendingBookings: updatedPending };
    });
  };

  const handleDeclineBooking = (bookingId: number) => {
    console.log('Decline booking:', bookingId);
    // Update UI optimistically
    setStats(prev => {
      if (!prev) return prev;
      const updatedPending = prev.pendingBookings.filter(b => b.bookingId !== bookingId);
      return { ...prev, pendingRequests: updatedPending.length, pendingBookings: updatedPending };
    });
  };

  if (isLoading) {
    return (
      <DashboardLayout role="teacher">
        <div className="space-y-6">
          <div className="h-32 bg-gray-200 rounded-2xl animate-pulse" />
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {[...Array(4)].map((_, i) => (
              <Card key={i} className="animate-pulse">
                <CardContent className="p-6">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                  <div className="h-8 bg-gray-200 rounded w-1/2"></div>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </DashboardLayout>
    );
  }

  // Calculate earnings growth
  const earningsGrowth = stats?.earningsBreakdown 
    ? Math.round(((stats.earningsBreakdown.thisMonth - stats.earningsBreakdown.lastMonth) / stats.earningsBreakdown.lastMonth) * 100)
    : 0;

  return (
    <DashboardLayout role="teacher">
      <div className="space-y-6">
        {/* Welcome Section */}
        <div className="bg-gradient-to-r from-ankur-secondary to-[#2a4a73] rounded-2xl p-8 text-white">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h1 className="text-2xl md:text-3xl font-bold mb-2">
                Welcome back, {user?.name?.split(' ')[0] || 'Teacher'}! 👋
              </h1>
              <p className="text-white/80">
                You have {stats?.pendingRequests || 0} pending requests and {stats?.upcomingClasses || 0} upcoming classes.
              </p>
            </div>
            <div className="flex items-center gap-6">
              <div className="text-center">
                <p className="text-4xl font-bold text-ankur-accent">{stats?.pendingRequests || 0}</p>
                <p className="text-sm text-white/70">Pending</p>
              </div>
              <div className="text-center">
                <p className="text-4xl font-bold text-white">{stats?.upcomingClasses || 0}</p>
                <p className="text-sm text-white/70">Upcoming</p>
              </div>
              <div className="flex items-center gap-1 text-center">
                <Star className="w-5 h-5 text-yellow-400 fill-current" />
                <p className="text-2xl font-bold text-white">{stats?.averageRating || 0}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <MetricCard
            title="Pending Requests"
            value={stats?.pendingRequests || 0}
            icon={AlertCircle}
            iconBgColor="bg-orange-500"
            href="/teacher/bookings"
          />
          <MetricCard
            title="Upcoming Classes"
            value={stats?.upcomingClasses || 0}
            icon={Calendar}
            iconBgColor="bg-blue-500"
            href="/teacher/schedule"
          />
          <MetricCard
            title="Earnings (MTD)"
            value={`₹${(stats?.earningsMTD || 0).toLocaleString()}`}
            change={earningsGrowth}
            changeLabel="vs last month"
            icon={DollarSign}
            iconBgColor="bg-ankur-primary"
          />
          <MetricCard
            title="Average Rating"
            value={`${stats?.averageRating || 0} ⭐`}
            icon={Star}
            iconBgColor="bg-yellow-500"
            iconColor="text-gray-900"
          />
        </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Pending Requests */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center">
              <AlertCircle className="h-5 w-5 mr-2 text-blue-500" />
              Pending Requests
            </CardTitle>
            <CardDescription>New booking requests waiting for your response</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {stats?.pendingBookings.map((booking) => (
                <div key={booking.bookingId} className="border border-blue-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-gray-900">{booking.topicTitle}</h3>
                    <Badge variant="outline" className="text-blue-600 border-blue-300">
                      {booking.category}
                    </Badge>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">Student: {booking.studentName}</p>
                  <p className="text-sm text-gray-600 mb-2">
                    {new Date(booking.startTime).toLocaleDateString()} at{' '}
                    {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                  <p className="text-sm text-gray-600 mb-3">
                    Duration: {Math.round((new Date(booking.endTime).getTime() - new Date(booking.startTime).getTime()) / 60000)} minutes
                  </p>
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-green-600">₹{(booking.priceCents / 100).toFixed(0)}</span>
                    <div className="flex space-x-2">
                      <Button 
                        size="sm" 
                        className="bg-green-600 hover:bg-green-700"
                        onClick={() => handleAcceptBooking(booking.bookingId)}
                      >
                        <CheckCircle className="h-4 w-4 mr-1" />
                        Accept
                      </Button>
                      <Button 
                        size="sm" 
                        variant="outline"
                        onClick={() => handleDeclineBooking(booking.bookingId)}
                      >
                        <XCircle className="h-4 w-4 mr-1" />
                        Decline
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
              <Link href="/teacher/bookings">
                <Button variant="outline" className="w-full">
                  View All Requests
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>

        {/* Upcoming Classes */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center">
              <Calendar className="h-5 w-5 mr-2 text-blue-500" />
              Upcoming Classes
            </CardTitle>
            <CardDescription>Your confirmed upcoming sessions</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {stats?.upcomingClassesList.map((classItem) => (
                <div key={classItem.bookingId} className="border border-blue-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-gray-900">{classItem.topicTitle}</h3>
                    <Badge className="bg-green-100 text-green-800 border-green-200">
                      {classItem.status}
                    </Badge>
                  </div>
                  <p className="text-sm text-gray-600 mb-2">Student: {classItem.studentName}</p>
                  <p className="text-sm text-gray-600 mb-3">
                    {new Date(classItem.startTime).toLocaleDateString()} at{' '}
                    {new Date(classItem.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                  <div className="flex space-x-2">
                    <Button size="sm" className="bg-blue-600 hover:bg-blue-700">
                      Start Class
                    </Button>
                    <Button size="sm" variant="outline">
                      View Details
                    </Button>
                  </div>
                </div>
              ))}
              <Link href="/teacher/calendar">
                <Button variant="outline" className="w-full">
                  View Full Calendar
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Earnings Overview */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <DollarSign className="h-5 w-5 mr-2 text-blue-500" />
            Earnings Overview
          </CardTitle>
          <CardDescription>Your teaching income breakdown</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <p className="text-sm text-gray-600">This Month</p>
              <p className="text-2xl font-bold text-blue-600">₹{stats?.earningsBreakdown.thisMonth.toLocaleString()}</p>
            </div>
            <div className="text-center">
              <p className="text-sm text-gray-600">Last Month</p>
              <p className="text-2xl font-bold text-gray-600">₹{stats?.earningsBreakdown.lastMonth.toLocaleString()}</p>
            </div>
            <div className="text-center">
              <p className="text-sm text-gray-600">This Year</p>
              <p className="text-2xl font-bold text-green-600">₹{stats?.earningsBreakdown.thisYear.toLocaleString()}</p>
            </div>
          </div>
          <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-blue-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600">Total Classes Taught</p>
              <p className="text-xl font-bold text-blue-600">{stats?.earningsBreakdown.totalClasses}</p>
            </div>
            <div className="bg-green-50 p-4 rounded-lg">
              <p className="text-sm text-gray-600">Average per Class</p>
              <p className="text-xl font-bold text-green-600">₹{stats?.earningsBreakdown.averagePerClass.toLocaleString()}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common tasks and shortcuts</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Link href="/teacher/bookings">
              <Button className="w-full h-20 flex flex-col items-center justify-center space-y-2">
                <BookOpen className="h-6 w-6" />
                <span className="text-sm">Manage Bookings</span>
              </Button>
            </Link>
            <Link href="/teacher/calendar">
              <Button variant="outline" className="w-full h-20 flex flex-col items-center justify-center space-y-2">
                <Calendar className="h-6 w-6" />
                <span className="text-sm">View Calendar</span>
              </Button>
            </Link>
            <Link href="/teacher/earnings">
              <Button variant="outline" className="w-full h-20 flex flex-col items-center justify-center space-y-2">
                <DollarSign className="h-6 w-6" />
                <span className="text-sm">Earnings</span>
              </Button>
            </Link>
            <Link href="/teacher/profile">
              <Button variant="outline" className="w-full h-20 flex flex-col items-center justify-center space-y-2">
                <Users className="h-6 w-6" />
                <span className="text-sm">Profile</span>
              </Button>
            </Link>
          </div>
        </CardContent>
      </Card>
      </div>
    </DashboardLayout>
  );
}