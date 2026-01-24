'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { 
  BookOpen, 
  Calendar, 
  DollarSign,
  Star,
  AlertCircle,
  CheckCircle,
  XCircle,
  Users,
} from 'lucide-react';
import Link from 'next/link';
import DashboardLayout from '@/components/layout/DashboardLayout';
import MetricCard from '@/components/ui/MetricCard';
import { useAuthStore } from '@/store/auth';
import { api } from '@/utils/api';
import { toast } from 'sonner';

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
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const response = await api.get<DashboardStats>('/dashboard/teacher/stats');
        setStats(response.data);
      } catch (err) {
        console.error('Failed to load teacher dashboard:', err);
        setError('Unable to load dashboard data. Please try again.');
        toast.error('Failed to load dashboard data');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboard();
  }, []);

  const handleAcceptBooking = async (bookingId: number) => {
    try {
      await api.post(`/teacher/bookings/${bookingId}/accept?acceptanceToken=manual`, {});
      toast.success('Booking accepted');
      setStats(prev => {
        if (!prev) return prev;
        const updatedPending = prev.pendingBookings.filter(b => b.bookingId !== bookingId);
        return { ...prev, pendingRequests: updatedPending.length, pendingBookings: updatedPending };
      });
    } catch (err: any) {
      console.error('Accept booking failed:', err);
      const message = err?.response?.data?.error || err?.response?.data?.message || 'Failed to accept booking';
      toast.error(message);
    }
  };

  const handleDeclineBooking = async (bookingId: number) => {
    try {
      await api.post(`/teacher/bookings/${bookingId}/decline`, {});
      toast.success('Booking declined');
      setStats(prev => {
        if (!prev) return prev;
        const updatedPending = prev.pendingBookings.filter(b => b.bookingId !== bookingId);
        return { ...prev, pendingRequests: updatedPending.length, pendingBookings: updatedPending };
      });
    } catch (err: any) {
      console.error('Decline booking failed:', err);
      const message = err?.response?.data?.error || err?.response?.data?.message || 'Failed to decline booking';
      toast.error(message);
    }
  };

  if (isLoading) {
    return (
      <DashboardLayout role="teacher">
        <div className="space-y-6">
          <div className="skeleton h-32 rounded-3xl" />
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="glass-panel p-6">
                <div className="skeleton h-4 rounded w-3/4 mb-2"></div>
                <div className="skeleton h-8 rounded w-1/2"></div>
              </div>
            ))}
          </div>
        </div>
      </DashboardLayout>
    );
  }

  if (error) {
    return (
      <DashboardLayout role="teacher">
        <div className="flex flex-col items-center justify-center min-h-[400px] space-y-4">
          <AlertCircle className="h-10 w-10 text-red-500" />
          <p className="text-slate-700 dark:text-slate-200">{error}</p>
          <Button className="btn-primary" onClick={() => window.location.reload()}>Retry</Button>
        </div>
      </DashboardLayout>
    );
  }

  const formatCurrency = (amount?: number | string | null) => {
    const safeAmount = typeof amount === 'number' ? amount : Number(amount || 0)
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(safeAmount || 0);
  };

  // Calculate earnings growth
  const earningsGrowth = stats?.earningsBreakdown 
    ? Math.round(((stats.earningsBreakdown.thisMonth - stats.earningsBreakdown.lastMonth) / (stats.earningsBreakdown.lastMonth || 1)) * 100)
    : 0;

  return (
    <DashboardLayout role="teacher">
      <div className="space-y-6">
        {/* Welcome Section */}
        <div className="page-header">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div>
              <h1 className="text-2xl md:text-3xl font-bold mb-2">
                Welcome back, {user?.name?.split(' ')[0] || 'Teacher'}!
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
                <p className="text-2xl font-bold text-white">{Number(stats?.averageRating || 0).toFixed(1)}</p>
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
            href="/teacher/sessions"
          />
          <MetricCard
            title="Earnings (MTD)"
            value={formatCurrency(stats?.earningsMTD)}
            change={earningsGrowth}
            changeLabel="vs last month"
            icon={DollarSign}
            iconBgColor="bg-ankur-primary"
          />
          <MetricCard
            title="Average Rating"
            value={(stats?.averageRating || 0).toFixed(1)}
            icon={Star}
            iconBgColor="bg-yellow-500"
            iconColor="text-gray-900"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Pending Requests */}
        <Card className="glass-panel border border-white/40">
          <CardHeader>
            <CardTitle className="flex items-center">
              <AlertCircle className="h-5 w-5 mr-2 text-emerald-500" />
              Pending Requests
            </CardTitle>
            <CardDescription>New booking requests waiting for your response</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {stats?.pendingBookings?.map((booking) => (
                <div key={booking.bookingId} className="glass rounded-lg p-4 border border-white/30">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-slate-900 dark:text-white">{booking.topicTitle}</h3>
                    <Badge variant="outline" className="border-white/40 bg-white/70 text-slate-700">
                      {booking.category}
                    </Badge>
                  </div>
                  <p className="text-sm text-slate-600 dark:text-slate-300 mb-2">Student: {booking.studentName}</p>
                  <p className="text-sm text-slate-600 dark:text-slate-300 mb-2">
                    {new Date(booking.startTime).toLocaleDateString()} at{' '}
                    {new Date(booking.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                  <p className="text-sm text-slate-600 dark:text-slate-300 mb-3">
                    Duration: {Math.round((new Date(booking.endTime).getTime() - new Date(booking.startTime).getTime()) / 60000)} minutes
                  </p>
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-emerald-500">{formatCurrency((booking.priceCents || 0) / 100)}</span>
                    <div className="flex space-x-2">
                      <Button 
                        size="sm" 
                        className="btn-primary h-9 px-4 text-sm"
                        onClick={() => handleAcceptBooking(booking.bookingId)}
                      >
                        <CheckCircle className="h-4 w-4 mr-1" />
                        Accept
                      </Button>
                      <Button 
                        size="sm" 
                        variant="outline"
                        className="btn-outline h-9 px-4 text-sm"
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
                <Button variant="outline" className="btn-outline w-full">
                  View All Requests
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>

        {/* Upcoming Classes */}
        <Card className="glass-panel border border-white/40">
          <CardHeader>
            <CardTitle className="flex items-center">
              <Calendar className="h-5 w-5 mr-2 text-emerald-500" />
              Upcoming Classes
            </CardTitle>
            <CardDescription>Your confirmed upcoming sessions</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {stats?.upcomingClassesList?.map((classItem) => (
                <div key={classItem.bookingId} className="glass rounded-lg p-4 border border-white/30">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-slate-900 dark:text-white">{classItem.topicTitle}</h3>
                    <Badge className="badge-success">
                      {classItem.status}
                    </Badge>
                  </div>
                  <p className="text-sm text-slate-600 dark:text-slate-300 mb-2">Student: {classItem.studentName}</p>
                  <p className="text-sm text-slate-600 dark:text-slate-300 mb-3">
                    {new Date(classItem.startTime).toLocaleDateString()} at{' '}
                    {new Date(classItem.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                  <div className="flex space-x-2">
                    <Link href="/teacher/sessions">
                      <Button size="sm" className="btn-primary h-9 px-4 text-sm">
                        Start Class
                      </Button>
                    </Link>
                    <Link href="/teacher/bookings">
                      <Button size="sm" variant="outline" className="btn-outline h-9 px-4 text-sm">
                        View Details
                      </Button>
                    </Link>
                  </div>
                </div>
              ))}
              <Link href="/teacher/availability">
                <Button variant="outline" className="btn-outline w-full">
                  Manage Availability
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Earnings Overview */}
      <Card className="glass-panel border border-white/40">
        <CardHeader>
          <CardTitle className="flex items-center">
            <DollarSign className="h-5 w-5 mr-2 text-emerald-500" />
            Earnings Overview
          </CardTitle>
          <CardDescription>Your teaching income breakdown</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <p className="text-sm text-slate-500 dark:text-slate-300">This Month</p>
              <p className="text-2xl font-bold text-emerald-500">{formatCurrency(stats?.earningsBreakdown.thisMonth)}</p>
            </div>
            <div className="text-center">
              <p className="text-sm text-slate-500 dark:text-slate-300">Last Month</p>
              <p className="text-2xl font-bold text-slate-600 dark:text-slate-200">{formatCurrency(stats?.earningsBreakdown.lastMonth)}</p>
            </div>
            <div className="text-center">
              <p className="text-sm text-slate-500 dark:text-slate-300">This Year</p>
              <p className="text-2xl font-bold text-emerald-500">{formatCurrency(stats?.earningsBreakdown.thisYear)}</p>
            </div>
          </div>
          <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="glass p-4 rounded-lg border border-white/30">
              <p className="text-sm text-slate-500 dark:text-slate-300">Total Classes Taught</p>
              <p className="text-xl font-bold text-emerald-500">{stats?.earningsBreakdown.totalClasses}</p>
            </div>
            <div className="glass p-4 rounded-lg border border-white/30">
              <p className="text-sm text-slate-500 dark:text-slate-300">Average per Class</p>
              <p className="text-xl font-bold text-emerald-500">{formatCurrency(stats?.earningsBreakdown.averagePerClass)}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Card className="glass-panel border border-white/40">
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common tasks and shortcuts</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Link href="/teacher/bookings">
              <Button className="btn-primary w-full h-20 flex flex-col items-center justify-center space-y-2">
                <BookOpen className="h-6 w-6" />
                <span className="text-sm">Manage Bookings</span>
              </Button>
            </Link>
            <Link href="/teacher/availability">
              <Button variant="outline" className="btn-outline w-full h-20 flex flex-col items-center justify-center space-y-2">
                <Calendar className="h-6 w-6" />
                <span className="text-sm">Availability</span>
              </Button>
            </Link>
            <Link href="/teacher/earnings">
              <Button variant="outline" className="btn-outline w-full h-20 flex flex-col items-center justify-center space-y-2">
                <DollarSign className="h-6 w-6" />
                <span className="text-sm">Earnings</span>
              </Button>
            </Link>
            <Link href="/teacher/profile">
              <Button variant="outline" className="btn-outline w-full h-20 flex flex-col items-center justify-center space-y-2">
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
