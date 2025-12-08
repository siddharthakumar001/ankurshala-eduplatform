'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { 
  Play, 
  Square, 
  MessageSquare, 
  Star, 
  Clock, 
  User, 
  BookOpen,
  CheckCircle,
  AlertCircle,
  Loader2,
  ThumbsUp,
  ThumbsDown,
  DollarSign
} from 'lucide-react'
import { toast } from 'sonner'
import { api } from '@/utils/api'
import { TeacherRoute } from '@/components/route-guard'

interface SessionBooking {
  id: number
  studentId: number
  studentName: string
  studentEmail: string
  topicId: number
  topicTitle: string
  subjectName: string
  startTime: string
  endTime: string
  durationMinutes: number
  status: string
  priceMin: number
  priceMax: number
  studentNotes?: string
  createdAt: string
}

interface SessionFeedback {
  bookingId: number
  sessionRating: number
  studentEngagement: string
  sessionNotes: string
  improvementSuggestions: string
  wouldRecommend: boolean
}

export default function TeacherSessionManagementPage() {
  const [loading, setLoading] = useState(true)
  const [sessions, setSessions] = useState<SessionBooking[]>([])
  const [activeSession, setActiveSession] = useState<SessionBooking | null>(null)
  const [sessionNotes, setSessionNotes] = useState('')
  const [showFeedbackDialog, setShowFeedbackDialog] = useState<number | null>(null)
  const [feedback, setFeedback] = useState<SessionFeedback>({
    bookingId: 0,
    sessionRating: 5,
    studentEngagement: '',
    sessionNotes: '',
    improvementSuggestions: '',
    wouldRecommend: true
  })

  useEffect(() => {
    fetchSessions()
  }, [])

  const fetchSessions = async () => {
    try {
      setLoading(true)
      
      // Fetch accepted and completed sessions
      const response = await api.get('/teacher/bookings')
      const allBookings = (response.data as any) || []
      
      // Filter for sessions that can be managed (accepted, completed)
      const sessionBookings = allBookings.filter((booking: SessionBooking) => 
        booking.status === 'ACCEPTED' || booking.status === 'COMPLETED'
      )
      
      setSessions(sessionBookings)
      
    } catch (error) {
      console.error('Error fetching sessions:', error)
      toast.error('Failed to load sessions')
    } finally {
      setLoading(false)
    }
  }

  const handleStartSession = async (bookingId: number) => {
    try {
      await api.post(`/teacher/sessions/${bookingId}/start`)
      toast.success('Session started successfully')
      setActiveSession(sessions.find(s => s.id === bookingId) || null)
      fetchSessions() // Refresh data
    } catch (error) {
      console.error('Error starting session:', error)
      toast.error('Failed to start session')
    }
  }

  const handleEndSession = async (bookingId: number) => {
    try {
      await api.post(`/teacher/sessions/${bookingId}/end`, {
        sessionNotes
      })
      toast.success('Session ended successfully')
      setActiveSession(null)
      setSessionNotes('')
      fetchSessions() // Refresh data
    } catch (error) {
      console.error('Error ending session:', error)
      toast.error('Failed to end session')
    }
  }

  const handleSubmitFeedback = async () => {
    try {
      await api.post('/teacher/sessions/feedback', feedback)
      toast.success('Session feedback submitted successfully')
      setShowFeedbackDialog(null)
      setFeedback({
        bookingId: 0,
        sessionRating: 5,
        studentEngagement: '',
        sessionNotes: '',
        improvementSuggestions: '',
        wouldRecommend: true
      })
      fetchSessions() // Refresh data
    } catch (error) {
      console.error('Error submitting feedback:', error)
      toast.error('Failed to submit feedback')
    }
  }

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount)
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACCEPTED':
        return <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">Ready to Start</Badge>
      case 'COMPLETED':
        return <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-200">Completed</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const canStartSession = (session: SessionBooking) => {
    const now = new Date()
    const startTime = new Date(session.startTime)
    const timeDiff = startTime.getTime() - now.getTime()
    const minutesDiff = timeDiff / (1000 * 60)
    
    // Can start 15 minutes before or after the scheduled time
    return minutesDiff >= -15 && minutesDiff <= 15 && session.status === 'ACCEPTED'
  }

  if (loading) {
    return (
      <TeacherRoute>
        <div className="min-h-screen bg-gray-50 py-8">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-center h-64">
              <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
              <span className="ml-2 text-gray-600">Loading sessions...</span>
            </div>
          </div>
        </div>
      </TeacherRoute>
    )
  }

  return (
    <TeacherRoute>
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-bold text-gray-900">Session Management</h1>
            <p className="text-gray-600">Start, manage, and provide feedback for your teaching sessions</p>
          </div>

          {/* Active Session Alert */}
          {activeSession && (
            <Card className="mb-6 border-green-200 bg-green-50">
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
                    <div>
                      <h3 className="font-medium text-green-900">Session in Progress</h3>
                      <p className="text-sm text-green-700">
                        Teaching {activeSession.studentName} - {activeSession.topicTitle}
                      </p>
                    </div>
                  </div>
                  <Button
                    onClick={() => handleEndSession(activeSession.id)}
                    className="bg-red-600 hover:bg-red-700"
                  >
                    <Square className="h-4 w-4 mr-2" />
                    End Session
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Session Notes for Active Session */}
          {activeSession && (
            <Card className="mb-6">
              <CardHeader>
                <CardTitle>Session Notes</CardTitle>
                <CardDescription>Add notes during the session</CardDescription>
              </CardHeader>
              <CardContent>
                <Textarea
                  placeholder="Add session notes, student progress, or important observations..."
                  value={sessionNotes}
                  onChange={(e) => setSessionNotes(e.target.value)}
                  rows={4}
                />
              </CardContent>
            </Card>
          )}

          {/* Sessions List */}
          <div className="space-y-4">
            {sessions.length === 0 ? (
              <Card>
                <CardContent className="text-center py-12">
                  <BookOpen className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                  <h3 className="text-lg font-medium text-gray-900 mb-2">No sessions found</h3>
                  <p className="text-gray-500">No accepted or completed sessions available.</p>
                </CardContent>
              </Card>
            ) : (
              sessions.map((session) => (
                <Card key={session.id}>
                  <CardContent className="p-6">
                    <div className="flex justify-between items-start mb-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <h3 className="text-lg font-medium">{session.studentName}</h3>
                          {getStatusBadge(session.status)}
                        </div>
                        <p className="text-gray-600 mb-1">{session.topicTitle} - {session.subjectName}</p>
                        <div className="flex items-center gap-4 text-sm text-gray-500">
                          <span className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            {formatDateTime(session.startTime)}
                          </span>
                          <span className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            {session.durationMinutes} minutes
                          </span>
                          <span className="flex items-center gap-1">
                            <DollarSign className="h-4 w-4" />
                            {formatCurrency(session.priceMin)} - {formatCurrency(session.priceMax)}
                          </span>
                        </div>
                        {session.studentNotes && (
                          <div className="mt-3 p-3 bg-gray-50 rounded-lg">
                            <p className="text-sm text-gray-600">
                              <strong>Student Notes:</strong> {session.studentNotes}
                            </p>
                          </div>
                        )}
                      </div>
                      
                      <div className="flex gap-2">
                        {session.status === 'ACCEPTED' && canStartSession(session) && (
                          <Button
                            onClick={() => handleStartSession(session.id)}
                            className="bg-green-600 hover:bg-green-700"
                          >
                            <Play className="h-4 w-4 mr-2" />
                            Start Session
                          </Button>
                        )}
                        
                        {session.status === 'ACCEPTED' && !canStartSession(session) && (
                          <div className="text-sm text-gray-500 text-center">
                            <p>Session can be started</p>
                            <p>15 min before/after</p>
                            <p>scheduled time</p>
                          </div>
                        )}
                        
                        {session.status === 'COMPLETED' && (
                          <Dialog open={showFeedbackDialog === session.id} onOpenChange={(open) => setShowFeedbackDialog(open ? session.id : null)}>
                            <DialogTrigger asChild>
                              <Button variant="outline">
                                <MessageSquare className="h-4 w-4 mr-2" />
                                Add Feedback
                              </Button>
                            </DialogTrigger>
                            <DialogContent className="max-w-2xl">
                              <DialogHeader>
                                <DialogTitle>Session Feedback</DialogTitle>
                              </DialogHeader>
                              <SessionFeedbackForm
                                session={session}
                                feedback={feedback}
                                setFeedback={setFeedback}
                                onSubmit={handleSubmitFeedback}
                                onCancel={() => setShowFeedbackDialog(null)}
                              />
                            </DialogContent>
                          </Dialog>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </div>
        </div>
      </div>
    </TeacherRoute>
  )
}

// Session Feedback Form Component
function SessionFeedbackForm({ 
  session, 
  feedback, 
  setFeedback, 
  onSubmit, 
  onCancel 
}: { 
  session: SessionBooking
  feedback: SessionFeedback
  setFeedback: (feedback: SessionFeedback) => void
  onSubmit: () => void
  onCancel: () => void
}) {
  const handleRatingChange = (rating: number) => {
    setFeedback({ ...feedback, sessionRating: rating })
  }

  const handleRecommendationChange = (recommend: boolean) => {
    setFeedback({ ...feedback, wouldRecommend: recommend })
  }

  return (
    <div className="space-y-6">
      <div>
        <Label>Session Rating</Label>
        <div className="flex items-center gap-2 mt-2">
          {[1, 2, 3, 4, 5].map((rating) => (
            <button
              key={rating}
              onClick={() => handleRatingChange(rating)}
              className={`p-2 rounded-full transition-colors ${
                feedback.sessionRating >= rating
                  ? 'text-yellow-400 bg-yellow-50'
                  : 'text-gray-300 hover:text-yellow-400'
              }`}
            >
              <Star className="h-6 w-6 fill-current" />
            </button>
          ))}
          <span className="ml-2 text-sm text-gray-600">
            {feedback.sessionRating}/5 stars
          </span>
        </div>
      </div>

      <div>
        <Label htmlFor="student-engagement">Student Engagement</Label>
        <Textarea
          id="student-engagement"
          placeholder="How was the student's engagement during the session?"
          value={feedback.studentEngagement}
          onChange={(e) => setFeedback({ ...feedback, studentEngagement: e.target.value })}
          rows={3}
        />
      </div>

      <div>
        <Label htmlFor="session-notes">Session Notes</Label>
        <Textarea
          id="session-notes"
          placeholder="Key points covered, student progress, areas of improvement..."
          value={feedback.sessionNotes}
          onChange={(e) => setFeedback({ ...feedback, sessionNotes: e.target.value })}
          rows={4}
        />
      </div>

      <div>
        <Label htmlFor="improvement-suggestions">Improvement Suggestions</Label>
        <Textarea
          id="improvement-suggestions"
          placeholder="Suggestions for the student's continued learning..."
          value={feedback.improvementSuggestions}
          onChange={(e) => setFeedback({ ...feedback, improvementSuggestions: e.target.value })}
          rows={3}
        />
      </div>

      <div>
        <Label>Would you recommend this student for future sessions?</Label>
        <div className="flex items-center gap-4 mt-2">
          <button
            onClick={() => handleRecommendationChange(true)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg border transition-colors ${
              feedback.wouldRecommend
                ? 'bg-green-50 border-green-200 text-green-700'
                : 'border-gray-200 text-gray-600 hover:bg-gray-50'
            }`}
          >
            <ThumbsUp className="h-4 w-4" />
            Yes
          </button>
          <button
            onClick={() => handleRecommendationChange(false)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg border transition-colors ${
              !feedback.wouldRecommend
                ? 'bg-red-50 border-red-200 text-red-700'
                : 'border-gray-200 text-gray-600 hover:bg-gray-50'
            }`}
          >
            <ThumbsDown className="h-4 w-4" />
            No
          </button>
        </div>
      </div>

      <div className="flex gap-2">
        <Button onClick={onSubmit} className="flex-1">
          <CheckCircle className="h-4 w-4 mr-2" />
          Submit Feedback
        </Button>
        <Button variant="outline" onClick={onCancel} className="flex-1">
          Cancel
        </Button>
      </div>
    </div>
  )
}
