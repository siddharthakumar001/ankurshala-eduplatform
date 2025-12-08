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
  TrendingUp, 
  Star,
  Users,
  Target,
  Brain,
  AlertCircle,
  Bell,
  CreditCard,
  Search,
  ChevronRight,
  PlayCircle,
  Award,
} from 'lucide-react';
import Link from 'next/link';
import { studentAPI } from '@/lib/apiClient';
import { toast } from 'sonner';
import { StudentRoute } from '@/components/route-guard';
import { useAuthStore } from '@/store/auth';

interface DashboardStats {
  upcomingBookings: number;
  completedBookings: number;
  totalHoursSpent: number;
  subjectMastery: SubjectMastery[];
  upcomingClasses: UpcomingClass[];
  recommendations: TopicRecommendation[];
}

interface SubjectMastery {
  subjectId: number;
  subjectName: string;
  masteryPercentage: number;
  totalQuizzes: number;
  averageScore: number;
}

interface UpcomingClass {
  bookingId: number;
  topicTitle: string;
  teacherName: string;
  startTime: string;
  endTime: string;
  status: string;
}

interface TopicRecommendation {
  topicId: number;
  title: string;
  description: string;
  confidence: number;
  reason: string;
}

export default function StudentDashboardPage() {
  return (
    <StudentRoute>
      <DashboardContent />
    </StudentRoute>
  );
}

// Separate component for dashboard content that renders AFTER authentication
function DashboardContent() {
  const user = useAuthStore((state) => state.user);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const data = await studentAPI.getDashboard()
        setStats(data)
      } catch (error: any) {
        console.error('Failed to load dashboard:', error)
        // If it's a 404 or the endpoint doesn't exist, use default empty data
        if (error.response?.status === 404 || error.response?.status === 500) {
          // Set default empty stats instead of showing error
          setStats({
            upcomingBookings: 0,
            completedBookings: 0,
            totalHoursSpent: 0,
            subjectMastery: [],
            upcomingClasses: [],
            recommendations: []
          })
        } else if (error.response?.status !== 401) {
          // Only show error if it's not a 401 (auth handled elsewhere)
          toast.error('Failed to load dashboard data')
        }
      } finally {
        setIsLoading(false)
      }
    }
    
    loadDashboard()
  }, []);

  if (isLoading) {
    return (
      <div className="space-y-6">
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
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-ankur-secondary to-[#2a4a73] rounded-2xl p-8 text-white shadow-lg">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold mb-2">
              Welcome back, {user?.name?.split(' ')[0] || 'Student'}! 👋
            </h1>
            <p className="text-white/80">
              Here&apos;s a quick overview of what&apos;s happening today.
            </p>
          </div>
          <div className="flex items-center gap-6">
            <div className="text-center">
              <p className="text-4xl font-bold text-ankur-accent">{stats?.upcomingBookings || 0}</p>
              <p className="text-sm text-white/70">Upcoming Classes</p>
            </div>
            <div className="text-center">
              <p className="text-4xl font-bold text-white">{stats?.completedBookings || 0}</p>
              <p className="text-sm text-white/70">Completed</p>
            </div>
          </div>
        </div>
      </div>

      {/* Platform Overview - Stats Cards */}
      <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
        <div className="mb-6">
          <h2 className="text-xl font-bold text-gray-900">Platform Overview</h2>
          <p className="text-sm text-gray-500">KEY METRICS AT A GLANCE</p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="flex items-center space-x-4 p-4 bg-emerald-50 rounded-xl">
            <div className="h-14 w-14 bg-gradient-to-br from-emerald-400 to-emerald-600 rounded-xl flex items-center justify-center shadow-lg">
              <Calendar className="h-7 w-7 text-white" />
            </div>
            <div>
              <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Upcoming</p>
              <p className="text-3xl font-bold text-gray-900">{stats?.upcomingBookings ?? 0}</p>
            </div>
          </div>

          <div className="flex items-center space-x-4 p-4 bg-blue-50 rounded-xl">
            <div className="h-14 w-14 bg-gradient-to-br from-blue-400 to-blue-600 rounded-xl flex items-center justify-center shadow-lg">
              <BookOpen className="h-7 w-7 text-white" />
            </div>
            <div>
              <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Completed</p>
              <p className="text-3xl font-bold text-gray-900">{stats?.completedBookings ?? 0}</p>
            </div>
          </div>

          <div className="flex items-center space-x-4 p-4 bg-purple-50 rounded-xl">
            <div className="h-14 w-14 bg-gradient-to-br from-purple-400 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
              <Clock className="h-7 w-7 text-white" />
            </div>
            <div>
              <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Hours</p>
              <p className="text-3xl font-bold text-gray-900">{stats?.totalHoursSpent ?? 0}</p>
            </div>
          </div>

          <div className="flex items-center space-x-4 p-4 bg-amber-50 rounded-xl">
            <div className="h-14 w-14 bg-gradient-to-br from-amber-400 to-amber-600 rounded-xl flex items-center justify-center shadow-lg">
              <TrendingUp className="h-7 w-7 text-white" />
            </div>
            <div>
              <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide">Avg Score</p>
              <p className="text-3xl font-bold text-gray-900">
                {stats?.subjectMastery && stats.subjectMastery.length > 0
                  ? Math.round(stats.subjectMastery.reduce((acc, subject) => acc + subject.averageScore, 0) / stats.subjectMastery.length)
                  : 0}%
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
        <div className="mb-6">
          <h2 className="text-xl font-bold text-gray-900">Quick Actions</h2>
          <p className="text-sm text-gray-500">FREQUENT TASKS</p>
        </div>
        
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Link href="/student/booking">
            <div className="flex flex-col items-center justify-center p-6 bg-gradient-to-br from-emerald-50 to-teal-50 rounded-xl hover:shadow-lg transition-all cursor-pointer border-2 border-transparent hover:border-emerald-200">
              <div className="h-14 w-14 bg-white rounded-xl flex items-center justify-center mb-3 shadow-sm">
                <BookOpen className="h-6 w-6 text-emerald-600" />
              </div>
              <p className="text-sm font-semibold text-gray-700">BOOK CLASS</p>
            </div>
          </Link>
          
          <Link href="/student/study-list">
            <div className="flex flex-col items-center justify-center p-6 bg-gradient-to-br from-blue-50 to-cyan-50 rounded-xl hover:shadow-lg transition-all cursor-pointer border-2 border-transparent hover:border-blue-200">
              <div className="h-14 w-14 bg-white rounded-xl flex items-center justify-center mb-3 shadow-sm">
                <Target className="h-6 w-6 text-blue-600" />
              </div>
              <p className="text-sm font-semibold text-gray-700">VIEW REPORTS</p>
            </div>
          </Link>
          
          <Link href="/student/calendar">
            <div className="flex flex-col items-center justify-center p-6 bg-gradient-to-br from-purple-50 to-pink-50 rounded-xl hover:shadow-lg transition-all cursor-pointer border-2 border-transparent hover:border-purple-200">
              <div className="h-14 w-14 bg-white rounded-xl flex items-center justify-center mb-3 shadow-sm">
                <Calendar className="h-6 w-6 text-purple-600" />
              </div>
              <p className="text-sm font-semibold text-gray-700">CALENDAR</p>
            </div>
          </Link>
          
          <Link href="/student/notifications">
            <div className="flex flex-col items-center justify-center p-6 bg-gradient-to-br from-amber-50 to-orange-50 rounded-xl hover:shadow-lg transition-all cursor-pointer border-2 border-transparent hover:border-amber-200">
              <div className="h-14 w-14 bg-white rounded-xl flex items-center justify-center mb-3 shadow-sm">
                <Bell className="h-6 w-6 text-amber-600" />
              </div>
              <p className="text-sm font-semibold text-gray-700">NOTIFICATIONS</p>
            </div>
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Upcoming Classes */}
        <Card className="border-gray-100 shadow-sm">
          <CardHeader className="border-b border-gray-100 bg-gray-50/50">
            <CardTitle className="flex items-center text-lg">
              <Calendar className="h-5 w-5 mr-2 text-emerald-500" />
              Upcoming Classes
            </CardTitle>
            <CardDescription>Your next scheduled sessions</CardDescription>
          </CardHeader>
          <CardContent className="pt-6">
            <div className="space-y-3">
              {stats?.upcomingClasses && stats.upcomingClasses.length > 0 ? (
                stats.upcomingClasses.map((classItem) => (
                <div key={classItem.bookingId} className="border border-gray-200 rounded-xl p-4 hover:border-emerald-300 hover:shadow-md transition-all bg-white">
                  <div className="flex items-center justify-between mb-3">
                    <h3 className="font-bold text-gray-900">{classItem.topicTitle}</h3>
                    <Badge className="bg-emerald-100 text-emerald-700 hover:bg-emerald-100 border-0">
                      {classItem.status}
                    </Badge>
                  </div>
                  <div className="flex items-center text-sm text-gray-600 mb-2">
                    <Users className="h-4 w-4 mr-2" />
                    {classItem.teacherName}
                  </div>
                  <div className="flex items-center text-sm text-gray-600 mb-3">
                    <Clock className="h-4 w-4 mr-2" />
                    {new Date(classItem.startTime).toLocaleDateString()} at{' '}
                    {new Date(classItem.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </div>
                  <Button size="sm" className="w-full bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600">
                    Join Class
                  </Button>
                </div>
                ))
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <Calendar className="h-12 w-12 mx-auto mb-3 text-gray-300" />
                  <p>No upcoming classes scheduled</p>
                  <Link href="/student/booking">
                    <Button variant="outline" className="mt-4">
                      Book Your First Class
                    </Button>
                  </Link>
                </div>
              )}
              {stats?.upcomingClasses && stats.upcomingClasses.length > 0 && (
                <Link href="/student/calendar">
                  <Button variant="outline" className="w-full mt-2 border-gray-300 hover:bg-gray-50">
                    View All Classes
                  </Button>
                </Link>
              )}
            </div>
          </CardContent>
        </Card>

        {/* Subject Mastery */}
        <Card className="border-gray-100 shadow-sm">
          <CardHeader className="border-b border-gray-100 bg-gray-50/50">
            <CardTitle className="flex items-center text-lg">
              <Target className="h-5 w-5 mr-2 text-emerald-500" />
              Subject Mastery
            </CardTitle>
            <CardDescription>Your progress across different subjects</CardDescription>
          </CardHeader>
          <CardContent className="pt-6">
            <div className="space-y-4">
              {stats?.subjectMastery && stats.subjectMastery.length > 0 ? (
                stats.subjectMastery.map((subject) => (
                <div key={subject.subjectId} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-gray-900">{subject.subjectName}</span>
                    <span className="text-sm text-gray-600">{subject.masteryPercentage}%</span>
                  </div>
                  <Progress value={subject.masteryPercentage} className="h-2" />
                  <div className="flex items-center justify-between text-sm text-gray-600">
                    <span>{subject.totalQuizzes} quizzes taken</span>
                    <span>Avg: {subject.averageScore}%</span>
                  </div>
                </div>
                ))
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <Target className="h-12 w-12 mx-auto mb-3 text-gray-300" />
                  <p>No subject data available yet</p>
                  <p className="text-sm">Complete some classes to see your progress</p>
                </div>
              )}
              {stats?.subjectMastery && stats.subjectMastery.length > 0 && (
                <Link href="/student/history">
                  <Button variant="outline" className="w-full">
                    View Detailed Progress
                  </Button>
                </Link>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Recent Activity */}
      <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
        <div className="mb-6">
          <h2 className="text-xl font-bold text-gray-900">Recent Activity</h2>
          <p className="text-sm text-gray-500">LIVE FEED</p>
        </div>
        
        <div className="space-y-3">
          {stats?.recommendations && stats.recommendations.length > 0 ? (
            stats.recommendations.map((rec, index) => (
              <div key={index} className="flex items-start space-x-3 p-3 rounded-lg hover:bg-gray-50 transition-colors">
                <div className="h-2 w-2 bg-emerald-500 rounded-full mt-2"></div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">{rec.title}</p>
                  <p className="text-xs text-gray-500">{rec.reason}</p>
                </div>
              </div>
            ))
          ) : (
            <div className="text-center py-8 text-gray-500">
              <Brain className="h-12 w-12 mx-auto mb-3 text-gray-300" />
              <p>No recent activity</p>
              <p className="text-sm">Your activity will appear here</p>
            </div>
          )}
        </div>
      </div>

      {/* Announcements */}
      <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
        <div className="mb-6">
          <h2 className="text-xl font-bold text-gray-900">Announcements</h2>
          <p className="text-sm text-gray-500">LATEST UPDATES</p>
        </div>
        
        <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center">
          <p className="text-gray-500">No announcements yet.</p>
        </div>
      </div>
    </div>
  );
}